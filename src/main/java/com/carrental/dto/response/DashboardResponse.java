package com.carrental.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class DashboardResponse {

    // KPI cards
    private final long totalUsers;
    private final long totalCars;
    private final long totalOrders;
    private final BigDecimal totalRevenue;

    // Chi tiết
    private final long pendingCars;        // Xe chờ duyệt
    private final long pendingComplaints;  // Khiếu nại chờ xử lý
    private final long pendingOwners;      // Đơn đăng ký Owner chờ duyệt
    private final long activeUsers;        // User đang hoạt động
    private final long lockedUsers;        // User bị khóa
}