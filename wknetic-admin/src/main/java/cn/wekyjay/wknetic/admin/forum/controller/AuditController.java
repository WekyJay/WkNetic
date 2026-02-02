package cn.wekyjay.wknetic.admin.forum.controller;

import cn.wekyjay.wknetic.admin.forum.dto.AuditPostVO;
import cn.wekyjay.wknetic.admin.forum.dto.AuditStatisticsVO;
import cn.wekyjay.wknetic.admin.forum.service.AuditService;
import cn.wekyjay.wknetic.common.model.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 审核Controller
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Tag(name = "审核管理", description = "审核相关接口")
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {
    
    private final AuditService auditService;
    
    /**
     * 获取待审核帖子列表 - 分页查询
     */
    @Operation(summary = "获取待审核帖子列表", description = "获取所有待审核的帖子，需要管理员权限。")
    @Parameters({
            @Parameter(name = "page", description = "页码", example = "1"),
            @Parameter(name = "size", description = "每页条数", example = "20")
    })
    @GetMapping("/pending")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<IPage<AuditPostVO>> getPendingPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<AuditPostVO> posts = auditService.getPendingPosts(page, size);
        return Result.success(posts);
    }
    
    /**
     * 审核通过帖子 - 批准帖子转为已发布状态
     */
    @Operation(summary = "审核通过帖子", description = "批准指定帖子的审核，帖子将转为已发布。")
    @Parameters({
            @Parameter(name = "postId", description = "帖子ID", required = true, example = "1")
    })
    @PostMapping("/approve/{postId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<Void> approvePost(@PathVariable Long postId) {
        auditService.approvePost(postId);
        return Result.success();
    }
    
    /**
     * 审核拒绝帖子 - 拒绝帖子并提供拒绝原因
     */
    @Operation(summary = "审核拒绝帖子", description = "拒绝指定帖子的审核，帖子将转为拒绝状态。")
    @Parameters({
            @Parameter(name = "postId", description = "帖子ID", required = true, example = "1"),
            @Parameter(name = "reason", description = "拒绝原因", required = true, example = "内容不符合社区规则")
    })
    @PostMapping("/reject/{postId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<Void> rejectPost(
            @PathVariable Long postId,
            @RequestParam String reason) {
        auditService.rejectPost(postId, reason);
        return Result.success();
    }
    
    /**
     * 获取审核历史 - 查询已处理的审核记录
     */
    @Operation(summary = "获取审核历史", description = "查询已处理的审核历史，按状态筛选。")
    @Parameters({
            @Parameter(name = "page", description = "页码", example = "1"),
            @Parameter(name = "size", description = "每页条数", example = "20"),
            @Parameter(name = "status", description = "审核状态：2=通过，3=拒绝（可选）", example = "2")
    })
    @GetMapping("/history")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<IPage<AuditPostVO>> getAuditHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        IPage<AuditPostVO> posts = auditService.getAuditHistory(page, size, status);
        return Result.success(posts);
    }
    
    /**
     * 获取待审核帖子数量 - 展示待处理的审核项目数
     */
    @Operation(summary = "获取待审核帖子数量", description = "获取当前待审核的帖子数量。")
    @GetMapping("/pending/count")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<Long> getPendingPostCount() {
        Long count = auditService.getPendingPostCount();
        return Result.success(count);
    }
    
    /**
     * 获取审核统计信息
     */
    @Operation(summary = "获取审核统计", description = "获取审核统计数据，包括待审核数、通过率、拒绝率等。")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<AuditStatisticsVO> getAuditStatistics() {
        AuditStatisticsVO statistics = auditService.getAuditStatistics();
        return Result.success(statistics);
    }
}
