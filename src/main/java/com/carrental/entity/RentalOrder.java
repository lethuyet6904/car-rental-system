package com.carrental.entity;

import com.carrental.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "RentalOrder")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RentalOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderId")
    private Long orderId;

    // FK → User (người thuê)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId", nullable = false)
    private User customer;

    // FK → Car
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carId", nullable = false)
    private Car car;

    // Ngày giờ nhận xe (tách date và time vì SQL schema dùng DATE + TIME riêng)
    @Column(name = "pickupDate", nullable = false)
    private LocalDate pickupDate;

    @Column(name = "pickupTime", nullable = false)
    private LocalTime pickupTime;

    // Ngày giờ trả xe
    @Column(name = "returnDate", nullable = false)
    private LocalDate returnDate;

    @Column(name = "returnTime", nullable = false)
    private LocalTime returnTime;

    @Column(name = "totalDays", nullable = false)
    private Integer totalDays;

    // decimal(12,0)
    @Column(name = "totalAmount", nullable = false, precision = 12, scale = 0)
    private BigDecimal totalAmount;

    @Column(name = "depositAmount", nullable = false, precision = 12, scale = 0)
    private BigDecimal depositAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "pickupMethod", nullable = false, length = 20)
    private PickupMethod pickupMethod;

    // nvarchar(200) NULL — địa chỉ giao xe nếu Delivery
    @Column(name = "deliveryAddress", length = 200)
    private String deliveryAddress;

    @Column(name = "note", length = 300)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "cancelReason", length = 300)
    private String cancelReason;

    // Thời điểm thực tế nhận xe (khác pickupDate vì có thể trễ)
    @Column(name = "actualPickupTime")
    private LocalDateTime actualPickupTime;

    // Thời điểm thực tế trả xe
    @Column(name = "actualReturnTime")
    private LocalDateTime actualReturnTime;

    // Ghi chú checklist lúc nhận xe
    @Column(name = "pickupChecklistNote", length = 500)
    private String pickupChecklistNote;

    // Ghi chú checklist lúc trả xe
    @Column(name = "returnChecklistNote", length = 500)
    private String returnChecklistNote;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = OrderStatus.Pending;
    }
}