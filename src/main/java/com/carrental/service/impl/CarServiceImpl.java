package com.carrental.service.impl;

import com.carrental.dto.response.CarListResponse;
import com.carrental.entity.Car;
import com.carrental.entity.CarImage;
import com.carrental.enums.CarStatus;
import com.carrental.repository.CarImageRepository;
import com.carrental.repository.CarRepository;
import com.carrental.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarImageRepository carImageRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CarListResponse> getActiveCars() {
        List<Car> cars = carRepository.findByStatusWithDetails(CarStatus.Active);
        return toResponseList(cars);
    }

    @Override
    @Transactional(readOnly = true)
    public CarListResponse getCarById(Long carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe với id: " + carId));
        List<CarImage> images = carImageRepository.findByCarCarIdOrderBySortOrderAsc(carId);
        return CarListResponse.from(car, images);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarListResponse> getActiveCarsByRegion(Long regionId) {
        List<Car> cars = carRepository.findActiveCarsByRegion(CarStatus.Active, regionId);
        return toResponseList(cars);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarListResponse> searchActiveCarsByCity(String city) {
        List<Car> cars = carRepository.findActiveCarsByCity(CarStatus.Active, city);
        return toResponseList(cars);
    }

    private List<CarListResponse> toResponseList(List<Car> cars) {
        if (cars.isEmpty()) return List.of();

        List<Long> carIds = cars.stream().map(Car::getCarId).toList();
        List<CarImage> allImages = carImageRepository.findByCarCarIdIn(carIds);

        Map<Long, List<CarImage>> imagesByCarId = allImages.stream()
                .collect(Collectors.groupingBy(img -> img.getCar().getCarId()));

        return cars.stream()
                .map(car -> CarListResponse.from(
                        car,
                        imagesByCarId.getOrDefault(car.getCarId(), List.of())
                ))
                .toList();
    }
}