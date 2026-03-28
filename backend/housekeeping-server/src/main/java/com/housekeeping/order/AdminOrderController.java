package com.housekeeping.order;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.common.PageQuery;
import com.housekeeping.common.PageResult;
import com.housekeeping.order.dto.OrderDto;
import com.housekeeping.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('ADMIN_ORDER_MANAGE')")
@Tag(name = "管理员订单总览", description = "管理员查看平台订单总览")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "获取平台订单总览")
    public ApiResponse<PageResult<OrderDto>> orders(PageQuery pageQuery,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String dateFrom,
                                                    @RequestParam(required = false) String dateTo) {
        return ApiResponse.ok(orderService.pageAllOrders(
                pageQuery.safeCurrent(),
                pageQuery.safeSize(),
                status,
                keyword,
                dateFrom,
                dateTo
        ));
    }
}
