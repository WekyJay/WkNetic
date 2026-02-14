package cn.wekyjay.wknetic.admin.system.service.impl;

import cn.wekyjay.wknetic.admin.system.service.GameChatService;
import cn.wekyjay.wknetic.admin.system.service.ISysServerTokenService;
import cn.wekyjay.wknetic.api.enums.PacketType;
import cn.wekyjay.wknetic.api.model.packet.AdminCommandPacket;
import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.mapper.SysUserMapper;
import cn.wekyjay.wknetic.common.model.dto.ChatHistoryDTO;
import cn.wekyjay.wknetic.common.model.dto.SendChatMessageDTO;
import cn.wekyjay.wknetic.common.model.vo.ChatMessageVO;
import cn.wekyjay.wknetic.common.model.vo.ServerStatusVO;
import cn.wekyjay.wknetic.common.utils.RedisUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * 游戏聊天服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameChatServiceImpl implements GameChatService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate; 
    private final SysUserMapper userMapper;
    private final ObjectMapper objectMapper;
    
    private static final String CHAT_HISTORY_KEY = "wknetic:chat:history";
    private static final String CHAT_CHANNEL = "wknetic:chat:message";
    private static final int MAX_HISTORY_SIZE = 500;
    private static final int DEFAULT_LIMIT = 100;
    
    @Override
    public List<ChatMessageVO> getChatHistory(ChatHistoryDTO dto) {
        try {
            int limit = dto.getLimit() != null ? dto.getLimit() : DEFAULT_LIMIT;
            
            // 【关键修改】使用 stringRedisTemplate，强制获取 String 类型的列表
            // 这样无论 Redis 里存的是什么鬼样子的 JSON，拿出来的绝对是 String
            List<String> rawJsonList = stringRedisTemplate.opsForList().range(CHAT_HISTORY_KEY, -limit, -1);
            
            if (rawJsonList == null || rawJsonList.isEmpty()) {
                return new ArrayList<>();
            }
            
            List<ChatMessageVO> result = new ArrayList<>();
            
            for (String jsonStr : rawJsonList) {
                // 排除 null 和 空字符串
                if (!StringUtils.hasText(jsonStr)) continue;
                
                try {
                    // 【关键修改】手动反序列化，通过这一步彻底解决类型转换问题
                    ChatMessageVO message = objectMapper.readValue(jsonStr, ChatMessageVO.class);
                    
                    // 筛选逻辑 (保持不变)
                    if (shouldIncludeMessage(message, dto)) {
                        result.add(message);
                    }
                } catch (Exception e) {
                    // 只有这里报错，才能说明是 JSON 格式本身有问题
                    log.error("JSON解析失败: {}", jsonStr, e);
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取聊天历史失败", e);
            return new ArrayList<>();
        }
    }

    // 抽取筛选逻辑，让主方法更干净
    private boolean shouldIncludeMessage(ChatMessageVO message, ChatHistoryDTO dto) {
        if (message == null || message.getServerName() == null) return false;
        
        if (!dto.getServerName().equals(message.getServerName())) return false;
        
        if (StringUtils.hasText(dto.getChannel()) && !dto.getChannel().equalsIgnoreCase(message.getChannel())) {
            return false;
        }

        if (StringUtils.hasText(dto.getWorld()) && !"all".equalsIgnoreCase(dto.getWorld()) 
                && !dto.getWorld().equalsIgnoreCase(message.getWorld())) {
            return false;
        }
        return true;
    }
    

    @Override
    public boolean sendChatMessage(SendChatMessageDTO dto, Long userId) {
        try {
            SysUser user = userMapper.selectById(userId);
            if (user == null || user.getMinecraftUsername() == null) {
                log.warn("用户未绑定Minecraft账号: {}", userId);
                return false;
            }
            String messageContent = String.format("[来自网页] %s: %s", 
                user.getMinecraftUsername(), dto.getContent());

            log.info("准备发送聊天消息到服务器 {}: {}", dto.toString(), messageContent);
            
            // --- 1. 根据服务器名称查找对应的sessionId ---
            String sessionId = dto.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                log.error("找不到服务器 {} 对应的sessionId，消息无法发送", dto.getServerName());
                return false;
            }
            
            // --- 2. 发送命令到插件端 (核心修改点) ---
            AdminCommandPacket packet = new AdminCommandPacket();
            packet.setToken(sessionId);
            packet.setSessionId(sessionId);
            packet.setCommand("say " + messageContent);
            packet.setCommandType("COMMAND");
            packet.setCommandId(UUID.randomUUID().toString());
            
            // 手动转 JSON
            String packetJson = objectMapper.writeValueAsString(packet);
            log.info("转换后的AdminCommandPacket JSON: {}", packetJson);
            
            // 【关键】使用 StringRedisTemplate 发送纯净的 JSON 字符串
            // 避免 RedisTemplate<String, Object> 把它变成带转义的字符串
            stringRedisTemplate.convertAndSend("wknetic:admin:command", packetJson);
            log.info("已发送聊天消息到服务器 {} [sessionId: {}]: {}", dto.getServerName(), sessionId, messageContent);
            
            // --- 2. 保存历史与通知前端 (保持不变，利用优化后的方法) ---
            ChatMessageVO message = ChatMessageVO.builder()
                .id(UUID.randomUUID().toString())
                .serverName(dto.getServerName())
                .channel(dto.getChannel())
                .world(dto.getWorld())
                .player(ChatMessageVO.PlayerInfo.builder()
                    .uuid(user.getMinecraftUuid())
                    .username(user.getMinecraftUsername())
                    .avatar(getMinecraftAvatarUrl(user.getMinecraftUuid()))
                    .build())
                .content(dto.getContent())
                .source("web")
                .timestamp(LocalDateTime.now())
                .build();
            
            // 这两个方法你已经优化过了，直接调用即可
            saveChatMessage(message);
            publishChatMessage(message);
            
            return true;
        } catch (Exception e) {
            log.error("发送聊天消息失败", e);
            return false;
        }
    }
    


    @Override
    public void saveChatMessage(ChatMessageVO message) {
        try {
            // 使用 SessionCallback，这样可以在 Pipeline 中复用 RedisTemplate 的序列化配置
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    // 强制转换 operations 以匹配你的泛型 <String, Object>
                    @SuppressWarnings("unchecked")
                    RedisOperations<String, Object> redisOps = (RedisOperations<String, Object>) operations;
                    
                    // 1. 直接传入对象，RedisTemplate 会自动调用配置好的 JSON 序列化器
                    redisOps.opsForList().rightPush(CHAT_HISTORY_KEY, message);
                    
                    // 2. 裁剪列表，保留最后 MAX_HISTORY_SIZE 条
                    redisOps.opsForList().trim(CHAT_HISTORY_KEY, -MAX_HISTORY_SIZE, -1);
                    
                    return null;
                }
            });
        } catch (Exception e) {
            log.error("保存聊天消息失败", e);
        }
    }
    
    @Override
    public void publishChatMessage(ChatMessageVO message) {
        try {
            redisTemplate.convertAndSend(CHAT_CHANNEL, message);
        } catch (Exception e) {
            log.error("发布聊天消息失败", e);
        }
    }
    
    /**
     * 获取Minecraft头像URL
     */
    private String getMinecraftAvatarUrl(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return "";
        }
        String cleanUuid = uuid.replace("-", "").toLowerCase();
        return "https://mc-heads.net/avatar/" + cleanUuid;
    }
}
