package cn.wekyjay.wknetic.admin.framework.config;


import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // 开启一个简单的内存消息代理，客户端订阅以 /topic 开头的消息
        config.enableSimpleBroker("/topic");
        // 客户端发送消息的前缀 (如果有网页发消息的需求)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // 注册一个 WebSocket 端点，前端连接这个 URL
        // setAllowedOriginPatterns("*") 解决跨域问题
        registry.addEndpoint("/ws-connect")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // 开启 SockJS 支持 (兼容性更好)
    }
}