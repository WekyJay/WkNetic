package cn.wekyjay.wknetic.admin.forum.service;

import cn.wekyjay.wknetic.community.event.EventPublisher;
import cn.wekyjay.wknetic.community.event.report.ReportCreatedEvent;
import cn.wekyjay.wknetic.common.mapper.ForumPostMapper;
import cn.wekyjay.wknetic.common.mapper.ForumReportMapper;
import cn.wekyjay.wknetic.common.mapper.NotificationMapper;
import cn.wekyjay.wknetic.common.model.dto.CreateReportDTO;
import cn.wekyjay.wknetic.common.model.entity.ForumPost;
import cn.wekyjay.wknetic.common.model.entity.ForumReport;
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
 * 举报Service
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final ForumReportMapper reportMapper;
    private final ForumPostMapper postMapper;
    private final NotificationMapper notificationMapper;
    private final EventPublisher eventPublisher;
    
    /**
     * 创建举报
     *
     * @param dto 举报DTO
     * @return 举报ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createReport(CreateReportDTO dto) {
        Long reporterId = SecurityUtils.getCurrentUserId();
        if (reporterId == null) {
            throw new RuntimeException("请先登录");
        }
        
        // 检查目标是否存在
        if ("POST".equals(dto.getTargetType())) {
            ForumPost post = postMapper.selectById(dto.getTargetId());
            if (post == null) {
                throw new RuntimeException("帖子不存在");
            }
        }
        // TODO: 检查其他类型（评论等）
        
        // 检查是否已经举报过（同一用户对同一目标只能举报一次）
        Long count = reportMapper.selectCount(new LambdaQueryWrapper<ForumReport>()
                .eq(ForumReport::getReporterId, reporterId)
                .eq(ForumReport::getTargetType, dto.getTargetType())
                .eq(ForumReport::getTargetId, dto.getTargetId())
                .in(ForumReport::getStatus, ForumReport.Status.PENDING.getCode()));
        
        if (count > 0) {
            throw new RuntimeException("您已经举报过该内容，请等待处理");
        }
        
        ForumReport report = new ForumReport();
        report.setReporterId(reporterId);
        report.setTargetType(dto.getTargetType());
        report.setTargetId(dto.getTargetId());
        report.setReason(dto.getReason());
        report.setStatus(ForumReport.Status.PENDING.getCode());
        report.setCreateTime(LocalDateTime.now());
        
        reportMapper.insert(report);
        
        // 发布举报创建事件（通知审核员）
        eventPublisher.publishEvent(new ReportCreatedEvent(this, report.getReportId(), 
                dto.getTargetType(), dto.getTargetId(), reporterId, dto.getReason()));
        
        return report.getReportId();
    }
    
    /**
     * 获取待处理举报列表
     *
     * @param page 页码
     * @param size 每页数量
     * @param status 举报状态（可选）
     * @return 举报列表
     */
    public IPage<ForumReport> getReports(int page, int size, Integer status) {
        if (!SecurityUtils.isModerator()) {
            throw new RuntimeException("只有审核员才能查看举报列表");
        }
        
        Page<ForumReport> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ForumReport> queryWrapper = new LambdaQueryWrapper<ForumReport>()
                .orderByDesc(ForumReport::getCreateTime);
        
        if (status != null) {
            queryWrapper.eq(ForumReport::getStatus, status);
        }
        
        return reportMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 处理举报
     *
     * @param reportId 举报ID
     * @param action 处理动作（ACCEPT=接受，REJECT=拒绝）
     * @param handleNote 处理备注
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleReport(Long reportId, String action, String handleNote) {
        if (!SecurityUtils.isModerator()) {
            throw new RuntimeException("只有审核员才能处理举报");
        }
        
        ForumReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("举报不存在");
        }
        
        if (report.getStatus() != ForumReport.Status.PENDING.getCode()) {
            throw new RuntimeException("该举报已处理");
        }
        
        Long handlerId = SecurityUtils.getCurrentUserId();
        Integer newStatus;
        
        if ("ACCEPT".equals(action)) {
            newStatus = ForumReport.Status.HANDLED.getCode();
            
            // 如果举报被接受，需要对目标内容进行处理
            if ("POST".equals(report.getTargetType())) {
                // 删除帖子（设置为已删除状态）
                LambdaUpdateWrapper<ForumPost> updateWrapper = new LambdaUpdateWrapper<ForumPost>()
                        .eq(ForumPost::getPostId, report.getTargetId())
                        .set(ForumPost::getStatus, ForumPost.Status.DELETED.getCode())
                        .set(ForumPost::getUpdateTime, LocalDateTime.now());
                postMapper.update(null, updateWrapper);
                
                // 通知帖子作者
                ForumPost post = postMapper.selectById(report.getTargetId());
                if (post != null) {
                    createReportNotification(post.getUserId(), reportId, 
                            "您的帖子因违规被删除：" + report.getReason());
                }
            }
            // TODO: 处理其他类型的举报（评论等）
            
            // 通知举报者
            createReportNotification(report.getReporterId(), reportId, 
                    "您的举报已被处理，举报内容已被删除");
            
        } else if ("REJECT".equals(action)) {
            newStatus = ForumReport.Status.REJECTED.getCode();
            
            // 通知举报者
            createReportNotification(report.getReporterId(), reportId, 
                    "您的举报未被采纳：" + handleNote);
            
        } else {
            throw new RuntimeException("无效的处理动作");
        }
        
        // 更新举报状态
        LambdaUpdateWrapper<ForumReport> updateWrapper = new LambdaUpdateWrapper<ForumReport>()
                .eq(ForumReport::getReportId, reportId)
                .set(ForumReport::getStatus, newStatus)
                .set(ForumReport::getHandlerId, handlerId)
                .set(ForumReport::getHandleTime, LocalDateTime.now())
                .set(ForumReport::getHandleRemark, handleNote);
        
        reportMapper.update(null, updateWrapper);
    }
    
    /**
     * 标记举报为处理中
     *
     * @param reportId 举报ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsProcessing(Long reportId) {
        if (!SecurityUtils.isModerator()) {
            throw new RuntimeException("只有审核员才能处理举报");
        }
        
        ForumReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("举报不存在");
        }
        
        if (report.getStatus() != ForumReport.Status.PENDING.getCode()) {
            throw new RuntimeException("该举报状态不正确");
        }
        
        Long handlerId = SecurityUtils.getCurrentUserId();
        
        LambdaUpdateWrapper<ForumReport> updateWrapper = new LambdaUpdateWrapper<ForumReport>()
                .eq(ForumReport::getReportId, reportId)
                .set(ForumReport::getStatus, ForumReport.Status.PENDING.getCode())
                .set(ForumReport::getHandlerId, handlerId);
        
        reportMapper.update(null, updateWrapper);
    }
    
    /**
     * 获取待处理举报数量
     *
     * @return 待处理举报数量
     */
    public Long getPendingReportCount() {
        if (!SecurityUtils.isModerator()) {
            return 0L;
        }
        
        return reportMapper.selectCount(new LambdaQueryWrapper<ForumReport>()
                .eq(ForumReport::getStatus, ForumReport.Status.PENDING.getCode()));
    }
    
    /**
     * 创建举报通知
     */
    private void createReportNotification(Long userId, Long reportId, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(Notification.Type.SYSTEM.getCode());
        notification.setTitle("举报通知");
        notification.setContent(message);
        notification.setRelatedId(reportId);
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());
        
        notificationMapper.insert(notification);
    }
}
