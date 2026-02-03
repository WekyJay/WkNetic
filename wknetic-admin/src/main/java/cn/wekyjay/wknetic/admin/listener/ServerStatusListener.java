package cn.wekyjay.wknetic.admin.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 服务器状态监听器
 * 监听Redis中的服务器状态更新并通过WebSocket推送到前端
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Slf4j
@Component
public class ServerStatusListener implements MessageListener {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String msg = new String(message.getBody());

            JsonNode statusData = objectMapper.readTree(msg);
            String sessionId = statusData.has("sessionId") ? statusData.get("sessionId").asText() : "";

            messagingTemplate.convertAndSend("/topic/server/monitor", msg);
            
            log.debug("推送服务器状态到前端 [sessionId: {}]", sessionId);
        } catch (Exception e) {
            log.error("处理服务器状态更新失败", e);
        }
    }
}
