package com.carrental.service.impl;

import com.carrental.dto.response.DashboardResponse;
import com.carrental.enums.CarStatus;
import com.carrental.enums.ComplaintStatus;
import com.carrental.enums.OrderStatus;
import com.carrental.enums.VerificationStatus;
import com.carrental.repository.*;
import com.carrental.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository            userRepository;
    private final CarRepository             carRepository;
    private final RentalOrderRepository     rentalOrderRepository;
    private final ComplaintRepository       complaintRepository;
    private final OwnerRegistrationRepository ownerRegistrationRepository;

    @Override
    public DashboardResponse getStats() {

        // KPI
        long totalUsers   = userRepository.count();
        long totalCars    = carRepository.countByStatus(CarStatus.Active);
        long totalOrders  = rentalOrderRepository.count();
        var  totalRevenue = rentalOrderRepository.sumCompletedRevenue();

        // Alerts
        long pendingCars      = carRepository.countByStatus(CarStatus.Pending);
        long pendingComplaints = complaintRepository.countByStatus(ComplaintStatus.Pending)
                               + complaintRepository.countByStatus(ComplaintStatus.Processing);
        long pendingOwners    = ownerRegistrationRepository
                                    .findByStatus(VerificationStatus.Pending).size();
        long activeOrders     = rentalOrderRepository.countByStatus(OrderStatus.InProgress);

        // Trạng thái đơn cho pie chart
        long completedOrders  = rentalOrderRepository.countByStatus(OrderStatus.Completed);
        long cancelledOrders  = rentalOrderRepository.countByStatus(OrderStatus.Cancelled);
        long inProgressOrders = activeOrders;

        // Top 5 xe
        List<Object[]> topCarsRaw = rentalOrderRepository
                .findTop5Cars(PageRequest.of(0, 5));
        List<DashboardResponse.TopCarItem> topCars = topCarsRaw.stream()
                .map(row -> DashboardResponse.TopCarItem.builder()
                        .modelName(   (String) row[0])
                        .licensePlate((String) row[1])
                        .orderCount(  ((Number) row[2]).longValue())
                        .build())
                .toList();

        // Top 5 khu vực
        List<Object[]> topRegionsRaw = rentalOrderRepository
                .findTop5Regions(PageRequest.of(0, 5));
        List<DashboardResponse.TopRegionItem> topRegions = topRegionsRaw.stream()
                .map(row -> DashboardResponse.TopRegionItem.builder()
                        .regionName((String) row[0])
                        .orderCount(((Number) row[1]).longValue())
                        .build())
                .toList();

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalCars(totalCars)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .pendingCars(pendingCars)
                .pendingComplaints(pendingComplaints)
                .pendingOwners(pendingOwners)
                .activeOrders(activeOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .inProgressOrders(inProgressOrders)
                .topCars(topCars)
                .topRegions(topRegions)
                .build();
    }
}