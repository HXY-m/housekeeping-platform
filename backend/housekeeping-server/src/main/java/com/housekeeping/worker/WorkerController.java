package com.housekeeping.worker;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.common.DemoDataService;
import com.housekeeping.home.dto.WorkerCardDto;
import com.housekeeping.worker.dto.WorkerDetailDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    private final DemoDataService demoDataService;

    public WorkerController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping
    public ApiResponse<List<WorkerCardDto>> workers(@RequestParam(required = false) String serviceName) {
        return ApiResponse.ok(demoDataService.getWorkers(serviceName));
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkerDetailDto> worker(@PathVariable Long id) {
        return ApiResponse.ok(demoDataService.getWorker(id));
    }
}
