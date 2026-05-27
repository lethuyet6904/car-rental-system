package com.carrental.service.impl;

import com.carrental.dto.response.AdminOrderDetailResponse;
import com.carrental.dto.response.AdminOrderListResponse;
import com.carrental.entity.Payment;
import com.carrental.entity.RentalOrder;
import com.carrental.entity.Review;
import com.carrental.enums.OrderStatus;
import com.carrental.enums.TransactionType;
import com.carrental.repository.CarImageRepository;
import com.carrental.repository.PaymentRepository;
import com.carrental.repository.RentalOrderRepository;
import com.carrental.repository.ReviewRepository;
import com.carrental.service.AdminOrderService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderServiceImpl implements AdminOrderService {

    private final RentalOrderRepository rentalOrderRepository;
    private final CarImageRepository    carImageRepository;
    private final ReviewRepository      reviewRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public Page<AdminOrderListResponse> getOrderList(
            OrderStatus status, String timeRange, String keyword, Pageable pageable) {

        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        LocalDate fromDate = resolveFromDate(timeRange);

        return rentalOrderRepository.findByFilters(status, fromDate, kw, pageable)
                .map(order -> {
                    String firstImage = carImageRepository
                            .findByCarCarIdOrderBySortOrderAsc(order.getCar().getCarId())
                            .stream().findFirst()
                            .map(img -> img.getImageUrl())
                            .orElse(null);

                    Byte rating = reviewRepository
                            .findByRentalOrderOrderId(order.getOrderId())
                            .map(Review::getRating)
                            .orElse(null);

                    return AdminOrderListResponse.from(order, firstImage, rating);
                });
    }
    
    @Override
    public AdminOrderDetailResponse getOrderDetail(Long orderId) {
        RentalOrder order = rentalOrderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy đơn thuê id=" + orderId));

        List<String> images = carImageRepository
                .findByCarCarIdOrderBySortOrderAsc(order.getCar().getCarId())
                .stream().map(img -> img.getImageUrl()).toList();

        Payment deposit = paymentRepository
                .findByRentalOrderOrderIdAndTransactionType(
                        orderId, TransactionType.Deposit)
                .orElse(null);

        return reviewRepository.findByRentalOrderOrderId(orderId)
                .map(r -> AdminOrderDetailResponse.from(
                        order, images, deposit, r.getRating(), r.getComment()))
                .orElse(AdminOrderDetailResponse.from(order, images, deposit, null, null));
    }

    private LocalDate resolveFromDate(String timeRange) {
        if (timeRange == null || timeRange.isBlank()) return null;
        return switch (timeRange) {
            case "thisMonth" -> LocalDate.now().withDayOfMonth(1);
            case "3months"   -> LocalDate.now().minusMonths(3);
            case "6months"   -> LocalDate.now().minusMonths(6);
            default          -> null;
        };
    }
}