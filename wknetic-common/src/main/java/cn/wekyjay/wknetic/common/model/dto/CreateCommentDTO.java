package cn.wekyjay.wknetic.common.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建评论DTO
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
public class CreateCommentDTO {
    
    /**
     * 所属帖子ID
     */
    @NotNull(message = "帖子ID不能为空")
    private Long postId;
    
    /**
     * Markdown内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    /**
     * 父评论ID（null表示顶级评论）
     */
    private Long parentId;
    
    /**
     * 回复的目标用户ID
     */
    private Long replyToUserId;
}
