package com.carrental.dto.response;

import com.carrental.entity.Car;
import com.carrental.entity.CarImage;
import com.carrental.enums.FuelType;
import com.carrental.enums.TransmissionType;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CarDetailResponse {

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
    private final String description;
    private final BigDecimal avgRating;
    private final List<String> imageUrls;
    
    // Thuộc tính phụ có thể có trong Entity, ta lấy nếu có
    // Dựa vào DB thiết kế, ta lấy các field cần thiết cho Detail view

    public static CarDetailResponse from(Car car, List<CarImage> images) {
        return new CarDetailResponse(car, images);
    }

    private CarDetailResponse(Car car, List<CarImage> images) {
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
        this.description       = car.getDescription();
        this.avgRating         = car.getAvgRating();

        this.imageUrls = images.stream()
                .sorted(Comparator.comparingInt(CarImage::getSortOrder))
                .map(CarImage::getImageUrl)
                .map(url -> url.startsWith("http") || url.startsWith("/") ? url : "/assets/images/cars/" + url)
                .collect(Collectors.toList());
    }
}
