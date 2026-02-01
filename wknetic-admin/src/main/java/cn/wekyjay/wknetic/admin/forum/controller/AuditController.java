package cn.wekyjay.wknetic.admin.forum.controller;

import cn.wekyjay.wknetic.admin.forum.service.AuditService;
import cn.wekyjay.wknetic.common.model.entity.ForumPost;
import cn.wekyjay.wknetic.common.model.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
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
     * 获取待审核帖子列表
     */
    @Operation(summary = "获取待审核帖子列表")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<IPage<ForumPost>> getPendingPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<ForumPost> posts = auditService.getPendingPosts(page, size);
        return Result.success(posts);
    }
    
    /**
     * 审核通过帖子
     */
    @Operation(summary = "审核通过帖子")
    @PostMapping("/approve/{postId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<Void> approvePost(@PathVariable Long postId) {
        auditService.approvePost(postId);
        return Result.success();
    }
    
    /**
     * 审核拒绝帖子
     */
    @Operation(summary = "审核拒绝帖子")
    @PostMapping("/reject/{postId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<Void> rejectPost(
            @PathVariable Long postId,
            @RequestParam String reason) {
        auditService.rejectPost(postId, reason);
        return Result.success();
    }
    
    /**
     * 获取审核历史
     */
    @Operation(summary = "获取审核历史")
    @GetMapping("/history")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<IPage<ForumPost>> getAuditHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        IPage<ForumPost> posts = auditService.getAuditHistory(page, size, status);
        return Result.success(posts);
    }
    
    /**
     * 获取待审核帖子数量
     */
    @Operation(summary = "获取待审核帖子数量")
    @GetMapping("/pending/count")
    @PreAuthorize("hasRole('MODERATOR')")
    public Result<Long> getPendingPostCount() {
        Long count = auditService.getPendingPostCount();
        return Result.success(count);
    }
}
