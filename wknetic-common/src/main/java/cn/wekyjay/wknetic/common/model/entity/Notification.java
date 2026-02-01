package cn.wekyjay.wknetic.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统通知实体
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
@TableName("sys_notification")
public class Notification implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 通知ID
     */
    @TableId(value = "notification_id", type = IdType.AUTO)
    private Long notificationId;
    
    /**
     * 接收通知的用户ID
     */
    private Long userId;
    
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
     * 触发通知的用户ID
     */
    private Long senderId;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
    
    /**
     * 通知类型枚举
     */
    public enum Type {
        POST_REPLY("POST_REPLY", "帖子回复"),
        COMMENT_REPLY("COMMENT_REPLY", "评论回复"),
        POST_LIKE("POST_LIKE", "帖子点赞"),
        COMMENT_LIKE("COMMENT_LIKE", "评论点赞"),
        MENTION("MENTION", "@提及"),
        POST_APPROVED("POST_APPROVED", "帖子审核通过"),
        POST_REJECTED("POST_REJECTED", "帖子审核拒绝"),
        POST_AUDIT_PASS("POST_AUDIT_PASS", "帖子审核通过"),
        POST_AUDIT_REJECT("POST_AUDIT_REJECT", "帖子审核拒绝"),
        SYSTEM("SYSTEM", "系统通知");
        
        private final String code;
        private final String desc;
        
        Type(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
    }
}
