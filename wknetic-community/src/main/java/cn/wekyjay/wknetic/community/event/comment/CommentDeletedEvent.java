package cn.wekyjay.wknetic.community.event.comment;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

/**
 * 评论删除事件
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class CommentDeletedEvent extends BaseEvent {
    
    /**
     * 评论ID
     */
    private final Long commentId;
    
    /**
     * 所属帖子ID
     */
    private final Long postId;
    
    /**
     * 评论作者ID
     */
    private final Long commentAuthorId;
    
    /**
     * 删除原因
     */
    private final String reason;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param commentId 评论ID
     * @param postId 帖子ID
     * @param commentAuthorId 评论作者ID
     * @param deleterId 删除者ID
     * @param reason 删除原因
     */
    public CommentDeletedEvent(Object source, Long commentId, Long postId, Long commentAuthorId,
                               Long deleterId, String reason) {
        super(source, deleterId, "COMMENT_DELETED");
        this.commentId = commentId;
        this.postId = postId;
        this.commentAuthorId = commentAuthorId;
        this.reason = reason;
    }
}
