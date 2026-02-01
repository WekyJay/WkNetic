package cn.wekyjay.wknetic.community.event.post;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

/**
 * 帖子删除事件
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class PostDeletedEvent extends BaseEvent {
    
    /**
     * 帖子ID
     */
    private final Long postId;
    
    /**
     * 帖子作者ID
     */
    private final Long postAuthorId;
    
    /**
     * 删除原因（可选）
     */
    private final String reason;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param postId 帖子ID
     * @param postAuthorId 帖子作者ID
     * @param deleterId 删除者ID
     * @param reason 删除原因
     */
    public PostDeletedEvent(Object source, Long postId, Long postAuthorId, Long deleterId, String reason) {
        super(source, deleterId, "POST_DELETED");
        this.postId = postId;
        this.postAuthorId = postAuthorId;
        this.reason = reason;
    }
}
