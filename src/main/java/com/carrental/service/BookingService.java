package com.carrental.service;

import com.carrental.entity.RentalOrder;
import com.carrental.enums.OrderStatus;
import com.carrental.enums.PickupMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingService {

    RentalOrder createOrder(Long customerId, Long carId,
            LocalDate pickupDate, LocalTime pickupTime,
            LocalDate returnDate, LocalTime returnTime,
            PickupMethod pickupMethod, String deliveryAddress,
            String note);

    RentalOrder getOrderById(Long orderId);

    List<RentalOrder> getOrdersByCustomer(Long customerId);

    void cancelOrder(Long orderId, Long customerId, String reason);

    RentalOrder updateOrderStatus(Long orderId, OrderStatus status);

    /**
     * Tính chính sách hoàn cọc dựa theo ngày hủy so với pickupDate.
     * - Chưa cọc (Pending) → refundPercent = 0, không cần hoàn
     * - Hủy ≥ 3 ngày trước → hoàn 100%
     * - Hủy 1-2 ngày trước → hoàn 50%
     * - Hủy < 1 ngày trước → hoàn 0%
     */
    RefundPolicy getRefundPolicy(Long orderId);

    @Getter
    @AllArgsConstructor
    class RefundPolicy {
        private final int refundPercent; // 0, 50, 100
        private final String message;
    }
}