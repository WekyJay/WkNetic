package cn.wekyjay.wknetic.community.event.post;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

/**
 * 帖子创建事件
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class PostCreatedEvent extends BaseEvent {
    
    /**
     * 帖子ID
     */
    private final Long postId;
    
    /**
     * 帖子标题
     */
    private final String title;
    
    /**
     * 话题ID
     */
    private final Long topicId;
    
    /**
     * 是否需要审核
     */
    private final boolean needsReview;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param postId 帖子ID
     * @param userId 作者ID
     * @param title 帖子标题
     * @param topicId 话题ID
     */
    public PostCreatedEvent(Object source, Long postId, Long userId, String title, Long topicId) {
        super(source, userId, "POST_CREATED");
        this.postId = postId;
        this.title = title;
        this.topicId = topicId;
        this.needsReview = false; // 默认不需要审核
    }
    
    /**
     * 构造函数（带审核标志）
     *
     * @param source 事件源
     * @param postId 帖子ID
     * @param userId 作者ID
     * @param title 帖子标题
     * @param topicId 话题ID
     * @param needsReview 是否需要审核
     */
    public PostCreatedEvent(Object source, Long postId, Long userId, String title, Long topicId, boolean needsReview) {
        super(source, userId, "POST_CREATED");
        this.postId = postId;
        this.title = title;
        this.topicId = topicId;
        this.needsReview = needsReview;
    }
}
