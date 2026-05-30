package com.carrental.dto.request;

import com.carrental.entity.Car;
import com.carrental.enums.FuelType;
import com.carrental.enums.TransmissionType;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CarRequest {

    private Long carId;

    @NotBlank(message = "Tên xe không được để trống")
    @Size(max = 100, message = "Tên xe không quá 100 ký tự")
    private String modelName;

    @NotBlank(message = "Biển số xe không được để trống")
    @Pattern(regexp = "^[0-9]{2}[A-Z]-[0-9]{4,5}$", message = "Biển số xe không hợp lệ (VD: 30A-12345)")
    private String licensePlate;

    @NotNull(message = "Hãng xe không được để trống")
    private Long brandId;

    @NotNull(message = "Loại xe không được để trống")
    private Long carTypeId;

    @NotNull(message = "Khu vực không được để trống")
    private Long regionId;

    @NotNull(message = "Số ghế không được để trống")
    @Min(value = 2, message = "Số ghế tối thiểu 2")
    @Max(value = 50, message = "Số ghế tối đa 50")
    private Integer seats;

    @NotNull(message = "Năm sản xuất không được để trống")
    @Min(value = 1990, message = "Năm sản xuất từ 1990")
    @Max(value = 2026, message = "Năm sản xuất không quá 2026")
    private Integer yearOfManufacture;

    @NotNull(message = "Nhiên liệu không được để trống")
    private FuelType fuel;

    @NotNull(message = "Hộp số không được để trống")
    private TransmissionType transmission;

    @NotNull(message = "Giá thuê không được để trống")
    @DecimalMin(value = "100000", message = "Giá thuê tối thiểu 100.000đ")
    @DecimalMax(value = "10000000", message = "Giá thuê tối đa 10.000.000đ")
    private BigDecimal pricePerDay;

    @NotBlank(message = "Địa chỉ nhận xe không được để trống")
    @Size(max = 200, message = "Địa chỉ không quá 200 ký tự")
    private String pickupLocation;

    @Size(max = 500, message = "Tiện ích không quá 500 ký tự")
    private String features;

    @Size(max = 500, message = "Mô tả không quá 500 ký tự")
    private String description;

    private List<MultipartFile> images;

    // Giấy tờ xe
    private MultipartFile registrationImage;   // Cà vẹt
    private MultipartFile inspectionImage;     // Đăng kiểm
    private MultipartFile insuranceImage;      // Bảo hiểm vật chất

    public static CarRequest from(Car car) {
        CarRequest request = new CarRequest();
        request.setCarId(car.getCarId());
        request.setModelName(car.getModelName());
        request.setLicensePlate(car.getLicensePlate());
        request.setBrandId(car.getBrand().getBrandId());
        request.setCarTypeId(car.getCarType().getCarTypeId());
        request.setRegionId(car.getRegion().getRegionId());
        request.setSeats(car.getSeats());
        request.setYearOfManufacture(car.getYearOfManufacture());
        request.setFuel(car.getFuel());
        request.setTransmission(car.getTransmission());
        request.setPricePerDay(car.getPricePerDay());
        request.setPickupLocation(car.getPickupLocation());
        request.setFeatures(car.getFeatures());
        request.setDescription(car.getDescription());
        return request;
    }
}