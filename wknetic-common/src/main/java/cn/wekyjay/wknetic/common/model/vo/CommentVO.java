package cn.wekyjay.wknetic.common.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
public class CommentVO {
    
    /**
     * 评论ID
     */
    private Long commentId;
    
    /**
     * 评论者信息
     */
    private UserInfoVO author;
    
    /**
     * Markdown内容
     */
    private String content;
    
    /**
     * HTML内容（缓存）
     */
    private String contentHtml;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 当前用户是否点赞
     */
    private Boolean isLiked;
    
    /**
     * 父评论ID
     */
    private Long parentId;
    
    /**
     * 回复的目标用户
     */
    private UserInfoVO replyToUser;
    
    /**
     * 子回复列表
     */
    private List<CommentVO> replies;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
