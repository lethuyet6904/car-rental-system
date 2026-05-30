package com.carrental.dto.response;

import com.carrental.entity.Car;
import com.carrental.enums.CarStatus;
import com.carrental.enums.FuelType;
import com.carrental.enums.TransmissionType;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AdminCarDetailResponse {

    private final Long carId;
    private final String modelName;
    private final String licensePlate;
    private final String brandName;
    private final String typeName;
    private final Integer seats;
    private final Integer yearOfManufacture;
    private final FuelType fuel;
    private final TransmissionType transmission;
    private final BigDecimal pricePerDay;
    private final String regionName;
    private final String features;
    private final String description;
    private final CarStatus status;
    private final String rejectReason;
    private final LocalDateTime createdAt;

    // Ảnh xe
    private final List<String> images;

    // Chủ xe
    private final Long ownerId;
    private final String ownerName;
    private final String ownerPhone;

    // Giấy tờ xe
    private final String registrationImage;
    private final String inspectionImage;
    private final String insuranceImage;

    private AdminCarDetailResponse(Car c, List<String> images) {
        this.carId             = c.getCarId();
        this.modelName         = c.getModelName();
        this.licensePlate      = c.getLicensePlate();
        this.brandName         = c.getBrand().getBrandName();
        this.typeName          = c.getCarType().getTypeName();
        this.seats             = c.getSeats();
        this.yearOfManufacture = c.getYearOfManufacture();
        this.fuel              = c.getFuel();
        this.transmission      = c.getTransmission();
        this.pricePerDay       = c.getPricePerDay();
        this.regionName        = c.getRegion().getRegionName();
        this.features          = c.getFeatures();
        this.description       = c.getDescription();
        this.status            = c.getStatus();
        this.rejectReason      = c.getRejectReason();
        this.createdAt         = c.getCreatedAt();
        this.images            = images != null ? images : List.of();
        this.ownerId           = c.getOwner().getUserId();
        this.ownerName         = c.getOwner().getFullName();
        this.ownerPhone        = c.getOwner().getPhone();
        this.registrationImage = c.getRegistrationImage();
        this.inspectionImage   = c.getInspectionImage();
        this.insuranceImage    = c.getInsuranceImage();
    }

    public static AdminCarDetailResponse from(Car c, List<String> images) {
        return new AdminCarDetailResponse(c, images);
    }
}