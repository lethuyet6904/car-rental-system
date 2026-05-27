package com.carrental.service;

import com.carrental.dto.response.CarDetailResponse;
import com.carrental.dto.response.CarListResponse;
import com.carrental.entity.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {

    // Gộp getActiveCars, getActiveCarsByRegion, và searchActiveCarsByCity
    Page<CarListResponse> searchActiveCars(String city, Long regionId, String fuel, String transmission, Pageable pageable);

    // Hàm lấy chi tiết xe giữ nguyên (trả về DTO)
    CarDetailResponse getCarById(Long carId);
    
    // THÊM METHOD MỚI - lấy entity Car (dùng cho Booking)
    Car getCarEntityById(Long carId);
}