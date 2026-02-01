package cn.wekyjay.wknetic.admin.forum.controller;

import cn.wekyjay.wknetic.admin.forum.service.ReportService;
import cn.wekyjay.wknetic.common.model.dto.CreateReportDTO;
import cn.wekyjay.wknetic.common.model.entity.ForumReport;
import cn.wekyjay.wknetic.common.model.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
     * 创建举报 - 用户可以对不适当的内容提交举报
     */
    @Operation(summary = "创建举报", description = "用户可以对帖子、评论或其他内容提交举报，指定举报类型、被举报对象和举报原因。")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public Result<Long> createReport(@Valid @RequestBody CreateReportDTO dto) {
        Long reportId = reportService.createReport(dto);
        return Result.success(reportId);
    }
    
    /**
     * 获取举报列表 - 管理员和版主查看待处理的举报
     */
    @Operation(summary = "获取举报列表", description = "分页获取举报列表，支持按状态筛选。状态值: 0-待处理, 1-处理中, 2-已处理, 3-已拒绝。")
    @Parameters({
            @Parameter(name = "page", description = "页码", example = "1"),
            @Parameter(name = "size", description = "每页条数", example = "20"),
            @Parameter(name = "status", description = "举报状态 (0-待处理 1-处理中 2-已处理 3-已拒绝)", example = "0")
    })
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
     * 处理举报 - 版主对举报进行最终处理决定
     */
    @Operation(summary = "处理举报", description = "版主对举报进行最终处理，可以批准（删除内容）或拒绝举报。")
    @Parameters({
            @Parameter(name = "reportId", description = "举报ID", required = true, example = "1"),
            @Parameter(name = "action", description = "处理动作 (approve-批准/reject-拒绝)", required = true, example = "approve"),
            @Parameter(name = "handleNote", description = "处理备注", example = "违反社区准则")
    })
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
     * 标记举报为处理中 - 版主认领举报开始处理
     */
    @Operation(summary = "标记举报为处理中", description = "版主认领举报并标记为处理中状态，防止重复处理。")
    @Parameters({
            @Parameter(name = "reportId", description = "举报ID", required = true, example = "1")
    })
    @PostMapping("/{reportId}/processing")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<Void> markAsProcessing(@PathVariable Long reportId) {
        reportService.markAsProcessing(reportId);
        return Result.success();
    }
    
    /**
     * 获取待处理举报数量 - 用于显示版主的待处理任务数
     */
    @Operation(summary = "获取待处理举报数量", description = "获取当前待处理的举报总数，用于提示版主有多少举报需要处理。")
    @GetMapping("/pending/count")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<Long> getPendingReportCount() {
        Long count = reportService.getPendingReportCount();
        return Result.success(count);
    }
}
