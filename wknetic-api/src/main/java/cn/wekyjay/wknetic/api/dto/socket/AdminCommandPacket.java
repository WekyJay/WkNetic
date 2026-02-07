package cn.wekyjay.wknetic.api.dto.socket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 管理员命令数据包
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommandPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 会话ID（用于标识目标服务器连接）
     */
    private String sessionId;

    /**
     * 命令类型：KICK, BAN, COMMAND, MESSAGE
     */
    private String commandType;

    /**
     * 目标玩家（用于KICK、BAN等）
     */
    private String targetPlayer;

    /**
     * 执行的命令（用于COMMAND类型）
     */
    private String command;

    /**
     * 原因或消息内容
     */
    private String reason;

    /**
     * 命令ID（用于追踪响应）
     */
    private String commandId;
}
