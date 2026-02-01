package cn.wekyjay.wknetic.community.event.report;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

/**
 * 内容被举报事件
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class ReportCreatedEvent extends BaseEvent {
    
    /**
     * 举报ID
     */
    private final Long reportId;
    
    /**
     * 举报对象类型：POST/COMMENT
     */
    private final String targetType;
    
    /**
     * 举报对象ID
     */
    private final Long targetId;
    
    /**
     * 举报人ID
     */
    private final Long reporterId;
    
    /**
     * 举报原因
     */
    private final String reason;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param reportId 举报ID
     * @param targetType 举报对象类型
     * @param targetId 举报对象ID
     * @param reporterId 举报人ID
     * @param reason 举报原因
     */
    public ReportCreatedEvent(Object source, Long reportId, String targetType, Long targetId,
                             Long reporterId, String reason) {
        super(source, reporterId, "REPORT_CREATED");
        this.reportId = reportId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reporterId = reporterId;
        this.reason = reason;
    }
}
