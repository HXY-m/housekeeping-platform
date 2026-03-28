package com.housekeeping.order;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.order.dto.BookingAvailabilityDto;
import com.housekeeping.order.dto.OrderDto;
import com.housekeeping.order.dto.OrderRequest;
import com.housekeeping.order.dto.OrderReviewRequest;
import com.housekeeping.order.service.OrderService;
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
@RequestMapping("/api/orders")
@PreAuthorize("hasRole('USER') and hasAuthority('USER_ORDER_USE')")
@Tag(name = "订单模块", description = "用户侧下单、订单查询、确认服务与评价接口")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "获取当前用户订单列表")
    public ApiResponse<List<OrderDto>> orders() {
        return ApiResponse.ok(orderService.listCurrentUserOrders());
    }

    @GetMapping("/availability")
    @Operation(summary = "查询服务人员在指定日期的可预约时段")
    public ApiResponse<BookingAvailabilityDto> availability(@RequestParam Long workerId,
                                                            @RequestParam String bookingDate) {
        return ApiResponse.ok(orderService.getBookingAvailability(workerId, bookingDate));
    }

    @PostMapping
    @Operation(summary = "创建预约订单")
    public ApiResponse<OrderDto> create(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.ok(orderService.createOrder(request));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "用户确认预约安排")
    public ApiResponse<OrderDto> confirm(@PathVariable Long id) {
        return ApiResponse.ok(orderService.confirmOrderByUser(id));
    }

    @PostMapping("/{id}/confirm-completion")
    @Operation(summary = "用户确认完工")
    public ApiResponse<OrderDto> confirmCompletion(@PathVariable Long id) {
        return ApiResponse.ok(orderService.confirmCompletionByUser(id));
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "提交订单评价")
    public ApiResponse<OrderDto> review(@PathVariable Long id,
                                        @Valid @RequestBody OrderReviewRequest request) {
        return ApiResponse.ok(orderService.reviewOrder(id, request));
    }
}
