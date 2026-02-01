package cn.wekyjay.wknetic.common.model.vo;

import cn.wekyjay.wknetic.common.model.entity.ForumPost;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子列表VO
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
public class PostVO {
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 简介
     */
    private String excerpt;
    
    /**
     * 作者信息
     */
    private UserInfoVO author;
    
    /**
     * 话题信息
     */
    private TopicVO topic;
    
    /**
     * 标签列表
     */
    private List<TagVO> tags;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 是否置顶
     */
    private Boolean isPinned;
    
    /**
     * 是否热门
     */
    private Boolean isHot;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 浏览数
     */
    private Integer viewCount;
    
    /**
     * 收藏数
     */
    private Integer bookmarkCount;
    
    /**
     * 当前用户是否点赞
     */
    private Boolean isLiked;
    
    /**
     * 当前用户是否收藏
     */
    private Boolean isBookmarked;
    
    /**
     * 最后评论时间
     */
    private LocalDateTime lastCommentTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
