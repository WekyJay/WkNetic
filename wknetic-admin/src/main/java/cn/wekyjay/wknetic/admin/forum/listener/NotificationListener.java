package cn.wekyjay.wknetic.admin.forum.listener;

import cn.wekyjay.wknetic.admin.system.service.NotificationService;
import cn.wekyjay.wknetic.community.event.post.*;
import cn.wekyjay.wknetic.community.event.comment.*;
import cn.wekyjay.wknetic.community.event.report.*;
import cn.wekyjay.wknetic.community.event.user.UserFollowedEvent;
import cn.wekyjay.wknetic.community.event.user.UserMentionedEvent;
import cn.wekyjay.wknetic.common.mapper.ForumPostMapper;
import cn.wekyjay.wknetic.common.model.entity.ForumPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 通知事件监听器
 * 监听各类业务事件并发送相应通知
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {
    
    private final NotificationService notificationService;
    private final ForumPostMapper postMapper;
    
    /**
     * 监听帖子创建事件
     * 如果帖子需要审核，通知审核员
     */
    @Async
    @EventListener
    public void onPostCreated(PostCreatedEvent event) {
        log.info("收到帖子创建事件: postId={}, userId={}, needsReview={}", 
                event.getPostId(), event.getUserId(), event.isNeedsReview());
        
        // TODO: 如果需要审核，通知所有审核员
        if (event.isNeedsReview()) {
            log.info("帖子需要审核，应通知审核员");
            // 实现：查询所有MODERATOR和ADMIN角色的用户，发送通知
        }
    }
    
    /**
     * 监听帖子点赞事件
     * 通知帖子作者（如果不是自己点赞自己的帖子）
     */
    @Async
    @EventListener
    public void onPostLiked(PostLikedEvent event) {
        log.info("收到帖子点赞事件: postId={}, userId={}, liked={}", 
                event.getPostId(), event.getUserId(), event.isLiked());
        
        if (event.isLiked()) {
            // 只在点赞时通知，取消点赞不通知
            ForumPost post = postMapper.selectById(event.getPostId());
            if (post != null && !post.getUserId().equals(event.getUserId())) {
                notificationService.createNotification(
                    post.getUserId(),
                    "POST_LIKE",
                    "您的帖子收到了新的点赞",
                    "有用户赞了您的帖子《" + post.getTitle() + "》",
                    event.getPostId(),
                    "POST",
                    event.getUserId()
                );
            }
        }
    }
    
    /**
     * 监听帖子审核事件
     * 通知帖子作者审核结果
     */
    @Async
    @EventListener
    public void onPostAudited(PostAuditedEvent event) {
        log.info("收到帖子审核事件: postId={}, approved={}, reason={}", 
                event.getPostId(), event.isApproved(), event.getReason());
        
        String title = event.isApproved() ? "帖子审核通过" : "帖子审核未通过";
        String content;
        
        if (event.isApproved()) {
            content = "您的帖子已通过审核，现在可以被其他用户看到了";
        } else {
            content = "您的帖子未通过审核，原因：" + event.getReason();
        }
        
        notificationService.createNotification(
            event.getAuthorId(),
            event.isApproved() ? "POST_AUDIT_PASS" : "POST_AUDIT_REJECT",
            title,
            content,
            event.getPostId(),
            "POST",
            event.getAuditorId()
        );
    }
    
    /**
     * 监听评论创建事件
     * 通知帖子作者和被回复的用户
     */
    @Async
    @EventListener
    public void onCommentCreated(CommentCreatedEvent event) {
        log.info("收到评论创建事件: commentId={}, postId={}, userId={}, replyToUserId={}", 
                event.getCommentId(), event.getPostId(), event.getUserId(), event.getReplyToUserId());
        
        ForumPost post = postMapper.selectById(event.getPostId());
        if (post == null) {
            return;
        }
        
        // 通知帖子作者（如果不是评论者本人）
        if (!post.getUserId().equals(event.getUserId())) {
            notificationService.createNotification(
                post.getUserId(),
                "COMMENT",
                "您的帖子收到了新评论",
                "有用户评论了您的帖子《" + post.getTitle() + "》",
                event.getCommentId(),
                "COMMENT",
                event.getUserId()
            );
        }
        
        // 如果是回复评论，通知被回复的用户
        if (event.getReplyToUserId() != null && !event.getReplyToUserId().equals(event.getUserId())) {
            notificationService.createNotification(
                event.getReplyToUserId(),
                "REPLY",
                "您收到了新的回复",
                "有用户回复了您在《" + post.getTitle() + "》下的评论",
                event.getCommentId(),
                "COMMENT",
                event.getUserId()
            );
        }
    }
    
    /**
     * 监听评论点赞事件
     * 通知评论作者
     */
    @Async
    @EventListener
    public void onCommentLiked(CommentLikedEvent event) {
        log.info("收到评论点赞事件: commentId={}, userId={}, liked={}", 
                event.getCommentId(), event.getUserId(), event.isLiked());
        
        if (event.isLiked() && event.getCommentAuthorId() != null 
                && !event.getCommentAuthorId().equals(event.getUserId())) {
            notificationService.createNotification(
                event.getCommentAuthorId(),
                "COMMENT_LIKE",
                "您的评论收到了新的点赞",
                "有用户赞了您的评论",
                event.getCommentId(),
                "COMMENT",
                event.getUserId()
            );
        }
    }
    
    /**
     * 监听用户提及事件
     * 通知被@的用户
     */
    @Async
    @EventListener
    public void onUserMentioned(UserMentionedEvent event) {
        log.info("收到用户提及事件: mentionedUserId={}, mentionerId={}, contentType={}, contentId={}", 
                event.getMentionedUserId(), event.getMentionerId(), event.getContentType(), event.getContentId());
        
        String title = "有人@了您";
        String content = "有用户在" + ("POST".equals(event.getContentType()) ? "帖子" : "评论") + "中提到了您";
        
        notificationService.createNotification(
            event.getMentionedUserId(),
            "MENTION",
            title,
            content,
            event.getContentId(),
            event.getContentType(),
            event.getMentionerId()
        );
    }
    
    /**
     * 监听用户关注事件
     * 通知被关注的用户
     */
    @Async
    @EventListener
    public void onUserFollowed(UserFollowedEvent event) {
        log.info("收到用户关注事件: followedUserId={}, followerId={}", 
                event.getFollowedUserId(), event.getFollowerId());
        
        notificationService.createNotification(
            event.getFollowedUserId(),
            "FOLLOW",
            "您有新的关注者",
            "有用户关注了您",
            event.getFollowerId(),
            "USER",
            event.getFollowerId()
        );
    }
    
    /**
     * 监听举报创建事件
     * 通知审核员
     */
    @Async
    @EventListener
    public void onReportCreated(ReportCreatedEvent event) {
        log.info("收到举报创建事件: reportId={}, targetType={}, targetId={}, reason={}", 
                event.getReportId(), event.getTargetType(), event.getTargetId(), event.getReason());
        
        // TODO: 通知所有审核员
        log.info("收到新举报，应通知审核员处理");
    }
}
