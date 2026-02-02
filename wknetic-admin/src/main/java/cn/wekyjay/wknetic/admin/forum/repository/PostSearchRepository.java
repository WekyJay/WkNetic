package cn.wekyjay.wknetic.admin.forum.repository;

import cn.wekyjay.wknetic.admin.forum.document.PostDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子搜索 Repository
 * 
 * @author WkNetic
 * @since 2026-02-02
 */
@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, Long> {
    
    /**
     * 根据状态查询帖子
     */
    Page<PostDocument> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 根据话题ID查询帖子
     */
    Page<PostDocument> findByTopicId(Long topicId, Pageable pageable);
    
    /**
     * 根据标签查询帖子
     */
    Page<PostDocument> findByTagsContaining(String tag, Pageable pageable);
    
    /**
     * 根据作者ID查询帖子
     */
    Page<PostDocument> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 根据创建时间范围查询帖子
     */
    Page<PostDocument> findByCreateTimeBetween(
        LocalDateTime startTime, 
        LocalDateTime endTime, 
        Pageable pageable
    );
    
    /**
     * 查询置顶帖子
     */
    Page<PostDocument> findByIsPinnedTrue(Pageable pageable);
    
    /**
     * 查询热门帖子
     */
    Page<PostDocument> findByIsHotTrue(Pageable pageable);
}
