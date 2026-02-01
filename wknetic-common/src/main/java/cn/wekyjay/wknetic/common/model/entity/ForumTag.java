package cn.wekyjay.wknetic.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 论坛标签实体
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
@TableName("forum_tag")
public class ForumTag implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 标签ID
     */
    @TableId(value = "tag_id", type = IdType.AUTO)
    private Long tagId;
    
    /**
     * 标签名称
     */
    private String tagName;
    
    /**
     * 使用次数
     */
    private Integer useCount;
    
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
}
