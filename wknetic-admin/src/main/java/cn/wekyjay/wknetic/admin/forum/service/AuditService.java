package cn.wekyjay.wknetic.admin.forum.service;

import cn.wekyjay.wknetic.admin.forum.dto.AuditPostVO;
import cn.wekyjay.wknetic.admin.forum.dto.AuditStatisticsVO;
import cn.wekyjay.wknetic.community.event.EventPublisher;
import cn.wekyjay.wknetic.community.event.post.PostAuditedEvent; 
import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.mapper.*;
import cn.wekyjay.wknetic.common.model.entity.*;
import cn.wekyjay.wknetic.auth.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final SysUserMapper userMapper;
    private final ForumTopicMapper topicMapper;
    private final ForumTagMapper tagMapper;
    private final PostTagMapper postTagMapper;
    
    /**
     * 获取待审核帖子列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 待审核帖子分页列表
     */
    public IPage<AuditPostVO> getPendingPosts(int page, int size) {
        if (!SecurityUtils.isModerator()) {
            throw new RuntimeException("只有审核员才能查看待审核帖子");
        }
        
        Page<ForumPost> pageParam = new Page<>(page, size);
        IPage<ForumPost> postPage = postMapper.selectPage(pageParam, new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, ForumPost.Status.UNDER_REVIEW.getCode())
                .orderByAsc(ForumPost::getCreateTime));
        
        return convertToAuditPostVOPage(postPage);
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
    public IPage<AuditPostVO> getAuditHistory(int page, int size, Integer status) {
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
        
        IPage<ForumPost> postPage = postMapper.selectPage(pageParam, queryWrapper);
        return convertToAuditPostVOPage(postPage);
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
     * 获取审核统计信息
     *
     * @return 审核统计数据
     */
    public AuditStatisticsVO getAuditStatistics() {
        if (!SecurityUtils.isModerator()) {
            throw new RuntimeException("只有审核员才能查看审核统计");
        }
        
        // 待审核数量
        Long pendingCount = postMapper.selectCount(new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, ForumPost.Status.UNDER_REVIEW.getCode()));
        
        // 今日审核统计
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        Long todayApprovedCount = postMapper.selectCount(new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, ForumPost.Status.PUBLISHED.getCode())
                .between(ForumPost::getAuditTime, todayStart, todayEnd));
        
        Long todayRejectedCount = postMapper.selectCount(new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, ForumPost.Status.REJECTED.getCode())
                .between(ForumPost::getAuditTime, todayStart, todayEnd));
        
        // 本周审核统计（基于今天往前7天）
        LocalDateTime weekStart = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
        LocalDateTime weekEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        Long weekApprovedCount = postMapper.selectCount(new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, ForumPost.Status.PUBLISHED.getCode())
                .between(ForumPost::getAuditTime, weekStart, weekEnd));
        
        Long weekRejectedCount = postMapper.selectCount(new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, ForumPost.Status.REJECTED.getCode())
                .between(ForumPost::getAuditTime, weekStart, weekEnd));
        
        // 总审核数
        Long totalApprovedCount = postMapper.selectCount(new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, ForumPost.Status.PUBLISHED.getCode())
                .isNotNull(ForumPost::getAuditUserId));
        
        Long totalRejectedCount = postMapper.selectCount(new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, ForumPost.Status.REJECTED.getCode()));
        
        Long totalAuditCount = totalApprovedCount + totalRejectedCount;
        
        // 计算通过率
        BigDecimal approvalRate = BigDecimal.ZERO;
        if (totalAuditCount > 0) {
            approvalRate = BigDecimal.valueOf(totalApprovedCount)
                    .divide(BigDecimal.valueOf(totalAuditCount), 2, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        // 计算平均审核时间（如果有数据）
        Long averageAuditTime = 0L;
        if (totalAuditCount > 0) {
            // 简化计算：这里可以通过SQL窗口函数更精确计算
            // TODO: 实现更精确的平均审核时间计算
        }
        
        return AuditStatisticsVO.builder()
                .pendingCount(pendingCount)
                .todayApprovedCount(todayApprovedCount)
                .todayRejectedCount(todayRejectedCount)
                .weekApprovedCount(weekApprovedCount)
                .weekRejectedCount(weekRejectedCount)
                .approvalRate(approvalRate)
                .averageAuditTime(averageAuditTime)
                .totalAuditCount(totalAuditCount)
                .build();
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
    
    /**
     * 将帖子分页转换为AuditPostVO分页
     */
    private IPage<AuditPostVO> convertToAuditPostVOPage(IPage<ForumPost> postPage) {
        List<ForumPost> posts = postPage.getRecords();
        if (posts.isEmpty()) {
            return postPage.convert(post -> new AuditPostVO());
        }
        
        // 收集所有需要的ID
        List<Long> userIds = posts.stream().map(ForumPost::getUserId).distinct().collect(Collectors.toList());
        List<Long> topicIds = posts.stream().map(ForumPost::getTopicId).distinct().collect(Collectors.toList());
        List<Long> postIds = posts.stream().map(ForumPost::getPostId).collect(Collectors.toList());
        List<Long> auditorIds = posts.stream()
                .map(ForumPost::getAuditUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getUserId, u -> u));
        
        // 批量查询审核人
        Map<Long, SysUser> auditorMap = auditorIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(auditorIds).stream()
                        .collect(Collectors.toMap(SysUser::getUserId, u -> u));
        
        // 批量查询话题
        Map<Long, ForumTopic> topicMap = topicMapper.selectBatchIds(topicIds).stream()
                .collect(Collectors.toMap(ForumTopic::getTopicId, t -> t));
        
        // 批量查询标签关联
        List<PostTag> postTags = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>()
                .in(PostTag::getPostId, postIds));
        List<Long> tagIds = postTags.stream().map(PostTag::getTagId).distinct().collect(Collectors.toList());
        Map<Long, ForumTag> tagMap = tagIds.isEmpty() ? Map.of() :
                tagMapper.selectBatchIds(tagIds).stream()
                        .collect(Collectors.toMap(ForumTag::getTagId, t -> t));
        
        // 按帖子分组标签
        Map<Long, List<ForumTag>> postTagsMap = postTags.stream()
                .collect(Collectors.groupingBy(
                        PostTag::getPostId,
                        Collectors.mapping(pt -> tagMap.get(pt.getTagId()), Collectors.toList())
                ));
        
        // 转换为VO
        return postPage.convert(post -> {
            AuditPostVO vo = new AuditPostVO();
            vo.setId(post.getPostId());
            vo.setTitle(post.getTitle());
            vo.setContent(post.getContent());
            vo.setExcerpt(post.getExcerpt());
            vo.setStatus(post.getStatus());
            vo.setViewCount(post.getViewCount());
            vo.setLikeCount(post.getLikeCount());
            vo.setCommentCount(post.getCommentCount());
            vo.setCreateTime(post.getCreateTime());
            vo.setAuditTime(post.getAuditTime());
            vo.setAuditRemark(post.getAuditRemark());
            
            // 设置作者信息
            SysUser author = userMap.get(post.getUserId());
            if (author != null) {
                AuditPostVO.AuthorVO authorVO = new AuditPostVO.AuthorVO();
                authorVO.setId(author.getUserId());
                authorVO.setUsername(author.getUsername());
                authorVO.setNickname(author.getNickname());
                authorVO.setAvatar(author.getAvatar());
                vo.setAuthor(authorVO);
            }
            
            // 设置审核人名称
            if (post.getAuditUserId() != null) {
                SysUser auditor = auditorMap.get(post.getAuditUserId());
                if (auditor != null) {
                    vo.setAuditorName(auditor.getNickname() != null ? auditor.getNickname() : auditor.getUsername());
                }
            }
            
            // 设置话题信息
            ForumTopic topic = topicMap.get(post.getTopicId());
            if (topic != null) {
                AuditPostVO.TopicInfoVO topicVO = new AuditPostVO.TopicInfoVO();
                topicVO.setId(topic.getTopicId());
                topicVO.setName(topic.getTopicName());
                vo.setTopic(topicVO);
            }
            
            // 设置标签列表
            List<ForumTag> tags = postTagsMap.getOrDefault(post.getPostId(), new ArrayList<>());
            vo.setTags(tags.stream()
                    .filter(tag -> tag != null)
                    .map(tag -> {
                        AuditPostVO.TagInfoVO tagVO = new AuditPostVO.TagInfoVO();
                        tagVO.setId(tag.getTagId());
                        tagVO.setName(tag.getTagName());
                        return tagVO;
                    })
                    .collect(Collectors.toList()));
            
            return vo;
        });
    }
}
