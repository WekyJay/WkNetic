package cn.wekyjay.wknetic.community.event.user;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

import java.util.List;

/**
 * 用户被@提及事件
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class UserMentionedEvent extends BaseEvent {
    
    /**
     * 被提及的用户ID列表
     */
    private final List<Long> mentionedUserIds;
    
    /**
     * 内容类型：POST-帖子，COMMENT-评论
     */
    private final String contentType;
    
    /**
     * 内容ID（帖子ID或评论ID）
     */
    private final Long contentId;
    
    /**
     * 提及者ID
     */
    private final Long mentionerId;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param mentionedUserIds 被提及的用户ID列表
     * @param contentType 内容类型
     * @param contentId 内容ID
     * @param mentionerId 提及者ID
     */
    public UserMentionedEvent(Object source, List<Long> mentionedUserIds, String contentType,
                             Long contentId, Long mentionerId) {
        super(source, mentionerId, "USER_MENTIONED");
        this.mentionedUserIds = mentionedUserIds;
        this.contentType = contentType;
        this.contentId = contentId;
        this.mentionerId = mentionerId;
    }
    
    /**
     * 获取第一个被提及的用户ID（兼容方法）
     */
    public Long getMentionedUserId() {
        return mentionedUserIds != null && !mentionedUserIds.isEmpty() ? mentionedUserIds.get(0) : null;
    }
}
