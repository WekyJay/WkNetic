package cn.wekyjay.wknetic.admin.forum.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审核帖子VO
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
public class AuditPostVO {
    
    /**
     * 帖子ID
     */
    private Long id;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容
     */
    private String content;
    
    /**
     * 简介
     */
    private String excerpt;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 浏览数
     */
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 作者信息
     */
    private AuthorVO author;
    
    /**
     * 话题信息
     */
    private TopicInfoVO topic;
    
    /**
     * 标签列表
     */
    private List<TagInfoVO> tags;
    
    /**
     * 审核人名称
     */
    private String auditorName;
    
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    
    /**
     * 审核备注（拒绝原因）
     */
    private String auditRemark;
    
    /**
     * 作者VO
     */
    @Data
    public static class AuthorVO {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
    }
    
    /**
     * 话题VO
     */
    @Data
    public static class TopicInfoVO {
        private Long id;
        private String name;
    }
    
    /**
     * 标签VO
     */
    @Data
    public static class TagInfoVO {
        private Long id;
        private String name;
    }
}
