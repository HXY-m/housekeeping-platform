package com.housekeeping.worker.application;

import com.housekeeping.auth.annotation.RequireLogin;
import com.housekeeping.auth.annotation.RequireRole;
import com.housekeeping.common.ApiResponse;
import com.housekeeping.worker.application.dto.WorkerApplicationDto;
import com.housekeeping.worker.application.dto.WorkerApplicationReviewRequest;
import com.housekeeping.worker.application.dto.WorkerApplicationSubmitRequest;
import com.housekeeping.worker.application.service.WorkerApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/worker-applications")
@Tag(name = "服务人员入驻", description = "普通用户申请成为服务人员，管理员审核")
public class WorkerApplicationController {

    private final WorkerApplicationService workerApplicationService;

    public WorkerApplicationController(WorkerApplicationService workerApplicationService) {
        this.workerApplicationService = workerApplicationService;
    }

    @RequireLogin
    @PostMapping
    @Operation(summary = "提交服务人员入驻申请")
    public ApiResponse<WorkerApplicationDto> submit(@Valid @RequestBody WorkerApplicationSubmitRequest request) {
        return ApiResponse.ok(workerApplicationService.submit(request));
    }

    @RequireLogin
    @GetMapping("/my")
    @Operation(summary = "查看我提交的入驻申请")
    public ApiResponse<List<WorkerApplicationDto>> myApplications() {
        return ApiResponse.ok(workerApplicationService.myApplications());
    }

    @RequireRole("ADMIN")
    @GetMapping("/admin")
    @Operation(summary = "管理员查看所有入驻申请")
    public ApiResponse<List<WorkerApplicationDto>> listAll() {
        return ApiResponse.ok(workerApplicationService.listAll());
    }

    @RequireRole("ADMIN")
    @PostMapping("/admin/{id}/review")
    @Operation(summary = "管理员审核入驻申请", description = "action 传 APPROVE 或 REJECT")
    public ApiResponse<WorkerApplicationDto> review(@PathVariable Long id,
                                                    @Valid @RequestBody WorkerApplicationReviewRequest request) {
        return ApiResponse.ok(workerApplicationService.review(id, request));
    }
}
