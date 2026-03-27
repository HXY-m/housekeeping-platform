package com.housekeeping.order.dto;

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
        boolean reviewed,
        Integer reviewRating,
        String reviewContent
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
                status, progressNote, remark, false, null, null);
    }
}
