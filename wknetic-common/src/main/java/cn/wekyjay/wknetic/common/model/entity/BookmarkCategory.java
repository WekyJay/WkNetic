package cn.wekyjay.wknetic.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏分类实体
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
@TableName("forum_bookmark_category")
public class BookmarkCategory implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 分类ID
     */
    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 排序权重
     */
    private Integer sortOrder;
    
    /**
     * 是否默认分类
     */
    private Boolean isDefault;
    
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
