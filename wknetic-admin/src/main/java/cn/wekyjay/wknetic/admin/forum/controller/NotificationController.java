package cn.wekyjay.wknetic.admin.forum.controller;

import cn.wekyjay.wknetic.admin.system.service.NotificationService;
import cn.wekyjay.wknetic.common.model.vo.NotificationVO;
import cn.wekyjay.wknetic.common.model.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
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
     * 获取用户通知列表
     */
    @Operation(summary = "获取用户通知列表")
    @GetMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public Result<IPage<NotificationVO>> getUserNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<NotificationVO> notifications = notificationService.getUserNotifications(page, size);
        return Result.success(notifications);
    }
    
    /**
     * 获取未读通知数量
     */
    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread/count")
    @PreAuthorize("hasRole('USER')")
    public Result<Long> getUnreadCount() {
        int count = notificationService.getUnreadCount();
        return Result.success((long) count);
    }
    
    /**
     * 标记通知为已读
     */
    @Operation(summary = "标记通知为已读")
    @PostMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return Result.success();
    }
    
    /**
     * 标记所有通知为已读
     */
    @Operation(summary = "标记所有通知为已读")
    @PostMapping("/read/all")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return Result.success();
    }
}
