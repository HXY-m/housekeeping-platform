package com.housekeeping.worker;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.worker.dto.WorkerProfileDto;
import com.housekeeping.worker.service.WorkerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/worker/profile")
@PreAuthorize("hasRole('WORKER')")
@Tag(name = "服务人员资料", description = "服务人员当前档案与资质状态接口")
public class WorkerProfileController {

    private final WorkerProfileService workerProfileService;

    public WorkerProfileController(WorkerProfileService workerProfileService) {
        this.workerProfileService = workerProfileService;
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前服务人员档案")
    public ApiResponse<WorkerProfileDto> currentProfile() {
        return ApiResponse.ok(workerProfileService.currentProfile());
    }
}
