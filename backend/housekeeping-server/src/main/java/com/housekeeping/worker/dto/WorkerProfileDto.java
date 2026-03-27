package com.housekeeping.worker.dto;

public record WorkerProfileDto(
        Long id,
        Long userId,
        String realName,
        String phone,
        String city,
        Integer hourlyPrice,
        String serviceTypes,
        String availableSchedule,
        Integer yearsOfExperience,
        String certificates,
        String serviceAreas,
        String intro,
        String qualificationStatus
) {
}
