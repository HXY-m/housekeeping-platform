package com.housekeeping.admin;

import com.housekeeping.admin.dto.AdminDashboardDto;
import com.housekeeping.auth.annotation.RequireRole;
import com.housekeeping.common.ApiResponse;
import com.housekeeping.common.DemoDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequireRole("ADMIN")
@Tag(name = "管理后台模块", description = "管理员统计与运营接口")
public class AdminController {

    private final DemoDataService demoDataService;

    public AdminController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取管理员统计看板", description = "需要 ADMIN 角色")
    public ApiResponse<AdminDashboardDto> dashboard() {
        return ApiResponse.ok(demoDataService.getAdminDashboard());
    }
}
