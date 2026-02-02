package cn.wekyjay.wknetic.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 帖子搜索结果VO
 * 
 * @author WkNetic
 * @since 2026-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchVO {
    
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
     * 作者ID
     */
    private Long userId;
    
    /**
     * 作者用户名
     */
    private String username;
    
    /**
     * 话题ID
     */
    private Long topicId;
    
    /**
     * 话题名称
     */
    private String topicName;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
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
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 最后评论时间
     */
    private LocalDateTime lastCommentTime;
    
    /**
     * 搜索相关性得分
     */
    private Double score;
    
    /**
     * 高亮字段
     * key: 字段名（title, excerpt, content）
     * value: 高亮片段列表
     */
    private Map<String, List<String>> highlights;
}
