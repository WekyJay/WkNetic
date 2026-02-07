package cn.wekyjay.wknetic.api.dto.socket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 服务器信息数据包
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerInfoPacket implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 服务器Token（仅用于兼容旧版本）
     */
    private String token;

    /**
     * 会话ID（用于标识服务器连接）
     */
    private String sessionId;

    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * 服务器MOTD（欢迎信息）
     */
    private String motd;

    /**
     * 在线玩家数
     */
    private Integer onlinePlayers;

    /**
     * 最大玩家数
     */
    private Integer maxPlayers;

    /**
     * TPS（每秒tick数）
     */
    private Double tps;

    /**
     * 内存使用量（MB）
     */
    private Long ramUsage;

    /**
     * 最大内存（MB）
     */
    private Long maxRam;

    /**
     * 在线玩家列表
     */
    private List<PlayerInfoDto> playerList;

    /**
     * 插件列表
     */
    private List<PluginInfoDto> pluginList;
}
