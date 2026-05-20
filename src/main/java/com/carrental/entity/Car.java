package com.carrental.entity;

import com.carrental.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Car")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carId")
    private Long carId;

    // FK → User (chủ xe) - Đã sửa thành EAGER
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ownerId", nullable = false)
    private User owner;

    // FK → Brand - Đã sửa thành EAGER
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brandId", nullable = false)
    private Brand brand;

    // FK → CarType - Đã sửa thành EAGER
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "carTypeId", nullable = false)
    private CarType carType;

    // FK → Region - Đã sửa thành EAGER
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "regionId", nullable = false)
    private Region region;

    @Column(name = "modelName", nullable = false, length = 100)
    private String modelName;

    @Column(name = "licensePlate", nullable = false, length = 20)
    private String licensePlate;

    @Column(name = "seats", nullable = false)
    private Integer seats;

    @Column(name = "yearOfManufacture", nullable = false)
    private Integer yearOfManufacture;

    // varchar(20) CHECK: Gasoline/Electric/Diesel
    @Enumerated(EnumType.STRING)
    @Column(name = "fuel", nullable = false, length = 20)
    private FuelType fuel;

    // varchar(20) CHECK: Automatic/Manual
    @Enumerated(EnumType.STRING)
    @Column(name = "transmission", nullable = false, length = 20)
    private TransmissionType transmission;

    // decimal(12,0) — dùng BigDecimal để tránh mất dữ liệu khi xử lý tiền
    @Column(name = "pricePerDay", nullable = false, precision = 12, scale = 0)
    private BigDecimal pricePerDay;

    @Column(name = "pickupLocation", nullable = false, length = 200)
    private String pickupLocation;

    // varchar(500) NULL — danh sách tính năng, lưu dạng "GPS,Camera360,Sunroof"
    @Column(name = "features", length = 500)
    private String features;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CarStatus status;

    @Column(name = "rejectReason", length = 300)
    private String rejectReason;

    // decimal(2,1) — ví dụ: 4.5, 3.0
    @Column(name = "avgRating", nullable = false, precision = 2, scale = 1)
    private BigDecimal avgRating;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    // Thêm relationship với CarImage (một xe có nhiều ảnh)
    @OneToMany(mappedBy = "car", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CarImage> carImages = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = CarStatus.Pending;
        // Khởi tạo avgRating = 0.0 khi chưa có review
        if (this.avgRating == null) this.avgRating = BigDecimal.ZERO;
    }
}