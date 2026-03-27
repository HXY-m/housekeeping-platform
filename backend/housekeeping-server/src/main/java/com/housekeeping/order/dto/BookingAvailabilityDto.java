package com.housekeeping.order.dto;

import java.util.List;

public record BookingAvailabilityDto(
        Long workerId,
        String bookingDate,
        List<String> availableSlots,
        List<String> occupiedSlots
) {
}
