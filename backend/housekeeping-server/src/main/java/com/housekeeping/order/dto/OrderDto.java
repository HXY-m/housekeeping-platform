package com.housekeeping.order.dto;

import java.util.List;

public record OrderDto(
        Long id,
        String serviceName,
        String workerName,
        String customerName,
        String contactPhone,
        String serviceAddress,
        String bookingDate,
        String bookingSlot,
        String status,
        String progressNote,
        String remark,
        Integer payableAmount,
        String paymentStatus,
        String paymentMethod,
        String paidAt,
        boolean reviewed,
        Integer reviewRating,
        String reviewContent,
        List<OrderServiceRecordDto> serviceRecords
) {
    public OrderDto(Long id,
                    String serviceName,
                    String workerName,
                    String customerName,
                    String contactPhone,
                    String serviceAddress,
                    String bookingDate,
                    String bookingSlot,
                    String status,
                    String progressNote,
                    String remark) {
        this(id, serviceName, workerName, customerName, contactPhone, serviceAddress, bookingDate, bookingSlot,
                status, progressNote, remark, 0, "UNPAID", "", null, false, null, null, List.of());
    }
}
