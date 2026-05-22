package com.carrental.service.impl;

import com.carrental.dto.response.CarDetailResponse;
import com.carrental.dto.response.CarListResponse;
import com.carrental.entity.Car;
import com.carrental.entity.CarImage;
import com.carrental.enums.CarStatus;
import com.carrental.repository.CarImageRepository;
import com.carrental.repository.CarRepository;
import com.carrental.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarImageRepository carImageRepository;

    /**
     * Tìm kiếm xe đang hoạt động (status = Active).
     * Hỗ trợ lọc theo tên thành phố (city) hoặc regionId.
     * Truyền null để bỏ qua filter đó.
     *
     * Dùng batch-load ảnh để tránh N+1 query:
     *   1 query lấy danh sách xe (phân trang)
     *   1 query lấy tất cả ảnh của những xe đó
     *   Ghép kết quả trong bộ nhớ
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CarListResponse> searchActiveCars(String city, Long regionId, String fuelStr, String transmissionStr, Pageable pageable) {

        com.carrental.enums.FuelType fuel = null;
        if (fuelStr != null && !fuelStr.trim().isEmpty()) {
            try {
                fuel = com.carrental.enums.FuelType.valueOf(fuelStr);
            } catch (IllegalArgumentException e) {
                // Ignore invalid
            }
        }

        com.carrental.enums.TransmissionType transmission = null;
        if (transmissionStr != null && !transmissionStr.trim().isEmpty()) {
            try {
                transmission = com.carrental.enums.TransmissionType.valueOf(transmissionStr);
            } catch (IllegalArgumentException e) {
                // Ignore invalid
            }
        }

        // Bước 1: lấy trang xe theo điều kiện tìm kiếm
        Page<Car> carPage = carRepository.searchCars(CarStatus.Active, city, regionId, fuel, transmission, pageable);

        if (carPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // Bước 2: lấy tất cả carId trong trang này
        List<Long> carIds = carPage.getContent().stream()
                .map(Car::getCarId)
                .toList();

        // Bước 3: 1 query duy nhất lấy tất cả ảnh của những xe trên
        // (thay vì N query nếu dùng car.getCarImages() trong vòng lặp)
        List<CarImage> allImages = carImageRepository.findByCarCarIdIn(carIds);

        // Bước 4: nhóm ảnh theo carId để tra nhanh O(1)
        Map<Long, List<CarImage>> imagesByCarId = allImages.stream()
                .collect(Collectors.groupingBy(img -> img.getCar().getCarId()));

        // Bước 5: ghép xe + ảnh → DTO
        List<CarListResponse> responses = carPage.getContent().stream()
                .map(car -> CarListResponse.from(
                        car,
                        imagesByCarId.getOrDefault(car.getCarId(), Collections.emptyList())
                ))
                .toList();

        return new PageImpl<>(responses, pageable, carPage.getTotalElements());
    }

    /**
     * Lấy chi tiết 1 xe theo carId.
     * Chỉ trả xe có status = Active (xe bị ẩn/bị từ chối không hiển thị với khách).
     */
    @Override
    @Transactional(readOnly = true)
    public CarDetailResponse getCarById(Long carId) {

        Car car = carRepository.findById(carId)
                .filter(c -> c.getStatus() == CarStatus.Active)
                .orElseThrow(() -> new RuntimeException("Xe không tồn tại hoặc không còn khả dụng"));

        // Lấy ảnh đã sắp xếp theo sortOrder (ảnh đầu = ảnh bìa)
        List<CarImage> images = carImageRepository.findByCarCarIdOrderBySortOrderAsc(carId);

        return CarDetailResponse.from(car, images);
    }
}
