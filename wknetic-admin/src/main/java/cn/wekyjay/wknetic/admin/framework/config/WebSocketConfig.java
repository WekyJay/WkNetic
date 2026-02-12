package cn.wekyjay.wknetic.admin.framework.config;


import cn.hutool.core.util.StrUtil;
import cn.wekyjay.wknetic.auth.security.JwtAuthenticationTokenFilter;
import cn.wekyjay.wknetic.common.config.JwtProperties;
import cn.wekyjay.wknetic.common.utils.JwtUtils;
import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Resource
    private JwtUtils jwtUtil;

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private SysUserMapper userMapper;

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // 开启一个简单的内存消息代理，客户端订阅以 /topic 开头的消息
        config.enableSimpleBroker("/topic");
        // 客户端发送消息的前缀 (如果有网页发消息的需求)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        // 在客户端入站通道添加拦截器，用于验证 JWT token
        registration.interceptors(
            // 先做 JWT 认证，保证后续拦截器能拿到 SecurityContext
            new ChannelInterceptor() {
                @Override
                public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                    
                    // 只在 STOMP CONNECT 命令时进行认证
                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        String authHeader = accessor.getFirstNativeHeader("Authorization");

                        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
                            try {
                                String token = authHeader.substring(7);
                                
                                if (jwtUtil.validateAndRenew(token)) {
                                    Long userId = jwtUtil.getUserId(token);

                                    SysUser user = userMapper.getUserWithRoleById(userId);
                                    
                                    List<SimpleGrantedAuthority> authorities = Collections.emptyList();
                                    if (user != null && user.getRole() != null && !user.getRole().isEmpty()) {
                                        authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
                                    }

                                    UsernamePasswordAuthenticationToken authentication = 
                                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                                    SecurityContextHolder.getContext().setAuthentication(authentication);
                                    accessor.setUser(authentication);
                                    // 确保用户也写入 header，避免后续解析不到
                                    accessor.setLeaveMutable(true);
                                }
                            } catch (Exception e) {
                                log.error("WebSocket JWT 认证异常", e);
                            }
                        }
                    }

                    return message;
                }
            },
            // 再由官方拦截器把 SecurityContext 传播到后续消息处理线程
            new SecurityContextChannelInterceptor()
        );
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
