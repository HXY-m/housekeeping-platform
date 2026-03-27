package com.housekeeping.worker.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record WorkerApplicationDto(
        Long id,
        Long userId,
        String realName,
        String phone,
        String serviceTypes,
        Integer yearsOfExperience,
        String certificates,
        String serviceAreas,
        String availableSchedule,
        String intro,
        String status,
        String adminRemark,
        LocalDateTime createdAt,
        List<WorkerApplicationAttachmentDto> attachments
) {
}
