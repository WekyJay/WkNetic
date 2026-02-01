package cn.wekyjay.wknetic.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子点赞实体
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
@TableName("forum_post_like")
public class PostLike implements Serializable {
    
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
     * 点赞用户ID
     */
    private Long userId;
    
    /**
     * 点赞时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
