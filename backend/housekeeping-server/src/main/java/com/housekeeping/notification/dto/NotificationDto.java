package com.housekeeping.notification.dto;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        String type,
        String title,
        String content,
        String relatedType,
        Long relatedId,
        String actionPath,
        boolean read,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
}
