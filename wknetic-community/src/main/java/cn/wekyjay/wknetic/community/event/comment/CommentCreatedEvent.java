package cn.wekyjay.wknetic.community.event.comment;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

/**
 * 评论创建事件
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class CommentCreatedEvent extends BaseEvent {
    
    /**
     * 评论ID
     */
    private final Long commentId;
    
    /**
     * 所属帖子ID
     */
    private final Long postId;
    
    /**
     * 帖子作者ID
     */
    private final Long postAuthorId;
    
    /**
     * 评论者ID
     */
    private final Long commenterId;
    
    /**
     * 父评论ID（null表示顶级评论）
     */
    private final Long parentId;
    
    /**
     * 回复的目标用户ID（null表示回复帖子）
     */
    private final Long replyToUserId;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param commentId 评论ID
     * @param postId 帖子ID
     * @param postAuthorId 帖子作者ID
     * @param commenterId 评论者ID
     * @param parentId 父评论ID
     * @param replyToUserId 回复目标用户ID
     */
    public CommentCreatedEvent(Object source, Long commentId, Long postId, Long postAuthorId,
                               Long commenterId, Long parentId, Long replyToUserId) {
        super(source, commenterId, parentId == null ? "COMMENT_CREATED" : "COMMENT_REPLIED");
        this.commentId = commentId;
        this.postId = postId;
        this.postAuthorId = postAuthorId;
        this.commenterId = commenterId;
        this.parentId = parentId;
        this.replyToUserId = replyToUserId;
    }
}
