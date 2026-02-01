package cn.wekyjay.wknetic.community.event.game;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

/**
 * 成就解锁事件（预留，用于未来游戏系统集成）
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class AchievementUnlockedEvent extends BaseEvent {
    
    /**
     * 用户ID
     */
    private final Long userId;
    
    /**
     * 成就ID
     */
    private final String achievementId;
    
    /**
     * 成就名称
     */
    private final String achievementName;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param userId 用户ID
     * @param achievementId 成就ID
     * @param achievementName 成就名称
     */
    public AchievementUnlockedEvent(Object source, Long userId, String achievementId, String achievementName) {
        super(source, userId, "ACHIEVEMENT_UNLOCKED");
        this.userId = userId;
        this.achievementId = achievementId;
        this.achievementName = achievementName;
    }
}
