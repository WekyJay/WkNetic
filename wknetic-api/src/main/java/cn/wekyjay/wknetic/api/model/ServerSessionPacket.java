package cn.wekyjay.wknetic.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import cn.wekyjay.wknetic.api.dto.socket.PlayerInfoDto;
import cn.wekyjay.wknetic.api.dto.socket.PluginInfoDto;

/**
 * 服务器会话信息（对外 API 模型）
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerSessionPacket extends BasePacket {
    
    /**
     * 会话ID（用于日志和查询，避免暴露token）
     */
    @Builder.Default
    private String sessionId = UUID.randomUUID().toString();

    /**
     * 服务器Token
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
     * MOTD
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
     * TPS
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
     * 玩家列表
     */
    private List<PlayerInfoDto> playerList;
    
    /**
     * 插件列表
     */
    private List<PluginInfoDto> pluginList;
    
    /**
     * 登录IP
     */
    private String loginIp;
    
    /**
     * 登录时间
     */
    private Date loginTime;
    
    /**
     * 最后活跃时间
     */
    private Date lastActiveTime;
    
}
