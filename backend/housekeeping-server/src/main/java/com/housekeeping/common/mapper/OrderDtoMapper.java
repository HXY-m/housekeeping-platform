package com.housekeeping.common.mapper;

import com.housekeeping.order.dto.OrderDto;
import com.housekeeping.order.dto.OrderServiceRecordDto;
import com.housekeeping.order.PaymentStatus;
import com.housekeeping.order.entity.OrderEntity;
import com.housekeeping.order.entity.OrderReviewEntity;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class OrderDtoMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public OrderDto toDto(OrderEntity order, String workerName, String latestProgress) {
        return toDto(order, workerName, latestProgress, null, List.of());
    }

    public OrderDto toDto(OrderEntity order, String workerName, String latestProgress, OrderReviewEntity review) {
        return toDto(order, workerName, latestProgress, review, List.of());
    }

    public OrderDto toDto(OrderEntity order,
                          String workerName,
                          String latestProgress,
                          OrderReviewEntity review,
                          List<OrderServiceRecordDto> serviceRecords) {
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
                order.getPayableAmount() == null ? 0 : order.getPayableAmount(),
                order.getPaymentStatus() == null ? PaymentStatus.UNPAID.code() : order.getPaymentStatus(),
                order.getPaymentMethod() == null ? "" : order.getPaymentMethod(),
                order.getPaidAt() == null ? null : order.getPaidAt().format(DATE_TIME_FORMATTER),
                review != null,
                review != null ? review.getRating() : null,
                review != null ? review.getContent() : null,
                serviceRecords == null ? List.of() : serviceRecords
        );
    }
}
