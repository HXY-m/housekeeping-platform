package com.housekeeping.worker.dto;

public record WorkerProfileUpsertCommand(
        Long userId,
        String realName,
        String city,
        Integer hourlyPrice,
        String serviceTypes,
        String availableSchedule,
        Integer yearsOfExperience,
        String certificates,
        String serviceAreas,
        String intro,
        String avatarUrl,
        String qualificationStatus,
        String roleLabel,
        String serviceCases
) {

    public WorkerProfileUpsertCommand(Long userId,
                                      String realName,
                                      String city,
                                      Integer hourlyPrice,
                                      String serviceTypes,
                                      String availableSchedule,
                                      Integer yearsOfExperience,
                                      String certificates,
                                      String serviceAreas,
                                      String intro,
                                      String qualificationStatus,
                                      String roleLabel,
                                      String serviceCases) {
        this(
                userId,
                realName,
                city,
                hourlyPrice,
                serviceTypes,
                availableSchedule,
                yearsOfExperience,
                certificates,
                serviceAreas,
                intro,
                "",
                qualificationStatus,
                roleLabel,
                serviceCases
        );
    }
}
