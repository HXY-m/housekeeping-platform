package com.housekeeping.audit.dto;

import java.time.LocalDateTime;

public record OperationLogDto(
        Long id,
        Long operatorUserId,
        String operatorName,
        String roleCode,
        String actionType,
        String targetType,
        Long targetId,
        String content,
        String ipAddress,
        LocalDateTime createdAt
) {
}
