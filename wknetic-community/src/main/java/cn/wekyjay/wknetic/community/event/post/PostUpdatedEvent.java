package cn.wekyjay.wknetic.community.event.post;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

/**
 * 帖子更新事件
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class PostUpdatedEvent extends BaseEvent {
    
    /**
     * 帖子ID
     */
    private final Long postId;
    
    /**
     * 编辑者ID
     */
    private final Long editorId;
    
    /**
     * 修改摘要
     */
    private final String changeSummary;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param postId 帖子ID
     * @param editorId 编辑者ID
     * @param changeSummary 修改摘要
     */
    public PostUpdatedEvent(Object source, Long postId, Long editorId, String changeSummary) {
        super(source, editorId, "POST_UPDATED");
        this.postId = postId;
        this.editorId = editorId;
        this.changeSummary = changeSummary;
    }
}
