package com.housekeeping.admin;

import com.housekeeping.admin.dto.AdminDashboardDto;
import com.housekeeping.common.ApiResponse;
import com.housekeeping.common.DemoDataService;
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

    private final DemoDataService demoDataService;

    public AdminController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取管理员统计看板")
    public ApiResponse<AdminDashboardDto> dashboard() {
        return ApiResponse.ok(demoDataService.getAdminDashboard());
    }
}
