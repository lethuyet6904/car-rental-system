package com.carrental.service;

import com.carrental.dto.response.AdminCarDetailResponse;
import com.carrental.dto.response.AdminCarListResponse;
import com.carrental.enums.CarStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCarService {

    Page<AdminCarListResponse> getCarList(CarStatus status, String keyword, Pageable pageable);

    AdminCarDetailResponse getCarDetail(Long carId);

    void approve(Long carId);

    void reject(Long carId, String rejectReason);
}