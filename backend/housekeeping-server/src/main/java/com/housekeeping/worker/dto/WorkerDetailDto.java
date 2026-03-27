package com.housekeeping.worker.dto;

import java.util.List;

public record WorkerDetailDto(
        Long id,
        String name,
        String roleLabel,
        double rating,
        int completedOrders,
        int hourlyPrice,
        String city,
        String intro,
        List<String> tags,
        String nextAvailable,
        int yearsOfExperience,
        List<String> certificates,
        List<String> serviceAreas,
        List<String> serviceCases
) {
}
