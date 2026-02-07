package cn.wekyjay.wknetic.api.dto.socket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 游戏服务器登录数据包
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerLoginPacket implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 服务器Token（用于认证）
     */
    private String token;

    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * 服务器版本
     */
    private String serverVersion;

    /**
     * 服务器IP地址
     */
    private String serverIp;
}
