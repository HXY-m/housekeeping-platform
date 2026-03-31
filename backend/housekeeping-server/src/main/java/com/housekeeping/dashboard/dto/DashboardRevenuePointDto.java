package com.housekeeping.dashboard.dto;

public record DashboardRevenuePointDto(
        String label,
        long amount,
        long orderCount
) {
}
