package cn.wekyjay.wknetic.socket.listener;

import cn.wekyjay.wknetic.common.dto.socket.AdminCommandPacket;
import cn.wekyjay.wknetic.common.enums.PacketType;
import cn.wekyjay.wknetic.socket.manager.ChannelManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 管理员命令监听器
 * 监听Redis中的管理员命令并转发给对应的游戏服务器
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Slf4j
@Component
public class AdminCommandListener implements MessageListener {

    @Resource
    private ChannelManager channelManager;

    @Resource
    private ObjectMapper objectMapper;

    public static final String ADMIN_COMMAND_TOPIC = "wknetic:admin:command";

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String msg = new String(message.getBody());
            log.info("收到管理员命令: {}", msg);

            // 解析命令
            AdminCommandPacket command = objectMapper.readValue(msg, AdminCommandPacket.class);
            
            // 获取目标服务器的Channel
            Channel channel = channelManager.getChannelByToken(command.getToken());
            if (channel == null || !channel.isActive()) {
                log.warn("目标服务器不在线或连接已断开: {}", command.getToken());
                return;
            }

            // 构造命令数据包
            ObjectNode packet = objectMapper.createObjectNode();
            packet.put("type", PacketType.ADMIN_COMMAND.getId());
            packet.put("commandType", command.getCommandType());
            packet.put("targetPlayer", command.getTargetPlayer());
            packet.put("command", command.getCommand());
            packet.put("reason", command.getReason());
            packet.put("commandId", command.getCommandId());

            // 发送命令到游戏服务器
            channel.writeAndFlush(packet.toString());
            
            log.info("已转发管理员命令到服务器: {} - 命令类型: {}", 
                     command.getToken(), command.getCommandType());
        } catch (Exception e) {
            log.error("处理管理员命令失败", e);
        }
    }
}
