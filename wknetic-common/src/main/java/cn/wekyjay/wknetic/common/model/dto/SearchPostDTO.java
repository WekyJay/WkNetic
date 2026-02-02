package cn.wekyjay.wknetic.common.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索帖子DTO
 * 
 * @author WkNetic
 * @since 2026-02-02
 */
@Data
public class SearchPostDTO {
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 话题ID（过滤条件）
     */
    private Long topicId;
    
    /**
     * 标签列表（过滤条件，OR关系）
     */
    private List<String> tags;
    
    /**
     * 状态（过滤条件）：0-草稿 1-已发布 2-审核中 3-已拒绝 4-已删除
     */
    private Integer status;
    
    /**
     * 创建时间-开始（过滤条件）
     */
    private LocalDateTime startTime;
    
    /**
     * 创建时间-结束（过滤条件）
     */
    private LocalDateTime endTime;
    
    /**
     * 排序字段：_score（相关性）, createTime, likeCount, commentCount, viewCount
     */
    private String sortBy = "_score";
    
    /**
     * 排序方向：asc, desc
     */
    private String sortOrder = "desc";
    
    /**
     * 页码（从0开始）
     */
    private Integer page = 0;
    
    /**
     * 每页大小
     */
    private Integer size = 20;
}
