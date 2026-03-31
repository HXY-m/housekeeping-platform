package com.housekeeping.order.dto;

public record OrderPaymentDto(
        Long id,
        Integer amount,
        String paymentMethod,
        String paymentStatus,
        String paymentNo,
        String createdAt,
        String paidAt
) {
}
