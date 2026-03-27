package com.housekeeping.admin.dto;

import java.util.Map;

public record AdminDashboardDto(
        int totalOrders,
        int completedOrders,
        int activeWorkers,
        double averageRating,
        Map<String, Integer> serviceSales
) {
}
