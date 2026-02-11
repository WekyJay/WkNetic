package cn.wekyjay.wknetic.admin.game.controller;

import cn.wekyjay.wknetic.admin.game.service.GameChatService;
import cn.wekyjay.wknetic.auth.utils.SecurityUtils;
import cn.wekyjay.wknetic.common.model.dto.SendChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

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
                           @Payload SendChatMessageDTO dto) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
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
