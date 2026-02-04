package cn.wekyjay.wknetic.admin.system.controller;

import cn.wekyjay.wknetic.admin.forum.dto.*;
import cn.wekyjay.wknetic.admin.system.service.DashboardService;
import cn.wekyjay.wknetic.common.model.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 仪表板控制器
 * 提供仪表板统计数据、趋势、活动日志和快捷入口管理接口
 * 
 * @author WkNetic
 * @since 2026-02-04
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Validated
@Tag(name = "仪表板管理", description = "管理后台仪表板数据、统计和快捷入口")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * 获取仪表板统计卡片数据
     * 包含：总用户数、当前在线、总发帖数、待审核数，及各项周环比
     * 
     * @return 统计VO
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取仪表板统计数据", description = "返回四个统计卡片的数据和周环比")
    public Result<DashboardStatisticsVO> getDashboardStatistics() {
        DashboardStatisticsVO statistics = dashboardService.getDashboardStatistics();
        return Result.success(statistics);
    }
    
    /**
     * 获取帖子发布趋势
     * 用于绘制趋势图表
     * 
     * @param days 查询天数，支持7或30天，默认7天
     * @return 趋势数据列表
     */
    @GetMapping("/post-trend")
    @Operation(summary = "获取帖子发布趋势", description = "返回指定天数内每日的发帖数量，用于绘制趋势图")
    @Parameters({
            @Parameter(name = "days", description = "查询天数（7或30），默认7天", example = "7")
    })
    public Result<List<PostTrendVO>> getPostTrend(
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        List<PostTrendVO> trends = dashboardService.getPostTrend(days);
        return Result.success(trends);
    }
    
    /**
     * 获取最近活动日志
     * 显示系统内的最近操作和重要事件
     * 
     * @param limit 返回条数，最多50条，默认10条
     * @return 活动日志列表
     */
    @GetMapping("/recent-activity")
    @Operation(summary = "获取最近活动日志", description = "返回最近的操作日志和系统事件")
    @Parameters({
            @Parameter(name = "limit", description = "返回条数（最多50条），默认10条", example = "10")
    })
    public Result<List<RecentActivityVO>> getRecentActivities(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<RecentActivityVO> activities = dashboardService.getRecentActivities(limit);
        return Result.success(activities);
    }
    
    /**
     * 获取用户已设置的快捷入口列表
     * 
     * @return 快捷入口VO列表
     */
    @GetMapping("/quick-actions")
    @Operation(summary = "获取用户快捷入口", description = "返回当前用户已配置的快捷操作入口")
    public Result<List<DashboardQuickActionVO>> getUserQuickActions() {
        List<DashboardQuickActionVO> actions = dashboardService.getUserQuickActions();
        return Result.success(actions);
    }
    
    /**
     * 保存或更新用户快捷入口
     * 每个用户最多可设置4个快捷入口
     * 
     * @param actions 快捷入口列表
     * @return 成功响应
     */
    @PostMapping("/quick-actions")
    @Operation(summary = "保存用户快捷入口", description = "保存用户自定义的快捷操作入口（最多4个）")
    public Result<Void> saveUserQuickActions(
            @Valid @RequestBody List<QuickActionDTO> actions) {
        dashboardService.saveUserQuickActions(actions);
        return Result.success();
    }
    
    /**
     * 获取可用的快捷入口选项列表
     * 返回系统预设的所有可选快捷入口
     * 
     * @return 可用快捷入口选项列表
     */
    @GetMapping("/quick-actions/available")
    @Operation(summary = "获取可用快捷入口选项", description = "返回系统预设的所有可选快捷入口，供用户选择")
    public Result<List<Map<String, Object>>> getAvailableQuickActions() {
        List<Map<String, Object>> options = dashboardService.getAvailableQuickActions();
        return Result.success(options);
    }
}
