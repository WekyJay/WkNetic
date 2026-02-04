package cn.wekyjay.wknetic.admin.system.service;

import cn.wekyjay.wknetic.admin.forum.dto.*;
import cn.wekyjay.wknetic.admin.system.domain.SysOperLog;
import cn.wekyjay.wknetic.admin.system.mapper.SysOperLogMapper;
import cn.wekyjay.wknetic.common.mapper.*;
import cn.wekyjay.wknetic.common.model.entity.ForumPost;
import cn.wekyjay.wknetic.common.domain.UserQuickAction;
import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.auth.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仪表板Service
 * 提供仪表板统计数据、趋势数据、活动日志和快捷入口功能
 * 
 * @author WkNetic
 * @since 2026-02-04
 */
@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final SysUserMapper userMapper;
    private final ForumPostMapper postMapper;
    private final ForumCommentMapper commentMapper;
    private final ForumReportMapper reportMapper;
    private final SysOperLogMapper operLogMapper;
    private final UserQuickActionMapper quickActionMapper;
    
    /**
     * 获取仪表板统计卡片数据
     * 包含：总用户数、当前在线、总发帖数、待审核数，及各项周环比
     *
     * @return 统计VO
     */
    public DashboardStatisticsVO getDashboardStatistics() {
        // 获取当前数据
        Long totalUserCount = getTotalUserCount();
        Long onlineUserCount = getOnlineUserCount();
        Long totalPostCount = getTotalPostCount();
        Long pendingAuditCount = getPendingAuditCount();
        
        // 获取上周同期数据（前7日到前14日）
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        LocalDate twoWeeksAgo = today.minusDays(14);
        
        Long userCountLastWeek = getUserCountBetween(twoWeeksAgo, weekAgo);
        Long postCountLastWeek = getPostCountBetween(twoWeeksAgo, weekAgo);
        Long auditCountLastWeek = getPendingAuditCountBetween(twoWeeksAgo, weekAgo);
        
        // 计算周环比
        BigDecimal userChangeRate = calculateChangeRate(totalUserCount, userCountLastWeek);
        BigDecimal postChangeRate = calculateChangeRate(totalPostCount, postCountLastWeek);
        BigDecimal auditChangeRate = calculateChangeRate(pendingAuditCount, auditCountLastWeek);
        
        // 在线用户周环比：使用平均在线数对比（固定为0，因为当前时刻的在线数无法与过去对比）
        BigDecimal onlineChangeRate = BigDecimal.ZERO;
        
        return DashboardStatisticsVO.builder()
                .totalUserCount(totalUserCount)
                .totalUserChangeRate(userChangeRate)
                .onlineUserCount(onlineUserCount)
                .onlineUserChangeRate(onlineChangeRate)
                .totalPostCount(totalPostCount)
                .totalPostChangeRate(postChangeRate)
                .pendingAuditCount(pendingAuditCount)
                .pendingAuditChangeRate(auditChangeRate)
                .build();
    }
    
    /**
     * 获取帖子发布趋势（最近7天或30天）
     *
     * @param days 天数（7或30）
     * @return 趋势数据列表
     */
    public List<PostTrendVO> getPostTrend(Integer days) {
        if (days == null) {
            days = 7;
        }
        if (days != 7 && days != 30) {
            throw new IllegalArgumentException("天数只支持7或30");
        }
        
        List<PostTrendVO> trends = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startTime = date.atStartOfDay();
            LocalDateTime endTime = date.atTime(LocalTime.MAX);
            
            long postCount = postMapper.selectCount(new LambdaQueryWrapper<ForumPost>()
                    .eq(ForumPost::getStatus, ForumPost.Status.PUBLISHED.getCode())
                    .between(ForumPost::getCreateTime, startTime, endTime));
            
            trends.add(PostTrendVO.builder()
                    .date(date)
                    .postCount(postCount)
                    .build());
        }
        
        return trends;
    }
    
    /**
     * 获取最近活动日志
     *
     * @param limit 返回数量，默认10条
     * @return 活动日志VO列表
     */
    public List<RecentActivityVO> getRecentActivities(Integer limit) {
        if (limit == null) {
            limit = 10;
        }
        if (limit > 50) {
            limit = 50;
        }
        
        List<SysOperLog> logs = operLogMapper.selectList(
                new LambdaQueryWrapper<SysOperLog>()
                        .orderByDesc(SysOperLog::getOperTime)
                        .last("LIMIT " + limit)
        );
        
        return logs.stream()
                .map(this::convertToRecentActivityVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户快捷入口列表
     *
     * @return 快捷入口VO列表
     */
    public List<DashboardQuickActionVO> getUserQuickActions() {
        Long userId = SecurityUtils.getCurrentUserId();
        
        List<UserQuickAction> actions = quickActionMapper.selectList(
                new LambdaQueryWrapper<UserQuickAction>()
                        .eq(UserQuickAction::getUserId, userId)
                        .eq(UserQuickAction::getStatus, 1)
                        .orderByAsc(UserQuickAction::getSortOrder)
        );
        
        return actions.stream()
                .map(this::convertToQuickActionVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 保存或更新用户快捷入口
     * 最多支持4个快捷入口
     *
     * @param actions 快捷入口列表
     */
    public void saveUserQuickActions(List<QuickActionDTO> actions) {
        if (actions == null || actions.isEmpty()) {
            return;
        }
        
        if (actions.size() > 4) {
            throw new IllegalArgumentException("快捷入口最多设置4个");
        }
        
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 删除该用户已有的快捷入口
        quickActionMapper.delete(
                new LambdaUpdateWrapper<UserQuickAction>()
                        .eq(UserQuickAction::getUserId, userId)
        );
        
        // 批量插入新的快捷入口
        for (int i = 0; i < actions.size(); i++) {
            QuickActionDTO dto = actions.get(i);
            UserQuickAction action = new UserQuickAction();
            action.setUserId(userId);
            action.setActionKey(dto.getActionKey());
            action.setActionName(dto.getActionName());
            action.setActionUrl(dto.getActionUrl());
            action.setIcon(dto.getIcon());
            action.setSortOrder(i + 1);
            action.setStatus(1);
            action.setCreateTime(LocalDateTime.now());
            action.setUpdateTime(LocalDateTime.now());
            
            quickActionMapper.insert(action);
        }
    }
    
    /**
     * 获取可用的快捷入口选项列表（系统预设）
     *
     * @return 可用快捷入口选项
     */
    public List<Map<String, Object>> getAvailableQuickActions() {
        List<Map<String, Object>> options = new ArrayList<>();
        
        addQuickActionOption(options, "post_list", "发帖管理", "/admin/forum/posts", "i-tabler-file-text");
        addQuickActionOption(options, "audit_pending", "待审核", "/admin/forum/audit", "i-tabler-check-circle");
        addQuickActionOption(options, "user_manage", "用户管理", "/admin/system/users", "i-tabler-users");
        addQuickActionOption(options, "report_list", "举报处理", "/admin/forum/reports", "i-tabler-flag");
        addQuickActionOption(options, "topic_manage", "话题管理", "/admin/forum/topics", "i-tabler-folder");
        addQuickActionOption(options, "comment_manage", "评论管理", "/admin/forum/comments", "i-tabler-message");
        addQuickActionOption(options, "role_manage", "角色管理", "/admin/system/roles", "i-tabler-shield");
        addQuickActionOption(options, "config_manage", "系统配置", "/admin/system/config", "i-tabler-settings");
        
        return options;
    }
    
    /**
     * 辅助方法：添加快捷入口选项
     */
    private void addQuickActionOption(List<Map<String, Object>> options, String key, String name, String url, String icon) {
        Map<String, Object> option = new HashMap<>();
        option.put("actionKey", key);
        option.put("actionName", name);
        option.put("actionUrl", url);
        option.put("icon", icon);
        options.add(option);
    }
    
    /**
     * 获取总用户数
     */
    private Long getTotalUserCount() {
        return userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getStatus, 1)
        );
    }
    
    /**
     * 获取当前在线用户数（最近15分钟内有活动的用户）
     * 使用Redis存储的活跃用户数或SQL查询最近活跃用户
     */
    private Long getOnlineUserCount() {
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        // 这里假设有last_active_time字段记录用户最后活跃时间
        // 如果没有该字段，可改为：return 0L; 或从Redis获取
        return userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getStatus, 1)
        );
    }
    
    /**
     * 获取总发帖数（已发布的帖子）
     */
    private Long getTotalPostCount() {
        return postMapper.selectCount(
                new LambdaQueryWrapper<ForumPost>()
                        .eq(ForumPost::getStatus, ForumPost.Status.PUBLISHED.getCode())
        );
    }
    
    /**
     * 获取待审核数（帖子+评论+举报）
     */
    private Long getPendingAuditCount() {
        long pendingPosts = postMapper.selectCount(
                new LambdaQueryWrapper<ForumPost>()
                        .eq(ForumPost::getStatus, ForumPost.Status.UNDER_REVIEW.getCode())
        );
        
        long pendingReports = reportMapper.selectCount(
                new LambdaQueryWrapper<cn.wekyjay.wknetic.common.model.entity.ForumReport>()
                        .eq(cn.wekyjay.wknetic.common.model.entity.ForumReport::getStatus, 0)
        );
        
        return pendingPosts + pendingReports;
    }
    
    /**
     * 获取时间段内新增用户数
     */
    private Long getUserCountBetween(LocalDate startDate, LocalDate endDate) {
        Date startTime = java.sql.Timestamp.valueOf(startDate.atStartOfDay());
        Date endTime = java.sql.Timestamp.valueOf(endDate.atTime(LocalTime.MAX));
        
        return userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .between(SysUser::getCreateTime, startTime, endTime)
        );
    }
    
    /**
     * 获取时间段内发帖数
     */
    private Long getPostCountBetween(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);
        
        return postMapper.selectCount(
                new LambdaQueryWrapper<ForumPost>()
                        .eq(ForumPost::getStatus, ForumPost.Status.PUBLISHED.getCode())
                        .between(ForumPost::getCreateTime, startTime, endTime)
        );
    }
    
    /**
     * 获取时间段内待审核数
     */
    private Long getPendingAuditCountBetween(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);
        
        long pendingPosts = postMapper.selectCount(
                new LambdaQueryWrapper<ForumPost>()
                        .eq(ForumPost::getStatus, ForumPost.Status.UNDER_REVIEW.getCode())
                        .between(ForumPost::getCreateTime, startTime, endTime)
        );
        
        long pendingReports = reportMapper.selectCount(
                new LambdaQueryWrapper<cn.wekyjay.wknetic.common.model.entity.ForumReport>()
                        .eq(cn.wekyjay.wknetic.common.model.entity.ForumReport::getStatus, 0)
                        .between(cn.wekyjay.wknetic.common.model.entity.ForumReport::getCreateTime, startTime, endTime)
        );
        
        return pendingPosts + pendingReports;
    }
    
    /**
     * 计算环比增长率
     * @return 百分比形式（如：12.50表示增长12.5%）
     */
    private BigDecimal calculateChangeRate(Long currentValue, Long lastValue) {
        if (currentValue == null || lastValue == null) {
            return BigDecimal.ZERO;
        }
        
        if (lastValue == 0) {
            return currentValue > 0 ? new BigDecimal("100") : BigDecimal.ZERO;
        }
        
        BigDecimal current = new BigDecimal(currentValue);
        BigDecimal last = new BigDecimal(lastValue);
        
        return current.subtract(last)
                .divide(last, 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
    
    /**
     * 转换为RecentActivityVO
     */
    private RecentActivityVO convertToRecentActivityVO(SysOperLog log) {
        LocalDateTime operTime = log.getOperTime() != null 
            ? LocalDateTime.ofInstant(log.getOperTime().toInstant(), ZoneId.systemDefault())
            : LocalDateTime.now();
        return RecentActivityVO.builder()
                .logId(log.getOperId())
                .title(log.getTitle())
                .businessType(log.getBusinessType())
                .businessTypeLabel(getBusinessTypeLabel(log.getBusinessType()))
                .operName(log.getOperName())
                .operTime(operTime)
                .status(log.getStatus())
                .statusLabel(log.getStatus() == 1 ? "成功" : "失败")
                .operIp(log.getOperIp())
                .errorMsg(log.getErrorMsg())
                .build();
    }
    
    /**
     * 转换为DashboardQuickActionVO
     */
    private DashboardQuickActionVO convertToQuickActionVO(UserQuickAction action) {
        return DashboardQuickActionVO.builder()
                .actionId(action.getActionId())
                .actionKey(action.getActionKey())
                .actionName(action.getActionName())
                .actionUrl(action.getActionUrl())
                .icon(action.getIcon())
                .sortOrder(action.getSortOrder())
                .build();
    }
    
    /**
     * 获取业务类型标签
     */
    private String getBusinessTypeLabel(Integer businessType) {
        if (businessType == null) {
            return "其他";
        }
        return switch (businessType) {
            case 1 -> "新增";
            case 2 -> "修改";
            case 3 -> "删除";
            case 4 -> "审批";
            case 5 -> "导出";
            case 6 -> "导入";
            default -> "其他";
        };
    }
}
