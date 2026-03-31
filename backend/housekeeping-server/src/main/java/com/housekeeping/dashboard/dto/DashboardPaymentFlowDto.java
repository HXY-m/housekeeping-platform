package com.housekeeping.dashboard.dto;

public record DashboardPaymentFlowDto(
        Long paymentId,
        Long orderId,
        String serviceName,
        String customerName,
        String workerName,
        int amount,
        String paymentMethod,
        String paymentStatus,
        String paidAt
) {
}
