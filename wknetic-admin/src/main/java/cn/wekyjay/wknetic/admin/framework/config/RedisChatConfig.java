package cn.wekyjay.wknetic.admin.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.wekyjay.wknetic.common.model.vo.ChatMessageVO;

import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Configuration
public class RedisChatConfig {

    public static final String CHAT_TOPIC = "wknetic:chat:message";

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,MessageListenerAdapter listenerAdapter) {

        // 设置 Redis 消息监听容器
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 订阅 "wknetic-global-chat" 频道
        container.addMessageListener(listenerAdapter, new PatternTopic(CHAT_TOPIC));
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(ChatRedisReceiver receiver) {
        // 指定接收到消息后调用 receiver 的 receiveMessage 方法
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    /**
     * 内部类：真正的消息转发者
     * Redis -> WebSocket
     */
    @Slf4j
    @Component
    public static class ChatRedisReceiver {
        
        @Resource
        private SimpMessagingTemplate messagingTemplate; // WebSocket 发送工具
        
        @Resource
        private ObjectMapper objectMapper;

        // 这个方法会被反射调用
        public void receiveMessage(String message) {
            try {
                log.info("Redis聊天消息收到: {}", message);
                // 解析消息
                ChatMessageVO chatMessage = objectMapper.readValue(message, ChatMessageVO.class);
                
                if (chatMessage == null || chatMessage.getServerName() == null) {
                    log.warn("Redis聊天消息缺少必要字段: {}", message);
                    return;
                }
                
                // 构建基础主题
                String baseTopic = String.format("/topic/chat/%s/%s", 
                    chatMessage.getServerName(), 
                    chatMessage.getChannel());

                log.info("向WebSocket推送聊天消息 topic={} world={} player={}", 
                        baseTopic, chatMessage.getWorld(), 
                        chatMessage.getPlayer() != null ? chatMessage.getPlayer().getUsername() : "unknown");
                
                // 推送到基础主题
                messagingTemplate.convertAndSend(baseTopic, chatMessage);
                
                // 如果有世界信息，也推送到世界特定主题
                if (chatMessage.getWorld() != null && !chatMessage.getWorld().isEmpty()) {
                    String worldTopic = String.format("%s/%s", baseTopic, chatMessage.getWorld());
                    messagingTemplate.convertAndSend(worldTopic, chatMessage);
                }
                
                // 同时也推送到全局主题（兼容旧客户端）
                messagingTemplate.convertAndSend("/topic/chat", chatMessage);
                
            } catch (Exception e) {
                // 如果解析失败，回退到旧的行为
                log.error("解析Redis聊天消息失败，回退到原始推送", e);
                messagingTemplate.convertAndSend("/topic/chat", message);
            }
        }
    }
}
