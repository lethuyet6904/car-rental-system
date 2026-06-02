package com.carrental.service.impl;

import com.carrental.entity.Car;
import com.carrental.entity.RentalOrder;
import com.carrental.entity.User;
import com.carrental.enums.CarStatus;
import com.carrental.enums.OrderStatus;
import com.carrental.enums.PickupMethod;
import com.carrental.repository.RentalOrderRepository;
import com.carrental.repository.UserRepository;
import com.carrental.entity.CarSchedule;
import com.carrental.enums.ScheduleType;
import com.carrental.repository.CarScheduleRepository;
import com.carrental.service.BookingService;
import com.carrental.service.CarService;
import com.carrental.service.PaymentService;
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
    private final CarScheduleRepository carScheduleRepository;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public RentalOrder createOrder(Long customerId, Long carId,
            LocalDate pickupDate, LocalTime pickupTime,
            LocalDate returnDate, LocalTime returnTime,
            PickupMethod pickupMethod,
            String deliveryAddress, String note) {

        if (!pickupDate.isAfter(LocalDate.now()))
            throw new RuntimeException("Ngày nhận xe phải từ ngày mai trở đi");
            
        if (pickupMethod == PickupMethod.Delivery && (deliveryAddress == null || deliveryAddress.isBlank()))
            throw new RuntimeException("Vui lòng nhập địa chỉ giao xe");

        if (carScheduleRepository.existsConflict(carId, pickupDate, returnDate))
            throw new RuntimeException("Xe đã được đặt trong khoảng thời gian này");

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        Car car = carService.getCarEntityById(carId);
        if (car == null)
            throw new RuntimeException("Không tìm thấy xe");
        if (car.getStatus() != CarStatus.Active)
            throw new RuntimeException("Xe hiện không khả dụng để thuê");

        if (car.getOwner().getUserId().equals(customerId)) {
            throw new RuntimeException("Bạn không thể đặt xe của chính mình");
        }

        long days = ChronoUnit.DAYS.between(pickupDate, returnDate);
        if (days <= 0)
            throw new RuntimeException("Ngày trả xe phải sau ngày nhận xe ít nhất 1 ngày");

        BigDecimal totalAmount = car.getPricePerDay().multiply(BigDecimal.valueOf(days));
        BigDecimal deposit = totalAmount.multiply(BigDecimal.valueOf(0.3));

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
        order.setStatus(OrderStatus.Pending);
        order.setCreatedAt(LocalDateTime.now());

        RentalOrder savedOrder = rentalOrderRepository.save(order);
        
        CarSchedule schedule = CarSchedule.builder()
            .car(car)
            .startDate(pickupDate)
            .endDate(returnDate)
            .scheduleType(ScheduleType.OrderBooked)
            .rentalOrder(savedOrder)
            .build();
        carScheduleRepository.save(schedule);
        
        return savedOrder;
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
                Hibernate.initialize(order.getCar().getOwner());
            }
        }
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalOrder> getOrdersByCustomer(Long customerId) {
        return rentalOrderRepository.findByCustomerUserIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long customerId, String reason) {
        RentalOrder order = getOrderById(orderId);
        if (order == null || !order.getCustomer().getUserId().equals(customerId)) {
            throw new RuntimeException("Không tìm thấy đơn hàng hoặc không có quyền");
        }

        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == OrderStatus.InProgress
                || currentStatus == OrderStatus.Completed
                || currentStatus == OrderStatus.Cancelled
                || currentStatus == OrderStatus.Rejected) {
            throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái hiện tại");
        }

        RefundPolicy policy = getRefundPolicy(orderId);
        paymentService.processRefund(orderId, policy.getRefundPercent(), null);

        order.setStatus(OrderStatus.Cancelled);
        order.setCancelReason(reason);
        rentalOrderRepository.save(order);

        carScheduleRepository.findByRentalOrderOrderId(orderId)
            .ifPresent(carScheduleRepository::delete);
    }

    @Override
    public RefundPolicy getRefundPolicy(Long orderId) {
        RentalOrder order = getOrderById(orderId);
        if (order == null)
            throw new RuntimeException("Không tìm thấy đơn hàng");

        // Chưa cọc → không cần hoàn
        if (order.getStatus() == OrderStatus.Pending) {
            return new RefundPolicy(0, "Chưa thanh toán cọc, không cần hoàn tiền");
        }

        long daysUntilPickup = ChronoUnit.DAYS.between(LocalDate.now(), order.getPickupDate());

        if (daysUntilPickup >= 3) {
            return new RefundPolicy(100, "Hủy trước 3 ngày — hoàn 100% cọc");
        } else if (daysUntilPickup >= 1) {
            return new RefundPolicy(50, "Hủy trước 1-2 ngày — hoàn 50% cọc");
        } else {
            return new RefundPolicy(0, "Hủy trong vòng 24h — mất cọc");
        }
    }
}
