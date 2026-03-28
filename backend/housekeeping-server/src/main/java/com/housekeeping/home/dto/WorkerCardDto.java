package com.housekeeping.home.dto;

import java.util.List;

public record WorkerCardDto(
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
        String avatarUrl
) {
}
