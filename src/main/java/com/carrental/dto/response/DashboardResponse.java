package com.carrental.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    // Row 1 — KPI
    private final long totalUsers;
    private final long totalCars;
    private final long totalOrders;
    private final BigDecimal totalRevenue;

    // Row 2 — Alerts
    private final long pendingComplaints;
    private final long pendingCars;
    private final long pendingIdentities;

    // Pie chart
    private final long completedOrders;
    private final long cancelledOrders;
    private final long inProgressOrders;
    private final long confirmedOrders;
    private final long pendingApprovalOrders;
    private final long pendingOrders;
    private final long rejectedOrders;

    // Chart data — dùng chung labels cho cả 2 chart
    private final List<String> chartLabels;
    private final List<Long> orderCountData;
    private final List<BigDecimal> revenueData;

    // Row 4 — Top lists
    private final List<TopCarItem> topCars;
    private final List<TopRegionItem> topRegions;

    @Getter
    @Builder
    public static class TopCarItem {
        private final String modelName;
        private final String licensePlate;
        private final long orderCount;
    }

    @Getter
    @Builder
    public static class TopRegionItem {
        private final String regionName;
        private final long orderCount;
    }
}