package com.carrental.service.impl;

import com.carrental.entity.Car;
import com.carrental.entity.RentalOrder;
import com.carrental.entity.User;
import com.carrental.enums.CarStatus;
import com.carrental.enums.OrderStatus;
import com.carrental.enums.PickupMethod;
import com.carrental.repository.RentalOrderRepository;
import com.carrental.repository.UserRepository;
import com.carrental.service.BookingService;
import com.carrental.service.CarService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final RentalOrderRepository rentalOrderRepository;
    private final UserRepository userRepository;
    private final CarService carService;

    @Override
    @Transactional
    public RentalOrder createOrder(Long customerId, Long carId,
                                   LocalDate pickupDate, LocalTime pickupTime,
                                   LocalDate returnDate, LocalTime returnTime,
                                   PickupMethod pickupMethod,
                                   String deliveryAddress, String note) {

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        Car car = carService.getCarEntityById(carId);
        if (car == null) {
            throw new RuntimeException("Không tìm thấy xe");
        }

        if (car.getStatus() != CarStatus.Active) {
            throw new RuntimeException("Xe hiện không khả dụng để thuê");
        }

        long days = ChronoUnit.DAYS.between(pickupDate, returnDate);
        if (days <= 0) {
            throw new RuntimeException("Ngày trả xe phải sau ngày nhận xe ít nhất 1 ngày");
        }

        BigDecimal totalAmount = car.getPricePerDay().multiply(BigDecimal.valueOf(days));
        BigDecimal deposit = totalAmount.multiply(BigDecimal.valueOf(0.3)); // 30% cọc

        RentalOrder order = new RentalOrder();
        order.setCustomer(customer);
        order.setCar(car);
        order.setPickupDate(pickupDate);
        order.setPickupTime(pickupTime);
        order.setReturnDate(returnDate);
        order.setReturnTime(returnTime);
        order.setTotalDays((int) days);
        order.setTotalAmount(totalAmount);
        order.setDepositAmount(deposit);
        order.setPickupMethod(pickupMethod);
        order.setDeliveryAddress(deliveryAddress);
        order.setNote(note);
        // SỬA: Chuyển sang chờ thanh toán thay vì pending ngay
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setCreatedAt(LocalDateTime.now());

        return rentalOrderRepository.save(order);
    }

    @Override
    @Transactional
    public RentalOrder updateOrderStatus(Long orderId, OrderStatus status) {
        RentalOrder order = rentalOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        order.setStatus(status);
        return rentalOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public RentalOrder getOrderById(Long orderId) {
        RentalOrder order = rentalOrderRepository.findDetailedById(orderId).orElse(null);
        
        if (order != null) {
            Hibernate.initialize(order.getCustomer());
            Hibernate.initialize(order.getCar());
            if (order.getCar() != null) {
                Hibernate.initialize(order.getCar().getBrand());
            }
        }
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalOrder> getOrdersByCustomer(Long customerId) {
        return rentalOrderRepository.findByCustomerUserId(customerId);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long customerId, String reason) {
        RentalOrder order = getOrderById(orderId);
        if (order == null || !order.getCustomer().getUserId().equals(customerId)) {
            throw new RuntimeException("Không tìm thấy đơn hàng hoặc không có quyền");
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT && order.getStatus() != OrderStatus.Pending) {
            throw new RuntimeException("Không thể hủy đơn hàng đã được xác nhận");
        }

        order.setStatus(OrderStatus.Cancelled);
        order.setCancelReason(reason);
        rentalOrderRepository.save(order);
    }
}