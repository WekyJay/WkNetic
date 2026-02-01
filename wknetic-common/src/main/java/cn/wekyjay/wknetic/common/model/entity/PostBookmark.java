package cn.wekyjay.wknetic.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子收藏实体
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
@TableName("forum_post_bookmark")
public class PostBookmark implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 收藏用户ID
     */
    private Long userId;
    
    /**
     * 所属分类ID
     */
    private Long categoryId;
    
    /**
     * 收藏时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
