package cn.wekyjay.wknetic.api.model.packet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import cn.wekyjay.wknetic.api.enums.PacketType;
import cn.wekyjay.wknetic.api.model.dto.socket.PlayerInfoDto;
import cn.wekyjay.wknetic.api.model.dto.socket.PluginInfoDto;

/**
 * 服务器会话信息（对外 API 模型）
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ServerSessionPacket extends ServerPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    

    /**
     * 会话ID（用于日志和查询，避免暴露token）
     */
    private String sessionId = UUID.randomUUID().toString();
    
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
     * 服务器端口
     */
    private Integer port;
    
    /**
     * 登录时间
     */
    private Date loginTime;
    
    /**
     * 最后活跃时间
     */
    private Date lastActiveTime;
    
    /**
     * 获取服务器版本（从父类继承serverVersion的别名方法）
     */
    public String getVersion() {
        return getServerVersion();
    }
    
    @Override
    protected PacketType defineType() {
        return PacketType.SERVER_INFO;
    }
}