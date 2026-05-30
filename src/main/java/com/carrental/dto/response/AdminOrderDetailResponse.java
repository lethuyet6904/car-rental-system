package com.carrental.dto.response;

import com.carrental.entity.Payment;
import com.carrental.entity.RentalOrder;
import com.carrental.enums.OrderStatus;
import com.carrental.enums.PaymentMethod;
import com.carrental.enums.PaymentStatus;
import com.carrental.enums.PickupMethod;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
public class AdminOrderDetailResponse {

    // Đơn thuê
    private final Long orderId;
    private final LocalDateTime createdAt;
    private final LocalDate pickupDate;
    private final LocalTime pickupTime;
    private final LocalDate returnDate;
    private final LocalTime returnTime;
    private final Integer totalDays;
    private final PickupMethod pickupMethod;
    private final String deliveryAddress;
    private final String note;
    private final OrderStatus status;
    private final LocalDateTime actualPickupTime;
    private final LocalDateTime actualReturnTime;
    private final BigDecimal remainingAmount;
    private final Boolean depositPaid;

    // Khách hàng
    private final Long customerId;
    private final String customerName;
    private final String customerPhone;

    // Xe
    private final Long carId;
    private final String carName;
    private final String licensePlate;
    private final List<String> carImages;

    // Ảnh giao nhận xe (Rental Images)
    private final List<String> pickupImages;
    private final List<String> returnImages;

    // Chủ xe
    private final Long ownerId;
    private final String ownerName;
    private final String ownerPhone;

    // Thanh toán
    private final BigDecimal pricePerDay;
    private final BigDecimal depositAmount;
    private final BigDecimal totalAmount;
    private final PaymentMethod paymentMethod;
    private final PaymentStatus paymentStatus;

    // Đánh giá (nullable)
    private final Byte rating;
    private final String reviewComment;

    private AdminOrderDetailResponse(RentalOrder o, List<String> carImages,
                                     Payment deposit, Byte rating, String reviewComment,
                                     List<String> pickupImages, List<String> returnImages) {
        this.orderId          = o.getOrderId();
        this.createdAt        = o.getCreatedAt();
        this.pickupDate       = o.getPickupDate();
        this.pickupTime       = o.getPickupTime();
        this.returnDate       = o.getReturnDate();
        this.returnTime       = o.getReturnTime();
        this.totalDays        = o.getTotalDays();
        this.pickupMethod     = o.getPickupMethod();
        this.deliveryAddress  = o.getDeliveryAddress();
        this.note             = o.getNote();
        this.status           = o.getStatus();
        this.actualPickupTime = o.getActualPickupTime();
        this.actualReturnTime = o.getActualReturnTime();

        this.customerId   = o.getCustomer().getUserId();
        this.customerName = o.getCustomer().getFullName();
        this.customerPhone= o.getCustomer().getPhone();

        this.carId        = o.getCar().getCarId();
        this.carName      = o.getCar().getModelName();
        this.licensePlate = o.getCar().getLicensePlate();
        this.carImages    = carImages != null ? carImages : List.of();

        this.ownerId      = o.getCar().getOwner().getUserId();
        this.ownerName    = o.getCar().getOwner().getFullName();
        this.ownerPhone   = o.getCar().getOwner().getPhone();

        this.pricePerDay  = o.getCar().getPricePerDay();
        this.depositAmount= o.getDepositAmount();
        this.totalAmount  = o.getTotalAmount();

        if (deposit != null) {
            this.paymentMethod = deposit.getPaymentMethod();
            this.paymentStatus = deposit.getStatus();
        } else {
            this.paymentMethod = null;
            this.paymentStatus = null;
        }
        
        this.remainingAmount = o.getTotalAmount().subtract(o.getDepositAmount());
        this.depositPaid     = deposit != null ? deposit.getIsPaid() : false;
        this.rating        = rating;
        this.reviewComment = reviewComment;
        this.pickupImages  = pickupImages != null ? pickupImages : List.of();
        this.returnImages  = returnImages != null ? returnImages : List.of();
    }

    public static AdminOrderDetailResponse from(RentalOrder o, List<String> carImages,
                                                Payment deposit, Byte rating,
                                                String reviewComment,
                                                List<String> pickupImages, List<String> returnImages) {
        return new AdminOrderDetailResponse(o, carImages, deposit, rating, reviewComment, pickupImages, returnImages);
    }
}