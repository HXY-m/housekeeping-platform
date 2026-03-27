package com.housekeeping.order;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.common.DemoDataService;
import com.housekeeping.order.dto.OrderDto;
import com.housekeeping.order.dto.OrderRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final DemoDataService demoDataService;

    public OrderController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping
    public ApiResponse<List<OrderDto>> orders() {
        return ApiResponse.ok(demoDataService.getOrders());
    }

    @PostMapping
    public ApiResponse<OrderDto> create(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.ok(demoDataService.createOrder(request));
    }
}
