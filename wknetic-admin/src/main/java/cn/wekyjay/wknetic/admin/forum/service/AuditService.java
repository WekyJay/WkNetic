package cn.wekyjay.wknetic.admin.forum.service;

import cn.wekyjay.wknetic.community.event.EventPublisher;
import cn.wekyjay.wknetic.community.event.post.PostAuditedEvent; 
import cn.wekyjay.wknetic.common.mapper.ForumPostMapper;
import cn.wekyjay.wknetic.common.mapper.NotificationMapper;
import cn.wekyjay.wknetic.common.model.entity.ForumPost;
import cn.wekyjay.wknetic.common.model.entity.Notification;
import cn.wekyjay.wknetic.auth.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 审核Service
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final ForumPostMapper postMapper;
    private final NotificationMapper notificationMapper;
    private final EventPublisher eventPublisher;
    
    /**
     * 获取待审核帖子列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 待审核帖子分页列表
     */
    public IPage<ForumPost> getPendingPosts(int page, int size) {
        if (!SecurityUtils.isModerator()) {
            throw new RuntimeException("只有审核员才能查看待审核帖子");
        }
        
        Page<ForumPost> pageParam = new Page<>(page, size);
        return postMapper.selectPage(pageParam, new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, ForumPost.Status.UNDER_REVIEW.getCode())
                .orderByAsc(ForumPost::getCreateTime));
    }
    
    /**
     * 审核通过帖子
     *
     * @param postId 帖子ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void approvePost(Long postId) {
        if (!SecurityUtils.isModerator()) {
            throw new RuntimeException("只有审核员才能审核帖子");
        }
        
        ForumPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        
        if (post.getStatus() != ForumPost.Status.UNDER_REVIEW.getCode()) {
            throw new RuntimeException("该帖子不在审核状态");
        }
        
        Long auditorId = SecurityUtils.getCurrentUserId();
        
        // 更新帖子状态为已发布
        LambdaUpdateWrapper<ForumPost> updateWrapper = new LambdaUpdateWrapper<ForumPost>()
                .eq(ForumPost::getPostId, postId)
                .set(ForumPost::getStatus, ForumPost.Status.PUBLISHED.getCode())
                .set(ForumPost::getAuditUserId, auditorId)
                .set(ForumPost::getAuditTime, LocalDateTime.now())
                .set(ForumPost::getUpdateTime, LocalDateTime.now());
        
        postMapper.update(null, updateWrapper);
        
        // 发送审核通过通知
        createAuditNotification(post.getUserId(), postId, "您的帖子已通过审核", Notification.Type.POST_AUDIT_PASS);
        
        // 发布审核通过事件
        eventPublisher.publishEvent(new PostAuditedEvent(this, postId, post.getUserId(), auditorId, true, null));
    }
    
    /**
     * 审核拒绝帖子
     *
     * @param postId 帖子ID
     * @param reason 拒绝原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectPost(Long postId, String reason) {
        if (!SecurityUtils.isModerator()) {
            throw new RuntimeException("只有审核员才能审核帖子");
        }
        
        ForumPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        
        if (post.getStatus() != ForumPost.Status.UNDER_REVIEW.getCode()) {
            throw new RuntimeException("该帖子不在审核状态");
        }
        
        Long auditorId = SecurityUtils.getCurrentUserId();
        
        // 更新帖子状态为已拒绝
        LambdaUpdateWrapper<ForumPost> updateWrapper = new LambdaUpdateWrapper<ForumPost>()
                .eq(ForumPost::getPostId, postId)
                .set(ForumPost::getStatus, ForumPost.Status.REJECTED.getCode())
                .set(ForumPost::getAuditUserId, auditorId)
                .set(ForumPost::getAuditTime, LocalDateTime.now())
                .set(ForumPost::getAuditRemark, reason)
                .set(ForumPost::getUpdateTime, LocalDateTime.now());
        
        postMapper.update(null, updateWrapper);
        
        // 发送审核拒绝通知
        String message = "您的帖子未通过审核，原因：" + reason;
        createAuditNotification(post.getUserId(), postId, message, Notification.Type.POST_AUDIT_REJECT);
        
        // 发布审核拒绝事件
        eventPublisher.publishEvent(new PostAuditedEvent(this, postId, post.getUserId(), auditorId, false, reason));
    }
    
    /**
     * 获取审核历史
     *
     * @param page 页码
     * @param size 每页数量
     * @param status 帖子状态（可选）
     * @return 审核历史分页列表
     */
    public IPage<ForumPost> getAuditHistory(int page, int size, Integer status) {
        if (!SecurityUtils.isModerator()) {
            throw new RuntimeException("只有审核员才能查看审核历史");
        }
        
        Page<ForumPost> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ForumPost> queryWrapper = new LambdaQueryWrapper<ForumPost>()
                .in(ForumPost::getStatus, ForumPost.Status.PUBLISHED.getCode(), ForumPost.Status.REJECTED.getCode())
                .isNotNull(ForumPost::getAuditUserId)
                .orderByDesc(ForumPost::getAuditTime);
        
        // 如果指定了状态，则过滤
        if (status != null) {
            queryWrapper.eq(ForumPost::getStatus, status);
        }
        
        return postMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 获取待审核帖子数量
     *
     * @return 待审核帖子数量
     */
    public Long getPendingPostCount() {
        if (!SecurityUtils.isModerator()) {
            return 0L;
        }
        
        return postMapper.selectCount(new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, ForumPost.Status.UNDER_REVIEW.getCode()));
    }
    
    /**
     * 创建审核通知
     */
    private void createAuditNotification(Long userId, Long postId, String message, Notification.Type type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type.getCode());
        notification.setTitle("帖子审核通知");
        notification.setContent(message);
        notification.setRelatedId(postId);
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());
        
        notificationMapper.insert(notification);
    }
}
