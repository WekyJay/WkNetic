package cn.wekyjay.wknetic.admin.system.controller;

import cn.wekyjay.wknetic.admin.system.service.GameChatService;
import cn.wekyjay.wknetic.auth.utils.SecurityUtils;
import cn.wekyjay.wknetic.common.model.dto.SendChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * 游戏聊天WebSocket控制器
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class GameChatWebSocketController {
    
    private final GameChatService gameChatService;
    
    /**
     * 处理前端发送的聊天消息
     * 订阅路径: /app/chat/{serverName}/send
     */
    @MessageMapping("/chat/{serverName}/send")
    public void sendMessage(@DestinationVariable String serverName,
                            @Payload SendChatMessageDTO dto,
                            Principal principal) {
        try {
            log.info("收到WebSocket聊天消息数据: {}", dto);
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null && principal != null) {
                // STOMP 会话里用户信息存在于 Principal，优先尝试获取
                Object p = principal instanceof UsernamePasswordAuthenticationToken
                        ? ((UsernamePasswordAuthenticationToken) principal).getPrincipal()
                        : principal.getName();
                if (p instanceof Long) {
                    userId = (Long) p;
                } else {
                    try {
                        userId = Long.valueOf(String.valueOf(p));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            if (userId == null) {
                log.warn("WebSocket聊天消息缺少用户身份，已丢弃。principal={}", principal);
                return;
            }

            dto.setServerName(serverName);
            
            boolean success = gameChatService.sendChatMessage(dto, userId);
            if (!success) {
                log.warn("用户 {} 发送消息失败", userId);
            }
        } catch (Exception e) {
            log.error("处理WebSocket聊天消息失败", e);
        }
    }
}
