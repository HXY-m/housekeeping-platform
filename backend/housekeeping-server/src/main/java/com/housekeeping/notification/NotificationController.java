package com.housekeeping.notification;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.notification.dto.NotificationDto;
import com.housekeeping.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasAnyRole('USER','WORKER','ADMIN')")
@Tag(name = "通知中心", description = "用户、服务人员和管理员的站内通知接口")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "获取当前账号的通知列表")
    public ApiResponse<List<NotificationDto>> list() {
        return ApiResponse.ok(notificationService.listCurrentNotifications());
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取当前账号未读通知数量")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.ok(notificationService.countUnreadCurrentUser());
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "将单条通知标记为已读")
    public ApiResponse<NotificationDto> markRead(@PathVariable Long id) {
        return ApiResponse.ok(notificationService.markRead(id));
    }

    @PostMapping("/read-all")
    @Operation(summary = "将当前账号所有通知标记为已读")
    public ApiResponse<Long> markAllRead() {
        return ApiResponse.ok(notificationService.markAllRead());
    }
}
