package cn.wekyjay.wknetic.common.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知VO
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
public class NotificationVO {
    
    /**
     * 通知ID
     */
    private Long notificationId;
    
    /**
     * 通知类型
     */
    private String type;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 关联对象ID
     */
    private Long relatedId;
    
    /**
     * 关联对象类型
     */
    private String relatedType;
    
    /**
     * 发送者信息
     */
    private UserInfoVO sender;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
}
