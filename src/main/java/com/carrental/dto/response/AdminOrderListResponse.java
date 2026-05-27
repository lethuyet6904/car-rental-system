package com.carrental.dto.response;

import com.carrental.entity.RentalOrder;
import com.carrental.enums.OrderStatus;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class AdminOrderListResponse {

    private final Long orderId;
    private final String carName;
    private final String firstImageUrl;
    private final String customerName;
    private final LocalDate pickupDate;
    private final LocalDate returnDate;
    private final BigDecimal totalAmount;
    private final OrderStatus status;
    // Review (nullable)
    private final Byte rating;

    private AdminOrderListResponse(RentalOrder o, String firstImageUrl, Byte rating) {
        this.orderId       = o.getOrderId();
        this.carName       = o.getCar().getModelName();
        this.firstImageUrl = firstImageUrl;
        this.customerName  = o.getCustomer().getFullName();
        this.pickupDate    = o.getPickupDate();
        this.returnDate    = o.getReturnDate();
        this.totalAmount   = o.getTotalAmount();
        this.status        = o.getStatus();
        this.rating        = rating;
    }

    public static AdminOrderListResponse from(RentalOrder o, String firstImageUrl, Byte rating) {
        return new AdminOrderListResponse(o, firstImageUrl, rating);
    }
}