package cn.wekyjay.wknetic.admin.system.service;

import cn.wekyjay.wknetic.admin.config.MicrosoftOAuthConfig;
import cn.wekyjay.wknetic.common.enums.DeviceFlowStatus;
import cn.wekyjay.wknetic.common.model.dto.DeviceFlowStateDTO;
import cn.wekyjay.wknetic.common.utils.RedisUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 设备流状态服务
 * 管理设备流状态在Redis中的存储和检索
 */
@Slf4j
@Service
public class DeviceFlowStateService {
    
    private static final String REDIS_KEY_PREFIX = "device_flow:";
    private static final long DEFAULT_EXPIRY_SECONDS = 900; // 15分钟
    
    private final RedisUtils redisUtils;
    private final MicrosoftOAuthConfig microsoftOAuthConfig;
    private final ObjectMapper objectMapper;
    
    public DeviceFlowStateService(RedisUtils redisUtils, MicrosoftOAuthConfig microsoftOAuthConfig) {
        this.redisUtils = redisUtils;
        this.microsoftOAuthConfig = microsoftOAuthConfig;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    

    /**
     * 创建新的设备流状态
     */
    public DeviceFlowStateDTO createDeviceFlowState(String userCode,String deviceCode, String verificationUri, String verificationUriComplete, Long userId) {
        DeviceFlowStateDTO state = new DeviceFlowStateDTO();
        state.setDeviceCode(deviceCode);
        state.setUserCode(userCode);
        state.setVerificationUri(verificationUri);
        state.setVerificationUriComplete(verificationUriComplete);
        state.setStatus(DeviceFlowStatus.PENDING);

        state.setUserId(userId);
        // 设置过期时间（默认5分钟）
        long expirySeconds = microsoftOAuthConfig.getPollingTimeout() > 0 
                ? microsoftOAuthConfig.getPollingTimeout() / 1000
                : DEFAULT_EXPIRY_SECONDS;
        state.setExpiresAt(LocalDateTime.now().plusSeconds(expirySeconds));
        
        // 设置轮询间隔（默认5秒）
        state.setInterval(microsoftOAuthConfig.getPollingInterval());
        
        // 计算剩余时间
        state.calculateTimeRemaining();
        
        // 保存到Redis
        saveDeviceFlowState(state);
        
        log.info("创建设备流状态: deviceCode={}, userCode={}", 
                state.getDeviceCode(), userCode, userId);
        
        return state;
    }
    
    /**
     * 保存设备流状态到Redis
     */
    public void saveDeviceFlowState(DeviceFlowStateDTO state) {
        try {
            String key = getRedisKey(state.getDeviceCode());
            String json = objectMapper.writeValueAsString(state);
            
            // 计算剩余过期时间
            long timeRemaining = state.getExpiresAt().toEpochSecond(ZoneOffset.UTC) - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            if (timeRemaining > 0) {
                redisUtils.set(key, json, timeRemaining, TimeUnit.SECONDS);
            } else {
                // 如果已经过期，设置较短过期时间
                redisUtils.set(key, json, 30, TimeUnit.SECONDS);
                
                log.warn("设备流状态已过期或即将过期: deviceCode={}", state.getDeviceCode());
            }
            
        } catch (JsonProcessingException e) {
            log.error("序列化设备流状态失败: deviceCode={}", state.getDeviceCode(), e);
            throw new RuntimeException("序列化设备流状态失败", e);
        }
    }
    
    /**
     * 从Redis获取设备流状态
     */
    public DeviceFlowStateDTO getDeviceFlowState(String deviceCode) {
        try {
            String key = getRedisKey(deviceCode);
            String json = redisUtils.get(key);
            
            if (json == null) {
                log.warn("设备流状态不存在或已过期: deviceCode={}", deviceCode);
                return null;
            }
            
            DeviceFlowStateDTO state = objectMapper.readValue(json, DeviceFlowStateDTO.class);
            
            // 检查是否过期
            if (state.isExpired()) {
                log.info("设备流状态已过期: deviceCode={}", deviceCode);
                state.setStatus(DeviceFlowStatus.EXPIRED);
                state.setTerminal(true);
                saveDeviceFlowState(state); // 更新状态
                return state;
            }
            
            // 更新剩余时间
            state.calculateTimeRemaining();
            
            return state;
            
        } catch (JsonProcessingException e) {
            log.error("反序列化设备流状态失败: deviceCode={}", deviceCode, e);
            return null;
        }
    }
    
    /**
     * 更新设备流状态
     */
    public DeviceFlowStateDTO updateDeviceFlowStatus(String deviceCode, DeviceFlowStatus newStatus) {
        DeviceFlowStateDTO state = getDeviceFlowState(deviceCode);
        if (state == null) {
            log.warn("设备流状态不存在，无法更新: deviceCode={}", deviceCode);
            return null;
        }
        
        state.setStatus(newStatus);
        state.calculateTimeRemaining();
        
        // 如果是终端状态，设置较短过期时间（5分钟）
        if (newStatus.isTerminal()) {
            state.setExpiresAt(LocalDateTime.now().plusSeconds(300));
        }
        
        saveDeviceFlowState(state);
        log.info("更新设备流状态: deviceCode={}, newStatus={}", deviceCode, newStatus);
        
        return state;
    }
    
    /**
     * 更新设备流状态并设置额外信息
     */
    public DeviceFlowStateDTO updateDeviceFlowStatus(String deviceCode, DeviceFlowStatus newStatus, 
                                                    String microsoftAccessToken, String minecraftUuid, 
                                                    String minecraftUsername) {
        DeviceFlowStateDTO state = getDeviceFlowState(deviceCode);
        if (state == null) {
            log.warn("设备流状态不存在，无法更新: deviceCode={}", deviceCode);
            return null;
        }
        
        state.setStatus(newStatus);
        state.setMicrosoftAccessToken(microsoftAccessToken);
        state.setMinecraftUuid(minecraftUuid);
        state.setMinecraftUsername(minecraftUsername);
        state.calculateTimeRemaining();
        
        // 如果是终端状态，设置较短过期时间（5分钟）
        if (newStatus.isTerminal()) {
            state.setExpiresAt(LocalDateTime.now().plusSeconds(300));
        }
        
        saveDeviceFlowState(state);
        log.info("更新设备流状态并设置Minecraft信息: deviceCode={}, newStatus={}, minecraftUuid={}", 
                deviceCode, newStatus, minecraftUuid);
        
        return state;
    }
    
    /**
     * 删除设备流状态
     */
    public boolean deleteDeviceFlowState(String deviceCode) {
        String key = getRedisKey(deviceCode);
        Boolean deleted = redisUtils.delete(key);
        
        if (Boolean.TRUE.equals(deleted)) {
            log.info("删除设备流状态: deviceCode={}", deviceCode);
            return true;
        } else {
            log.warn("删除设备流状态失败或不存在: deviceCode={}", deviceCode);
            return false;
        }
    }
    
    /**
     * 清理过期的设备流状态
     */
    public void cleanupExpiredStates() {
        // Redis会自动过期删除，这里主要做日志记录
        log.debug("清理过期设备流状态 - Redis自动处理");
    }
    
    /**
     * 获取Redis键名
     */
    private String getRedisKey(String deviceCode) {
        return REDIS_KEY_PREFIX + deviceCode;
    }
    
    /**
     * 检查设备流状态是否存在
     */
    public boolean exists(String deviceCode) {
        String key = getRedisKey(deviceCode);
        return Boolean.TRUE.equals(redisUtils.hasKey(key));
    }
    
    /**
     * 获取设备流状态（如果不存在返回默认值）
     */
    public DeviceFlowStateDTO getDeviceFlowStateOrDefault(String deviceCode) {
        DeviceFlowStateDTO state = getDeviceFlowState(deviceCode);
        if (state == null) {
            state = new DeviceFlowStateDTO();
            state.setDeviceCode(deviceCode);
            state.setStatus(DeviceFlowStatus.EXPIRED);
            state.setStatusDescription("设备流状态不存在或已过期");
            state.setTerminal(true);
        }
        return state;
    }
}