package com.carrental.service.impl;

import com.carrental.dto.response.AdminCarDetailResponse;
import com.carrental.dto.response.AdminCarListResponse;
import com.carrental.entity.Car;
import com.carrental.enums.CarStatus;
import com.carrental.repository.CarImageRepository;
import com.carrental.repository.CarRepository;
import com.carrental.service.AdminCarService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCarServiceImpl implements AdminCarService {

    private final CarRepository carRepository;
    private final CarImageRepository carImageRepository;

    @Override
    public Page<AdminCarListResponse> getCarList(CarStatus status, String keyword, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return carRepository.findByFilters(status, kw, pageable)
                .map(car -> {
                    String firstImage = carImageRepository
                            .findByCarCarIdOrderBySortOrderAsc(car.getCarId())
                            .stream().findFirst()
                            .map(img -> img.getImageUrl())
                            .orElse(null);
                    return AdminCarListResponse.from(car, firstImage);
                });
    }

    @Override
    public AdminCarDetailResponse getCarDetail(Long carId) {
        Car car = carRepository.findWithDetailsById(carId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy xe carId=" + carId));

        List<String> images = carImageRepository
                .findByCarCarIdOrderBySortOrderAsc(car.getCarId())
                .stream()
                .map(img -> img.getImageUrl())
                .toList();

        return AdminCarDetailResponse.from(car, images);
    }

    @Override
    @Transactional
    public void approve(Long carId) {
        Car car = findCarOrThrow(carId);

        if (!CarStatus.Pending.equals(car.getStatus())) {
            throw new IllegalStateException("Xe không ở trạng thái chờ duyệt");
        }

        car.setStatus(CarStatus.Active);
        carRepository.save(car);
    }

    @Override
    @Transactional
    public void reject(Long carId, String rejectReason) {
        Car car = findCarOrThrow(carId);

        if (!CarStatus.Pending.equals(car.getStatus())) {
            throw new IllegalStateException("Xe không ở trạng thái chờ duyệt");
        }

        car.setStatus(CarStatus.Rejected);
        car.setRejectReason(rejectReason);
        carRepository.save(car);
    }

    private Car findCarOrThrow(Long carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy xe carId=" + carId));
    }
}