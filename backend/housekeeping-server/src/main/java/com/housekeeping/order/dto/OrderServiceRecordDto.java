package com.housekeeping.order.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderServiceRecordDto(
        Long id,
        String stage,
        String description,
        LocalDateTime createdAt,
        List<OrderServiceRecordAttachmentDto> attachments
) {
}
