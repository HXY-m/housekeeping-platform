package com.housekeeping.message;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.message.dto.OrderConversationDto;
import com.housekeeping.message.dto.OrderMessageDto;
import com.housekeeping.message.dto.OrderMessageSendRequest;
import com.housekeeping.message.service.OrderMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/communications/orders")
@PreAuthorize("(hasRole('USER') and hasAuthority('USER_MESSAGE_USE'))"
        + " or (hasRole('WORKER') and hasAuthority('WORKER_MESSAGE_USE'))")
@Tag(name = "订单沟通", description = "用户与服务人员围绕订单进行留言沟通")
public class OrderMessageController {

    private final OrderMessageService orderMessageService;

    public OrderMessageController(OrderMessageService orderMessageService) {
        this.orderMessageService = orderMessageService;
    }

    @GetMapping
    @Operation(summary = "获取当前角色可见的订单沟通列表")
    public ApiResponse<List<OrderConversationDto>> conversations() {
        return ApiResponse.ok(orderMessageService.listCurrentConversations());
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "获取指定订单的留言记录")
    public ApiResponse<List<OrderMessageDto>> messages(@PathVariable Long orderId) {
        return ApiResponse.ok(orderMessageService.listOrderMessages(orderId));
    }

    @PostMapping("/{orderId}")
    @Operation(summary = "发送订单沟通留言")
    public ApiResponse<OrderMessageDto> send(@PathVariable Long orderId,
                                             @Valid @RequestBody OrderMessageSendRequest request) {
        return ApiResponse.ok(orderMessageService.sendMessage(orderId, request.content()));
    }
}
