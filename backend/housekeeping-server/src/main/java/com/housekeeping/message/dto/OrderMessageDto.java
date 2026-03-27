package com.housekeeping.message.dto;

import java.time.LocalDateTime;

public record OrderMessageDto(
        Long id,
        Long orderId,
        Long senderUserId,
        String senderRoleCode,
        String senderName,
        String content,
        LocalDateTime createdAt
) {
}
