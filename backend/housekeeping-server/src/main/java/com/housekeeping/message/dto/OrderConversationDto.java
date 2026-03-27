package com.housekeeping.message.dto;

import java.time.LocalDateTime;

public record OrderConversationDto(
        Long orderId,
        String serviceName,
        String counterpartName,
        String bookingDate,
        String bookingSlot,
        String status,
        String progressNote,
        String latestMessage,
        LocalDateTime latestMessageAt
) {
}
