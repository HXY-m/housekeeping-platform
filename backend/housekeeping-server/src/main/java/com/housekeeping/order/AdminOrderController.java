package com.housekeeping.order;

import com.housekeeping.auth.annotation.RequireRole;
import com.housekeeping.common.ApiResponse;
import com.housekeeping.order.dto.OrderDto;
import com.housekeeping.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequireRole("ADMIN")
@Tag(name = "管理员订单总览", description = "管理员查看平台订单、支付状态与履约记录")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "获取平台订单总览")
    public ApiResponse<List<OrderDto>> orders() {
        return ApiResponse.ok(orderService.listAllOrders());
    }
}
