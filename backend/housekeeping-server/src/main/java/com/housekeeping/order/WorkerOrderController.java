package com.housekeeping.order;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.order.dto.OrderDto;
import com.housekeeping.order.dto.OrderServiceRecordDto;
import com.housekeeping.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/worker/orders")
@PreAuthorize("hasRole('WORKER')")
@Tag(name = "服务人员订单工作台", description = "服务人员查看、接单、开工、上传过程凭证与提交完工")
public class WorkerOrderController {

    private final OrderService orderService;

    public WorkerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "获取当前服务人员订单列表")
    public ApiResponse<List<OrderDto>> orders() {
        return ApiResponse.ok(orderService.listCurrentWorkerOrders());
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "接单")
    public ApiResponse<OrderDto> accept(@PathVariable Long id) {
        return ApiResponse.ok(orderService.acceptOrder(id));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "开始服务")
    public ApiResponse<OrderDto> start(@PathVariable Long id) {
        return ApiResponse.ok(orderService.startOrder(id));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "提交完工，等待用户确认")
    public ApiResponse<OrderDto> complete(@PathVariable Long id) {
        return ApiResponse.ok(orderService.submitCompletionByWorker(id));
    }

    @PostMapping(value = "/{id}/service-records", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传服务过程记录")
    public ApiResponse<OrderServiceRecordDto> uploadServiceRecord(@PathVariable Long id,
                                                                  @RequestPart("stage") String stage,
                                                                  @RequestPart("description") String description,
                                                                  @RequestPart(value = "file", required = false) MultipartFile file) {
        return ApiResponse.ok(orderService.uploadWorkerServiceRecord(id, stage, description, file));
    }
}
