package com.carrental.dto.response;

import com.carrental.entity.Car;
import com.carrental.enums.CarStatus;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class AdminCarListResponse {

    private final Long carId;
    private final String modelName;
    private final String licensePlate;
    private final String brandName;
    private final String typeName;
    private final String ownerName;
    private final String firstImageUrl;   // set riêng sau khi query CarImage
    private final LocalDateTime createdAt;
    private final CarStatus status;

    private AdminCarListResponse(Car c, String firstImageUrl) {
        this.carId        = c.getCarId();
        this.modelName    = c.getModelName();
        this.licensePlate = c.getLicensePlate();
        this.brandName    = c.getBrand().getBrandName();
        this.typeName     = c.getCarType().getTypeName();
        this.ownerName    = c.getOwner().getFullName();
        this.firstImageUrl = firstImageUrl;
        this.createdAt    = c.getCreatedAt();
        this.status       = c.getStatus();
    }

    public static AdminCarListResponse from(Car c, String firstImageUrl) {
        return new AdminCarListResponse(c, firstImageUrl);
    }

    // Overload khi chưa có ảnh
    public static AdminCarListResponse from(Car c) {
        return new AdminCarListResponse(c, null);
    }
}