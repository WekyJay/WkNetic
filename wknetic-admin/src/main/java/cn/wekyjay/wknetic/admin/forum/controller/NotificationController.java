package cn.wekyjay.wknetic.admin.forum.controller;

import cn.wekyjay.wknetic.admin.system.service.NotificationService;
import cn.wekyjay.wknetic.common.model.vo.NotificationVO;
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
 * 通知Controller
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Tag(name = "通知管理", description = "通知相关接口")
@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * 获取用户通知列表 - 分页查询用户的所有通知
     */
    @Operation(summary = "获取用户通知列表", description = "获取当前用户的通知列表，按时间倒序。仅包含未推送或已读取的通知。")
    @Parameters({
            @Parameter(name = "page", description = "页码", example = "1"),
            @Parameter(name = "size", description = "每页条数", example = "20")
    })
    @GetMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public Result<IPage<NotificationVO>> getUserNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<NotificationVO> notifications = notificationService.getUserNotifications(page, size);
        return Result.success(notifications);
    }
    
    /**
     * 获取未读通知数量 - 获取有多少未读通知
     */
    @Operation(summary = "获取未读通知数量", description = "获取当前用户有数多少未读通知。")
    @GetMapping("/unread/count")
    @PreAuthorize("hasRole('USER')")
    public Result<Long> getUnreadCount() {
        int count = notificationService.getUnreadCount();
        return Result.success((long) count);
    }
    
    /**
     * 标记通知为已读 - 标记单一通知为已读状态
     */
    @Operation(summary = "标记通知为已读", description = "标记指定通知为已读。")
    @Parameters({
            @Parameter(name = "notificationId", description = "通知ID", required = true, example = "1")
    })
    @PostMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return Result.success();
    }
    
    /**
     * 标记所有通知为已读 - 一次全部标记为已读
     */
    @Operation(summary = "标记所有通知为已读", description = "一次将当前用户的所有未读通知标记为已读。")
    @PostMapping("/read/all")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return Result.success();
    }
}
