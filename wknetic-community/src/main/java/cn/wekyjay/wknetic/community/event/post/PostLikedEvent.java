package cn.wekyjay.wknetic.community.event.post;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

/**
 * 帖子被点赞事件
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class PostLikedEvent extends BaseEvent {
    
    /**
     * 帖子ID
     */
    private final Long postId;
    
    /**
     * 帖子作者ID
     */
    private final Long postAuthorId;
    
    /**
     * 点赞者ID（即userId）
     */
    private final Long likerId;
    
    /**
     * 是否为取消点赞（false=点赞，true=取消点赞）
     */
    private final boolean unliked;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param postId 帖子ID
     * @param postAuthorId 帖子作者ID
     * @param likerId 点赞者ID
     * @param unliked 是否取消点赞
     */
    public PostLikedEvent(Object source, Long postId, Long postAuthorId, Long likerId, boolean unliked) {
        super(source, likerId, unliked ? "POST_UNLIKED" : "POST_LIKED");
        this.postId = postId;
        this.postAuthorId = postAuthorId;
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
