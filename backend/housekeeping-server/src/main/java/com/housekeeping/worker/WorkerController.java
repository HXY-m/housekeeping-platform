package com.housekeeping.worker;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.common.DemoDataService;
import com.housekeeping.common.PageQuery;
import com.housekeeping.common.PageResult;
import com.housekeeping.home.dto.WorkerCardDto;
import com.housekeeping.worker.dto.WorkerDetailDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workers")
@Tag(name = "服务人员模块", description = "服务人员列表与详情接口")
public class WorkerController {

    private final DemoDataService demoDataService;

    public WorkerController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping
    @Operation(summary = "获取服务人员列表", description = "支持通过 serviceName 按服务类型筛选")
    public ApiResponse<PageResult<WorkerCardDto>> workers(PageQuery pageQuery,
                                                          @RequestParam(required = false) String serviceName) {
        return ApiResponse.ok(demoDataService.pageWorkers(pageQuery.safeCurrent(), pageQuery.safeSize(), serviceName));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取服务人员详情")
    public ApiResponse<WorkerDetailDto> worker(@PathVariable Long id) {
        return ApiResponse.ok(demoDataService.getWorker(id));
    }
}
