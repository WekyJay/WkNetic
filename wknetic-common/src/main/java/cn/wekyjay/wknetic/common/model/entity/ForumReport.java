package cn.wekyjay.wknetic.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 内容举报实体
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
@TableName("forum_report")
public class ForumReport implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 举报ID
     */
    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;
    
    /**
     * 举报对象类型：POST/COMMENT
     */
    private String targetType;
    
    /**
     * 举报对象ID
     */
    private Long targetId;
    
    /**
     * 举报人ID
     */
    private Long reporterId;
    
    /**
     * 举报原因
     */
    private String reason;
    
    /**
     * 处理状态：0-待处理 1-已处理 2-已驳回
     */
    private Integer status;
    
    /**
     * 处理人ID
     */
    private Long handlerId;
    
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
    
    /**
     * 处理备注
     */
    private String handleRemark;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 举报状态枚举
     */
    public enum Status {
        PENDING(0, "待处理"),
        PROCESSING(0, "处理中"),
        HANDLED(1, "已处理"),
        ACCEPTED(1, "已接受"),
        REJECTED(2, "已驳回");
        
        private final int code;
        private final String desc;
        
        Status(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
    }
}
