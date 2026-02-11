package cn.wekyjay.wknetic.admin.game.service.impl;

import cn.wekyjay.wknetic.admin.game.service.GameChatService;
import cn.wekyjay.wknetic.admin.system.service.ISysServerTokenService;
import cn.wekyjay.wknetic.api.enums.PacketType;
import cn.wekyjay.wknetic.api.model.packet.AdminCommandPacket;
import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.mapper.SysUserMapper;
import cn.wekyjay.wknetic.common.model.dto.ChatHistoryDTO;
import cn.wekyjay.wknetic.common.model.dto.SendChatMessageDTO;
import cn.wekyjay.wknetic.common.model.vo.ChatMessageVO;
import cn.wekyjay.wknetic.common.utils.RedisUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 游戏聊天服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameChatServiceImpl implements GameChatService {
    
    private final RedisUtils redisUtils;
    private final StringRedisTemplate redisTemplate;
    private final SysUserMapper userMapper;
    private final ObjectMapper objectMapper;
    
    private static final String CHAT_HISTORY_KEY = "wknetic:chat:history";
    private static final String CHAT_CHANNEL = "wknetic:chat:message";
    private static final int MAX_HISTORY_SIZE = 500;
    private static final int DEFAULT_LIMIT = 100;
    
    @Override
    public List<ChatMessageVO> getChatHistory(ChatHistoryDTO dto) {
        try {
            // 从Redis获取历史消息
            int limit = dto.getLimit() != null ? dto.getLimit() : DEFAULT_LIMIT;
            List<String> messages = redisTemplate.opsForList().range(CHAT_HISTORY_KEY, -limit, -1);
            log.info("从Redis获取聊天历史: server={}, channel={}, world={}, limit={}", 
                dto.getServerName(), dto.getChannel(), dto.getWorld(), limit);
            log.info("原始历史消息数量: {}", messages != null ? messages.size() : 0);
            
            if (messages == null || messages.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 解析消息并筛选
            List<ChatMessageVO> result = new ArrayList<>();
            for (String obj : messages) {
                if (obj == null) {
                    log.warn("历史消息列表中存在null值，跳过");
                    continue; // 跳过 null 值
                }
                try {
                    log.debug("读取到的Redis对象类型: {}, 值: {}", obj.getClass().getName(), obj);
                    
                    ChatMessageVO message = null;
                    
                    // 尝试不同的解析方式
    
                    if (obj instanceof String) {
                        // 如果是字符串，尝试解析为JSON
                        try {
                            message = objectMapper.readValue((String) obj, ChatMessageVO.class);
                            log.info("从JSON字符串解析ChatMessageVO: {}", message);
                        } catch (JsonProcessingException e) {
                            log.error("JSON字符串解析失败: {}", obj, e);
                        }
                    } else {
                        // 其他类型，尝试toString后解析
                        try {
                            message = objectMapper.readValue(obj.toString(), ChatMessageVO.class);
                            log.info("从对象toString()解析ChatMessageVO: {}", message);
                        } catch (JsonProcessingException e) {
                            log.error("对象toString()解析失败: {}", obj, e);
                        }
                    }
                    
                    if (message == null || message.getServerName() == null) {
                        log.warn("消息解析失败或serverName为空: {}", obj);
                        continue;
                    }
                    
                    // 筛选条件
                    if (!dto.getServerName().equals(message.getServerName())) {
                        log.debug("服务器名称不匹配: 期望={}, 实际={}", dto.getServerName(), message.getServerName());
                        continue;
                    }
                    
                    // 如果请求了特定频道但消息频道不匹配，跳过
                    if (StringUtils.hasText(dto.getChannel()) && !dto.getChannel().equalsIgnoreCase(message.getChannel())) {
                        log.debug("频道不匹配: 期望={}, 实际={}", dto.getChannel(), message.getChannel());
                        continue;
                    }

                    // 如果请求了特定世界但消息世界不匹配，跳过
                    if (StringUtils.hasText(dto.getWorld()) && !"all".equalsIgnoreCase(dto.getWorld()) 
                            && !dto.getWorld().equalsIgnoreCase(message.getWorld())) {
                        log.debug("世界不匹配: 期望={}, 实际={}", dto.getWorld(), message.getWorld());
                        continue;
                    }
                    
                    log.info("消息通过筛选: {}", message);
                    result.add(message);
                } catch (Exception e) {
                    log.error("处理聊天消息时发生未知错误: {}", obj, e);
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取聊天历史失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean sendChatMessage(SendChatMessageDTO dto, Long userId) {
        try {
            // 获取用户信息
            SysUser user = userMapper.selectById(userId);
            if (user == null || user.getMinecraftUsername() == null) {
                log.warn("用户未绑定Minecraft账号: {}", userId);
                return false;
            }
            
            // 构建消息内容（添加前缀）
            String messageContent = String.format("[来自网页] %s: %s", 
                user.getMinecraftUsername(), dto.getContent());
            
            // 创建AdminCommandPacket发送到插件端
            AdminCommandPacket packet = new AdminCommandPacket();

            packet.setToken(dto.getServerName());
            packet.setSessionId(dto.getServerName());
            packet.setCommand("say " + messageContent);
            packet.setCommandType("COMMAND");
            packet.setCommandId(UUID.randomUUID().toString());
            
            // 通过Redis发布到插件端
            String packetJson = objectMapper.writeValueAsString(packet);
            redisTemplate.convertAndSend("wknetic:admin:command", packetJson);
            
            // 创建消息对象
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
            
            // 保存到Redis历史
            saveChatMessage(message);
            
            // 发布到Redis供其他前端订阅
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
            // 直接存储ChatMessageVO对象，RedisTemplate的序列化器会自动处理
            redisTemplate.opsForList().rightPush(CHAT_HISTORY_KEY, message);
            
            // 限制历史消息数量
            Long size = redisTemplate.opsForList().size(CHAT_HISTORY_KEY);
            if (size != null && size > MAX_HISTORY_SIZE) {
                redisTemplate.opsForList().trim(CHAT_HISTORY_KEY, -MAX_HISTORY_SIZE, -1);
            }
        } catch (Exception e) {
            log.error("保存聊天消息失败", e);
        }
    }
    
    @Override
    public void publishChatMessage(ChatMessageVO message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(CHAT_CHANNEL, messageJson);
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
