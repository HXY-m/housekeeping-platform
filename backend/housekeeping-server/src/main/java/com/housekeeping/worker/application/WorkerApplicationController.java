package com.housekeeping.worker.application;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.common.PageQuery;
import com.housekeeping.common.PageResult;
import com.housekeeping.worker.application.dto.WorkerApplicationDto;
import com.housekeeping.worker.application.dto.WorkerApplicationReviewRequest;
import com.housekeeping.worker.application.dto.WorkerApplicationSubmitRequest;
import com.housekeeping.worker.application.service.WorkerApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/worker-applications")
@Tag(name = "服务人员资质", description = "服务人员提交资质审核资料，管理员审核")
public class WorkerApplicationController {

    private final WorkerApplicationService workerApplicationService;

    public WorkerApplicationController(WorkerApplicationService workerApplicationService) {
        this.workerApplicationService = workerApplicationService;
    }

    @PreAuthorize("hasRole('WORKER') and hasAuthority('WORKER_QUALIFICATION_SUBMIT')")
    @PostMapping
    @Operation(summary = "提交服务人员资质申请")
    public ApiResponse<WorkerApplicationDto> submit(@Valid @RequestBody WorkerApplicationSubmitRequest request) {
        return ApiResponse.ok(workerApplicationService.submit(request));
    }

    @PreAuthorize("hasRole('WORKER') and hasAuthority('WORKER_QUALIFICATION_SUBMIT')")
    @GetMapping("/my")
    @Operation(summary = "查看我提交的资质申请")
    public ApiResponse<List<WorkerApplicationDto>> myApplications() {
        return ApiResponse.ok(workerApplicationService.myApplications());
    }

    @PreAuthorize("hasRole('ADMIN') and hasAuthority('ADMIN_APPLICATION_REVIEW')")
    @GetMapping("/admin")
    @Operation(summary = "管理员查看所有资质申请")
    public ApiResponse<PageResult<WorkerApplicationDto>> listAll(PageQuery pageQuery,
                                                                 @RequestParam(required = false) String status,
                                                                 @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(workerApplicationService.pageAll(
                pageQuery.safeCurrent(),
                pageQuery.safeSize(),
                status,
                keyword
        ));
    }

    @PreAuthorize("hasRole('ADMIN') and hasAuthority('ADMIN_APPLICATION_REVIEW')")
    @PostMapping("/admin/{id}/review")
    @Operation(summary = "管理员审核资质申请")
    public ApiResponse<WorkerApplicationDto> review(@PathVariable Long id,
                                                    @Valid @RequestBody WorkerApplicationReviewRequest request) {
        return ApiResponse.ok(workerApplicationService.review(id, request));
    }
}
