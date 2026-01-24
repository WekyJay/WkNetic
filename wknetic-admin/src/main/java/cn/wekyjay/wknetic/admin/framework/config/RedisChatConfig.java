package cn.wekyjay.wknetic.admin.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Configuration
public class RedisChatConfig {

    public static final String CHAT_TOPIC = "wknetic-global-chat";

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
    @Component
    public static class ChatRedisReceiver {
        
        @Resource
        private SimpMessagingTemplate messagingTemplate; // WebSocket 发送工具

        // 这个方法会被反射调用
        public void receiveMessage(String message) {
            // message 是 JSON 字符串
            // 直接推送到所有订阅了 /topic/chat 的 WebSocket 客户端
            messagingTemplate.convertAndSend("/topic/chat", message);
        }
    }
}