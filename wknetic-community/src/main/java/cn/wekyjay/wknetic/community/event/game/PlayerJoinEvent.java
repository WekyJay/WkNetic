package cn.wekyjay.wknetic.community.event.game;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

/**
 * 玩家加入游戏事件（预留，用于未来游戏系统集成）
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class PlayerJoinEvent extends BaseEvent {
    
    /**
     * 用户ID
     */
    private final Long userId;
    
    /**
     * Minecraft UUID
     */
    private final String minecraftUuid;
    
    /**
     * 服务器名称
     */
    private final String serverName;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param userId 用户ID
     * @param minecraftUuid Minecraft UUID
     * @param serverName 服务器名称
     */
    public PlayerJoinEvent(Object source, Long userId, String minecraftUuid, String serverName) {
        super(source, userId, "PLAYER_JOIN");
        this.userId = userId;
        this.minecraftUuid = minecraftUuid;
        this.serverName = serverName;
    }
}
