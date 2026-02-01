package cn.wekyjay.wknetic.community.event.comment;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

/**
 * 评论被点赞事件
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class CommentLikedEvent extends BaseEvent {
    
    /**
     * 评论ID
     */
    private final Long commentId;
    
    /**
     * 评论作者ID
     */
    private final Long commentAuthorId;
    
    /**
     * 点赞者ID
     */
    private final Long likerId;
    
    /**
     * 是否为取消点赞
     */
    private final boolean unliked;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param commentId 评论ID
     * @param commentAuthorId 评论作者ID
     * @param likerId 点赞者ID
     * @param unliked 是否取消点赞
     */
    public CommentLikedEvent(Object source, Long commentId, Long commentAuthorId, Long likerId, boolean unliked) {
        super(source, likerId, unliked ? "COMMENT_UNLIKED" : "COMMENT_LIKED");
        this.commentId = commentId;
        this.commentAuthorId = commentAuthorId;
        this.likerId = likerId;
        this.unliked = unliked;
    }
    
    /**
     * 是否为点赞（与unliked相反）
     */
    public boolean isLiked() {
        return !unliked;
    }
}
