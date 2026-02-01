package cn.wekyjay.wknetic.admin.forum.controller;

import cn.wekyjay.wknetic.admin.forum.service.ReportService;
import cn.wekyjay.wknetic.common.model.dto.CreateReportDTO;
import cn.wekyjay.wknetic.common.model.entity.ForumReport;
import cn.wekyjay.wknetic.common.model.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 举报Controller
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Tag(name = "举报管理", description = "举报相关接口")
@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportService reportService;
    
    /**
     * 创建举报
     */
    @Operation(summary = "创建举报")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public Result<Long> createReport(@Valid @RequestBody CreateReportDTO dto) {
        Long reportId = reportService.createReport(dto);
        return Result.success(reportId);
    }
    
    /**
     * 获取举报列表
     */
    @Operation(summary = "获取举报列表")
    @GetMapping("/list")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<IPage<ForumReport>> getReports(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        IPage<ForumReport> reports = reportService.getReports(page, size, status);
        return Result.success(reports);
    }
    
    /**
     * 处理举报
     */
    @Operation(summary = "处理举报")
    @PostMapping("/{reportId}/handle")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<Void> handleReport(
            @PathVariable Long reportId,
            @RequestParam String action,
            @RequestParam(required = false) String handleNote) {
        reportService.handleReport(reportId, action, handleNote);
        return Result.success();
    }
    
    /**
     * 标记举报为处理中
     */
    @Operation(summary = "标记举报为处理中")
    @PostMapping("/{reportId}/processing")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<Void> markAsProcessing(@PathVariable Long reportId) {
        reportService.markAsProcessing(reportId);
        return Result.success();
    }
    
    /**
     * 获取待处理举报数量
     */
    @Operation(summary = "获取待处理举报数量")
    @GetMapping("/pending/count")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<Long> getPendingReportCount() {
        Long count = reportService.getPendingReportCount();
        return Result.success(count);
    }
}
