package com.housekeeping.admin;

import com.housekeeping.audit.dto.OperationLogDto;
import com.housekeeping.audit.service.OperationLogService;
import com.housekeeping.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/operation-logs")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('ADMIN_OPERATION_LOG_VIEW')")
@Tag(name = "管理员操作日志", description = "管理员筛选查看关键治理操作日志")
public class AdminOperationLogController {

    private final OperationLogService operationLogService;

    public AdminOperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    @Operation(summary = "获取操作日志")
    public ApiResponse<List<OperationLogDto>> logs(@RequestParam(required = false) String operatorName,
                                                   @RequestParam(required = false) String roleCode,
                                                   @RequestParam(required = false) String actionType,
                                                   @RequestParam(required = false) String dateFrom,
                                                   @RequestParam(required = false) String dateTo) {
        return ApiResponse.ok(operationLogService.list(operatorName, roleCode, actionType, dateFrom, dateTo));
    }
}
