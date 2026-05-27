package com.carrental.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    // KPI cards
    private final long totalUsers;
    private final long totalCars;
    private final long totalOrders;
    private final BigDecimal totalRevenue;

    // Alert badges
    private final long pendingComplaints;
    private final long pendingCars;
    private final long pendingOwners;
    private final long activeOrders;

    // Biểu đồ trạng thái đơn
    private final long completedOrders;
    private final long cancelledOrders;
    private final long inProgressOrders;

    // Top xe & khu vực
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