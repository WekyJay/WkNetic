package cn.wekyjay.wknetic.community.event.post;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

/**
 * 帖子审核事件
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class PostAuditedEvent extends BaseEvent {
    
    /**
     * 帖子ID
     */
    private final Long postId;
    
    /**
     * 帖子作者ID
     */
    private final Long postAuthorId;
    
    /**
     * 审核人ID
     */
    private final Long auditorId;
    
    /**
     * 审核结果：true=通过，false=拒绝
     */
    private final boolean approved;
    
    /**
     * 审核备注（拒绝原因等）
     */
    private final String remark;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param postId 帖子ID
     * @param postAuthorId 帖子作者ID
     * @param auditorId 审核人ID
     * @param approved 是否通过
     * @param remark 审核备注
     */
    public PostAuditedEvent(Object source, Long postId, Long postAuthorId, Long auditorId, boolean approved, String remark) {
        super(source, auditorId, approved ? "POST_APPROVED" : "POST_REJECTED");
        this.postId = postId;
        this.postAuthorId = postAuthorId;
        this.auditorId = auditorId;
        this.approved = approved;
        this.remark = remark;
    }
    
    /**
     * 获取审核原因（与remark同义）
     */
    public String getReason() {
        return remark;
    }
    
    /**
     * 获取作者ID（与postAuthorId同义）
     */
    public Long getAuthorId() {
        return postAuthorId;
    }
}
