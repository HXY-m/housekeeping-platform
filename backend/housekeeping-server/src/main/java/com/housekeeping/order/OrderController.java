package com.housekeeping.order;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.common.DemoDataService;
import com.housekeeping.order.dto.OrderDto;
import com.housekeeping.order.dto.OrderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单模块", description = "预约下单与订单查询接口")
public class OrderController {

    private final DemoDataService demoDataService;

    public OrderController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping
    @Operation(summary = "获取订单列表")
    public ApiResponse<List<OrderDto>> orders() {
        return ApiResponse.ok(demoDataService.getOrders());
    }

    @PostMapping
    @Operation(summary = "创建订单")
    public ApiResponse<OrderDto> create(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.ok(demoDataService.createOrder(request));
    }
}
