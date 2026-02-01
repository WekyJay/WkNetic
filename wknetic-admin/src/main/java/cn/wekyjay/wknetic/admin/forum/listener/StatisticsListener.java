package cn.wekyjay.wknetic.admin.forum.listener;

import cn.wekyjay.wknetic.admin.forum.service.TopicService;
import cn.wekyjay.wknetic.community.event.post.*;
import cn.wekyjay.wknetic.community.event.comment.*;
import cn.wekyjay.wknetic.community.event.report.*;
import cn.wekyjay.wknetic.common.mapper.ForumPostMapper;
import cn.wekyjay.wknetic.common.model.entity.ForumPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 统计数据监听器
 * 监听业务事件并更新相关统计数据
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsListener {
    
    private final TopicService topicService;
    private final ForumPostMapper postMapper;
    
    /**
     * 监听帖子审核事件
     * 审核通过时增加板块帖子数
     */
    @Async
    @EventListener
    public void onPostAudited(PostAuditedEvent event) {
        if (event.isApproved()) {
            log.info("帖子审核通过，更新板块统计: postId={}", event.getPostId());
            
            ForumPost post = postMapper.selectById(event.getPostId());
            if (post != null && post.getTopicId() != null) {
                topicService.incrementPostCount(post.getTopicId());
            }
        }
    }
    
    /**
     * 监听帖子删除事件（通过PostUpdatedEvent判断状态）
     * 删除帖子时减少板块帖子数
     */
    @Async
    @EventListener
    public void onPostUpdated(PostUpdatedEvent event) {
        log.debug("帖子更新事件: postId={}", event.getPostId());
        
        ForumPost post = postMapper.selectById(event.getPostId());
        if (post != null && post.getStatus() == ForumPost.Status.DELETED.getCode()) {
            if (post.getTopicId() != null) {
                log.info("帖子被删除，减少板块帖子数: postId={}, topicId={}", 
                        event.getPostId(), post.getTopicId());
                topicService.decrementPostCount(post.getTopicId());
            }
        }
    }
    
    /**
     * 监听评论创建事件
     * 增加帖子评论数（在CommentService中已处理，这里仅记录日志）
     */
    @Async
    @EventListener
    public void onCommentCreated(CommentCreatedEvent event) {
        log.debug("评论创建，帖子评论数已在Service中更新: postId={}, commentId={}", 
                event.getPostId(), event.getCommentId());
    }
    
    /**
     * 监听帖子点赞事件
     * 点赞数在Service中已处理，这里可以记录用户行为用于推荐算法
     */
    @Async
    @EventListener
    public void onPostLiked(PostLikedEvent event) {
        log.debug("帖子点赞事件: postId={}, userId={}, liked={}", 
                event.getPostId(), event.getUserId(), event.isLiked());
        
        // TODO: 记录用户行为数据用于推荐系统
        // 例如：记录到Redis或单独的行为表，用于计算热度、推荐等
    }
    
    /**
     * 监听评论点赞事件
     * 记录用户行为
     */
    @Async
    @EventListener
    public void onCommentLiked(CommentLikedEvent event) {
        log.debug("评论点赞事件: commentId={}, userId={}, liked={}", 
                event.getCommentId(), event.getUserId(), event.isLiked());
        
        // TODO: 记录用户行为数据
    }
    
    /**
     * 监听举报创建事件
     * 统计举报数据，可用于自动识别问题内容
     */
    @Async
    @EventListener
    public void onReportCreated(ReportCreatedEvent event) {
        log.info("举报创建，记录统计数据: targetType={}, targetId={}, reason={}", 
                event.getTargetType(), event.getTargetId(), event.getReason());
        
        // TODO: 统计举报次数，如果同一内容被多次举报，可以自动标记或隐藏
    }
}
