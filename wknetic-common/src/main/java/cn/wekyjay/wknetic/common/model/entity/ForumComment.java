package cn.wekyjay.wknetic.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 论坛评论实体
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
@TableName("forum_comment")
public class ForumComment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 评论ID
     */
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;
    
    /**
     * 所属帖子ID
     */
    private Long postId;
    
    /**
     * 评论用户ID
     */
    private Long userId;
    
    /**
     * 父评论ID（NULL表示顶级评论）
     */
    private Long parentId;
    
    /**
     * 回复的目标用户ID
     */
    private Long replyToUserId;
    
    /**
     * Markdown格式评论内容
     */
    private String content;
    
    /**
     * 缓存的HTML内容
     */
    private String contentHtml;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 状态：1-正常 2-已删除 3-已隐藏
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
