package com.carrental.service;

import com.carrental.dto.response.CarDetailResponse;
import com.carrental.dto.response.CarListResponse;
import com.carrental.entity.Brand;
import com.carrental.entity.Car;
import com.carrental.entity.CarType;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {

    // Gộp getActiveCars, getActiveCarsByRegion, và searchActiveCarsByCity
    Page<CarListResponse> searchActiveCars(String city, Long regionId, String fuel, String transmission,
            Pageable pageable);

    Page<CarListResponse> searchActiveCarsWithFilter(
            String city,
            LocalDate dateFrom, LocalDate dateTo,
            Long brandId, Long carTypeId,
            String fuel, String transmission,
            Integer seats,
            Boolean deliveryAvailable,
            String sortBy,
            Pageable pageable);

    // Lấy danh sách cho dropdown
    List<Brand> getActiveBrands();

    List<CarType> getActiveCarTypes();

    // Hàm lấy chi tiết xe giữ nguyên (trả về DTO)
    CarDetailResponse getCarById(Long carId);

    // THÊM METHOD MỚI - lấy entity Car (dùng cho Booking)
    Car getCarEntityById(Long carId);
}
