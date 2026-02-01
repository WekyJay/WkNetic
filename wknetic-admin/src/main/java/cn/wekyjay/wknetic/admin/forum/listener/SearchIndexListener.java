package cn.wekyjay.wknetic.admin.forum.listener;

import cn.wekyjay.wknetic.community.event.post.*;
import cn.wekyjay.wknetic.community.event.comment.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 搜索索引监听器
 * 监听内容变更事件并同步到Elasticsearch
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexListener {
    
    // TODO: 注入ElasticsearchService
    // private final ElasticsearchService elasticsearchService;
    
    /**
     * 监听帖子创建事件
     * 创建搜索索引
     */
    @Async
    @EventListener
    public void onPostCreated(PostCreatedEvent event) {
        log.info("帖子创建，同步到搜索引擎: postId={}", event.getPostId());
        
        // TODO: 实现Elasticsearch索引同步
        // if (event.isNeedsReview()) {
        //     // 如果需要审核，等审核通过后再索引
        //     return;
        // }
        // elasticsearchService.indexPost(event.getPostId());
    }
    
    /**
     * 监听帖子更新事件
     * 更新搜索索引
     */
    @Async
    @EventListener
    public void onPostUpdated(PostUpdatedEvent event) {
        log.info("帖子更新，更新搜索索引: postId={}", event.getPostId());
        
        // TODO: 实现Elasticsearch索引更新
        // elasticsearchService.updatePost(event.getPostId());
    }
    
    /**
     * 监听帖子审核事件
     * 审核通过时创建索引，审核拒绝时删除索引
     */
    @Async
    @EventListener
    public void onPostAudited(PostAuditedEvent event) {
        log.info("帖子审核完成，更新搜索索引: postId={}, approved={}", 
                event.getPostId(), event.isApproved());
        
        // TODO: 实现Elasticsearch索引同步
        // if (event.isApproved()) {
        //     elasticsearchService.indexPost(event.getPostId());
        // } else {
        //     elasticsearchService.deletePost(event.getPostId());
        // }
    }
    
    /**
     * 监听评论创建事件
     * 评论也可以被搜索，创建评论索引
     */
    @Async
    @EventListener
    public void onCommentCreated(CommentCreatedEvent event) {
        log.debug("评论创建，同步到搜索引擎: commentId={}, postId={}", 
                event.getCommentId(), event.getPostId());
        
        // TODO: 实现评论索引
        // elasticsearchService.indexComment(event.getCommentId());
        
        // 也可以选择只索引帖子，评论作为帖子的一部分
        // elasticsearchService.updatePost(event.getPostId());
    }
}
