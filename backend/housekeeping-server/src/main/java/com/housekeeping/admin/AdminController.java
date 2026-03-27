package com.housekeeping.admin;

import com.housekeeping.admin.dto.AdminDashboardDto;
import com.housekeeping.common.ApiResponse;
import com.housekeeping.common.DemoDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DemoDataService demoDataService;

    public AdminController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardDto> dashboard() {
        return ApiResponse.ok(demoDataService.getAdminDashboard());
    }
}
