package com.carrental.service;

import com.carrental.entity.RentalOrder;
import com.carrental.enums.OrderStatus;
import com.carrental.enums.PickupMethod;

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
    // THÊM MỚI
    RentalOrder updateOrderStatus(Long orderId, OrderStatus status);
}	