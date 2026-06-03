package com.carrental.service.impl;

import com.carrental.dto.response.DashboardResponse;
import com.carrental.enums.*;
import com.carrental.repository.*;
import com.carrental.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository              userRepository;
    private final CarRepository               carRepository;
    private final RentalOrderRepository       rentalOrderRepository;
    private final ComplaintRepository         complaintRepository;
    private final IdentityVerificationRepository identityVerificationRepository;

    @Override
    public DashboardResponse getStats(String period) {
        LocalDateTime fromDate = resolveFromDate(period);

        // Row 1
        long       totalUsers   = userRepository.count();
        long       totalCars    = carRepository.countByStatus(CarStatus.Active);
        long       totalOrders  = rentalOrderRepository.count();
        BigDecimal totalRevenue = rentalOrderRepository.sumCompletedRevenue();

        // Row 2
        long pendingCars       = carRepository.countByStatus(CarStatus.Pending);
        long pendingComplaints = complaintRepository.countByStatus(ComplaintStatus.Pending);
        long pendingIdentities = identityVerificationRepository.countTotalPendingVerifications(VerificationStatus.Pending, VerificationStatus.Approved, LicenseStatus.Pending);

        // Pie
        // Sau khi fix — lọc theo fromDate
        long completedOrders       = rentalOrderRepository.countByStatusAndFromDate(OrderStatus.Completed,       fromDate);
        long cancelledOrders       = rentalOrderRepository.countByStatusAndFromDate(OrderStatus.Cancelled,       fromDate);
        long inProgressOrders      = rentalOrderRepository.countByStatusAndFromDate(OrderStatus.InProgress,      fromDate);
        long confirmedOrders       = rentalOrderRepository.countByStatusAndFromDate(OrderStatus.Confirmed,       fromDate);
        long pendingApprovalOrders = rentalOrderRepository.countByStatusAndFromDate(OrderStatus.PendingApproval, fromDate);
        long pendingOrders         = rentalOrderRepository.countByStatusAndFromDate(OrderStatus.Pending,         fromDate);
        long rejectedOrders        = rentalOrderRepository.countByStatusAndFromDate(OrderStatus.Rejected,        fromDate);

        // Chart data — dùng labels từ orderCount (bao quát hơn revenue)
        List<Object[]> orderRaw;
        List<Object[]> revenueRaw;

        if ("all".equals(period) || period == null) {
            orderRaw   = rentalOrderRepository.findOrderCountByYear();
            revenueRaw = rentalOrderRepository.findRevenueByYear();
        } else if ("year".equals(period)) {
            orderRaw   = rentalOrderRepository.findOrderCountByMonthThisYear();
            revenueRaw = rentalOrderRepository.findRevenueByMonthThisYear();
        } else {
            // Giữ nguyên query cũ theo ngày cho today/week/month
            orderRaw   = rentalOrderRepository.findOrderCountByDate(fromDate);
            revenueRaw = rentalOrderRepository.findRevenueByDate(fromDate);
        }

        // Build map revenue theo date để align với order labels
        java.util.Map<String, BigDecimal> revenueMap = new java.util.LinkedHashMap<>();
        for (Object[] r : revenueRaw) {
            revenueMap.put(r[0].toString(), (BigDecimal) r[1]);
        }

        List<String>     chartLabels    = orderRaw.stream().map(r -> r[0].toString()).toList();
        List<Long>       orderCountData = orderRaw.stream()
                .map(r -> ((Number) r[1]).longValue()).toList();
        List<BigDecimal> revenueData    = chartLabels.stream()
                .map(label -> revenueMap.getOrDefault(label, BigDecimal.ZERO)).toList();

        // Top 5
        List<Object[]> topCarsRaw = rentalOrderRepository.findTop5Cars(PageRequest.of(0, 5));
        List<DashboardResponse.TopCarItem> topCars = topCarsRaw.stream()
                .map(r -> DashboardResponse.TopCarItem.builder()
                        .modelName(   (String) r[0])
                        .licensePlate((String) r[1])
                        .orderCount(  ((Number) r[2]).longValue())
                        .build())
                .toList();

        List<Object[]> topRegionsRaw = rentalOrderRepository.findTop5Regions(PageRequest.of(0, 5));
        List<DashboardResponse.TopRegionItem> topRegions = topRegionsRaw.stream()
                .map(r -> DashboardResponse.TopRegionItem.builder()
                        .regionName((String) r[0])
                        .orderCount(((Number) r[1]).longValue())
                        .build())
                .toList();

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalCars(totalCars)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .pendingCars(pendingCars)
                .pendingComplaints(pendingComplaints)
                .pendingIdentities(pendingIdentities)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .inProgressOrders(inProgressOrders)
                .confirmedOrders(confirmedOrders)
                .pendingApprovalOrders(pendingApprovalOrders)
                .pendingOrders(pendingOrders)
                .rejectedOrders(rejectedOrders)
                .chartLabels(chartLabels)
                .orderCountData(orderCountData)
                .revenueData(revenueData)
                .topCars(topCars)
                .topRegions(topRegions)
                .build();
    }

    private LocalDateTime resolveFromDate(String period) {
        return switch (period == null ? "all" : period) {
            case "year" -> LocalDate.of(LocalDate.now().getYear(), 1, 1).atStartOfDay(); // 01/01 năm nay
            case "all"  -> LocalDate.of(2000, 1, 1).atStartOfDay();
            default     -> LocalDate.of(2000, 1, 1).atStartOfDay(); // default cũng là all
        };
    }
}