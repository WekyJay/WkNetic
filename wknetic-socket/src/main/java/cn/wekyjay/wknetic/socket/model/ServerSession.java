package cn.wekyjay.wknetic.socket.model;

import cn.wekyjay.wknetic.common.dto.socket.PlayerInfoDto;
import cn.wekyjay.wknetic.common.dto.socket.PluginInfoDto;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 服务器会话信息
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerSession {
    
    /**
     * Token
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
    
    /**
     * Netty Channel
     */
    private transient Channel channel;
}
