package com.housekeeping.common.mapper;

import com.housekeeping.order.dto.OrderDto;
import com.housekeeping.order.entity.OrderEntity;
import com.housekeeping.order.entity.OrderReviewEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderDtoMapper {

    public OrderDto toDto(OrderEntity order, String workerName, String latestProgress) {
        return toDto(order, workerName, latestProgress, null);
    }

    public OrderDto toDto(OrderEntity order, String workerName, String latestProgress, OrderReviewEntity review) {
        return new OrderDto(
                order.getId(),
                order.getServiceName(),
                workerName,
                order.getCustomerName(),
                order.getContactPhone(),
                order.getServiceAddress(),
                order.getBookingDate(),
                order.getBookingSlot(),
                order.getStatus(),
                latestProgress,
                order.getRemark(),
                review != null,
                review != null ? review.getRating() : null,
                review != null ? review.getContent() : null
        );
    }
}
