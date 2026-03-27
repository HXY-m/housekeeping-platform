package com.housekeeping.order;

import com.housekeeping.auth.annotation.RequireRole;
import com.housekeeping.common.ApiResponse;
import com.housekeeping.order.dto.OrderDto;
import com.housekeeping.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/worker/orders")
@RequireRole("WORKER")
@Tag(name = "服务人员订单工作台", description = "服务人员查看、接单、开工和完成订单")
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
    @Operation(summary = "完成服务")
    public ApiResponse<OrderDto> complete(@PathVariable Long id) {
        return ApiResponse.ok(orderService.completeOrder(id));
    }
}
