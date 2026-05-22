package com.carrental.service;

import com.carrental.dto.response.CarDetailResponse;
import com.carrental.dto.response.CarListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {

    // Gộp getActiveCars, getActiveCarsByRegion, và searchActiveCarsByCity
    Page<CarListResponse> searchActiveCars(String city, Long regionId, String fuel, String transmission, Pageable pageable);

    // Hàm lấy chi tiết xe giữ nguyên
    CarDetailResponse getCarById(Long carId);
}