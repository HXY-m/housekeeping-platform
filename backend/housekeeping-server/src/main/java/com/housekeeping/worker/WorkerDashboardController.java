package com.housekeeping.worker;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.dashboard.service.DashboardService;
import com.housekeeping.worker.dto.WorkerDashboardDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/worker/dashboard")
@PreAuthorize("hasRole('WORKER') and hasAuthority('WORKER_DASHBOARD_VIEW')")
@Tag(name = "服务人员工作台看板", description = "服务人员营业概览、收入趋势与订单总览接口")
public class WorkerDashboardController {

    private final DashboardService dashboardService;

    public WorkerDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "获取当前服务人员数据中心")
    public ApiResponse<WorkerDashboardDto> dashboard() {
        return ApiResponse.ok(dashboardService.getCurrentWorkerDashboard());
    }
}
