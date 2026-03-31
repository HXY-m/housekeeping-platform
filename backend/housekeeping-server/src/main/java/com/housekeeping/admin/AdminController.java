package com.housekeeping.admin;

import com.housekeeping.admin.dto.AdminDashboardDto;
import com.housekeeping.common.ApiResponse;
import com.housekeeping.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('ADMIN_DASHBOARD_VIEW')")
@Tag(name = "管理后台模块", description = "管理员统计与运营接口")
public class AdminController {

    private final DashboardService dashboardService;

    public AdminController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取管理员统计看板")
    public ApiResponse<AdminDashboardDto> dashboard() {
        return ApiResponse.ok(dashboardService.getAdminDashboard());
    }
}
