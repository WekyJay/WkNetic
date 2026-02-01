package cn.wekyjay.wknetic.community.event.user;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

/**
 * 用户关注事件
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class UserFollowedEvent extends BaseEvent {
    
    /**
     * 关注者ID
     */
    private final Long followerId;
    
    /**
     * 被关注者ID
     */
    private final Long followedId;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     */
    public UserFollowedEvent(Object source, Long followerId, Long followedId) {
        super(source, followerId, "USER_FOLLOWED");
        this.followerId = followerId;
        this.followedId = followedId;
    }
    
    /**
     * 获取被关注者ID（别名方法）
     */
    public Long getFollowedUserId() {
        return followedId;
    }
}
