package com.carrental.dto.response;

import com.carrental.entity.Car;
import com.carrental.entity.CarImage;
import com.carrental.enums.FuelType;
import com.carrental.enums.TransmissionType;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Getter
public class CarListResponse {

    private final Long carId;
    private final String modelName;
    private final String brandName;
    private final String brandLogo;
    private final String carTypeName;
    private final String regionName;
    private final Integer seats;
    private final Integer yearOfManufacture;
    private final FuelType fuel;
    private final TransmissionType transmission;
    private final BigDecimal pricePerDay;
    private final String pickupLocation;
    private final String features;
    private final BigDecimal avgRating;
    private final String thumbnailUrl;  // ảnh bìa (sortOrder nhỏ nhất)

    public static CarListResponse from(Car car, List<CarImage> images) {
        return new CarListResponse(car, images);
    }

    private CarListResponse(Car car, List<CarImage> images) {
        this.carId             = car.getCarId();
        this.modelName         = car.getModelName();
        this.brandName         = car.getBrand().getBrandName();
        this.brandLogo         = car.getBrand().getLogo();
        this.carTypeName       = car.getCarType().getTypeName();
        this.regionName        = car.getRegion().getRegionName();
        this.seats             = car.getSeats();
        this.yearOfManufacture = car.getYearOfManufacture();
        this.fuel              = car.getFuel();
        this.transmission      = car.getTransmission();
        this.pricePerDay       = car.getPricePerDay();
        this.pickupLocation    = car.getPickupLocation();
        this.features          = car.getFeatures();
        this.avgRating         = car.getAvgRating();

        // Lấy ảnh có sortOrder nhỏ nhất làm ảnh bìa, fallback null nếu chưa có ảnh
        this.thumbnailUrl = images.stream()
                .min(Comparator.comparingInt(CarImage::getSortOrder))
                .map(CarImage::getImageUrl)
                .orElse(null);
    }
}