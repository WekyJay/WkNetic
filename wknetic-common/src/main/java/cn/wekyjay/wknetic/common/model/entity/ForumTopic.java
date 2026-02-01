package cn.wekyjay.wknetic.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 论坛话题实体
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
@TableName("forum_topic")
public class ForumTopic implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 话题ID
     */
    @TableId(value = "topic_id", type = IdType.AUTO)
    private Long topicId;
    
    /**
     * 话题名称
     */
    private String topicName;
    
    /**
     * 话题描述
     */
    private String topicDesc;
    
    /**
     * 图标地址
     */
    private String icon;
    
    /**
     * 主题颜色
     */
    private String color;
    
    /**
     * 排序权重
     */
    private Integer sortOrder;
    
    /**
     * 帖子数量
     */
    private Integer postCount;
    
    /**
     * 状态
     */
    private Integer status;
    
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
