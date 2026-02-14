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
import org.springframework.messaging.support.MessageHeaderAccessor;
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
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                // 1. 使用 MessageHeaderAccessor 获取可变的 Accessor
                // 这样对 accessor 的修改（如 setUser）会直接反映在 message 处理流中
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                // 防御性判断：如果是心跳等其他类型消息，accessor 可能为 null
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
                        try {
                            String token = authHeader.substring(7);
                            if (jwtUtil.validateAndRenew(token)) {
                                Long userId = jwtUtil.getUserId(token);
                                SysUser user = userMapper.getUserWithRoleById(userId);

                                List<SimpleGrantedAuthority> authorities = Collections.emptyList();
                                if (user != null && StrUtil.isNotBlank(user.getRole())) {
                                    authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
                                }

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(String.valueOf(userId), null, authorities);
                                
                                // 2. 【核心】将认证信息绑定到 WebSocket Session
                                // Spring 会在后续所有消息中自动携带这个 Principal
                                accessor.setUser(authentication);
                                
                                // 可选：如果是 JWT 场景，通常不需要这一步，除非你的某些 Filter 强依赖它
                                // SecurityContextHolder.getContext().setAuthentication(authentication);
                            }
                        } catch (Exception e) {
                            log.error("WebSocket JWT 认证异常", e);
                            // 可选：认证失败可以直接返回 null 来拒绝连接
                            // return null; 
                        }
                    }
                }
                return message;
            }
        });
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
