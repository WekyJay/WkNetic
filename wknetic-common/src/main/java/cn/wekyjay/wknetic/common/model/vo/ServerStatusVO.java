package cn.wekyjay.wknetic.common.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 服务器状态视图对象
 */
@Data
public class ServerStatusVO implements Serializable {
    
    /**
     * 服务器会话ID
     */
    private String sessionId;
    
    /**
     * 服务器名称
     */
    private String serverName;
    
    /**
     * 服务器IP地址
     */
    private String ip;
    
    /**
     * 服务器端口
     */
    private Integer port;
    
    /**
     * 在线玩家数量
     */
    private Integer onlinePlayers;
    
    /**
     * 最大玩家数量
     */
    private Integer maxPlayers;
    
    /**
     * 服务器版本
     */
    private String version;
    
    /**
     * 服务器状态：在线/离线/维护中
     */
    private String status;
    
    /**
     * 服务器TPS（每秒处理游戏刻数）
     */
    private Double tps;
    
    /**
     * 最后心跳时间
     */
    private Date lastHeartbeat;
    
    /**
     * 服务器所在地区
     */
    private String region;
    
    /**
     * 服务器描述
     */
    private String description;
    
    /**
     * 是否为默认服务器
     */
    private Boolean defaultServer;
    
    /**
     * 服务器类型：主服务器/测试服务器/创造服务器等
     */
    private String serverType;
    
    /**
     * 服务器启动时间
     */
    private Date startupTime;
    
    /**
     * 是否支持插件
     */
    private Boolean pluginsEnabled;
    
    /**
     * 服务器内存使用情况（MB）
     */
    private Long memoryUsage;
    
    /**
     * 服务器总内存（MB）
     */
    private Long totalMemory;
    
    /**
     * 世界数量
     */
    private Integer worldCount;
    
    /**
     * 插件数量
     */
    private Integer pluginCount;
    
    /**
     * 服务器配置的标签
     */
    private String tags;
    
    public ServerStatusVO() {
        this.onlinePlayers = 0;
        this.maxPlayers = 50;
        this.tps = 20.0;
        this.status = "离线";
        this.defaultServer = false;
        this.pluginsEnabled = true;
        this.worldCount = 1;
        this.pluginCount = 0;
    }
    
    /**
     * 计算在线状态百分比
     */
    public Double getOnlinePercentage() {
        if (maxPlayers == null || maxPlayers == 0) {
            return 0.0;
        }
        if (onlinePlayers == null) {
            return 0.0;
        }
        return (onlinePlayers * 100.0) / maxPlayers;
    }
    
    /**
     * 检查服务器是否在线
     */
    public boolean isOnline() {
        return "在线".equals(status);
    }
    
    /**
     * 检查服务器是否拥挤（超过80%玩家）
     */
    public boolean isCrowded() {
        return getOnlinePercentage() >= 80.0;
    }
    
    /**
     * 检查服务器是否空闲（低于20%玩家）
     */
    public boolean isIdle() {
        return getOnlinePercentage() <= 20.0;
    }
    
    /**
     * 获取内存使用百分比
     */
    public Double getMemoryUsagePercentage() {
        if (totalMemory == null || totalMemory == 0 || memoryUsage == null) {
            return 0.0;
        }
        return (memoryUsage * 100.0) / totalMemory;
    }
}