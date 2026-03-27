package com.housekeeping.aftersale.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AfterSaleDto(
        Long id,
        Long orderId,
        String serviceName,
        String workerName,
        String customerName,
        String orderStatus,
        String issueType,
        String content,
        String contactPhone,
        String status,
        String adminRemark,
        List<AfterSaleAttachmentDto> attachments,
        LocalDateTime createdAt,
        LocalDateTime handledAt
) {
}
