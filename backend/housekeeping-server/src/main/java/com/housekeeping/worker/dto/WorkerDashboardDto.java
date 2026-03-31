package com.housekeeping.worker.dto;

import com.housekeeping.dashboard.dto.DashboardPaymentFlowDto;
import com.housekeeping.dashboard.dto.DashboardRevenuePointDto;

import java.util.List;
import java.util.Map;

public record WorkerDashboardDto(
        String qualificationStatus,
        long totalOrders,
        long pendingOrders,
        long confirmedOrders,
        long inServiceOrders,
        long waitingUserConfirmationOrders,
        long completedOrders,
        long todoOrders,
        long paidOrderCount,
        long totalRevenue,
        long todayRevenue,
        long monthRevenue,
        Map<String, Integer> serviceSales,
        Map<String, Long> statusDistribution,
        List<DashboardRevenuePointDto> revenueTrend,
        List<DashboardPaymentFlowDto> recentPayments
) {
}
