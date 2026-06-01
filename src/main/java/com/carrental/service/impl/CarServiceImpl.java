package com.carrental.service.impl;

import com.carrental.dto.response.CarDetailResponse;
import com.carrental.dto.response.CarListResponse;
import com.carrental.entity.Brand;
import com.carrental.entity.Car;
import com.carrental.entity.CarImage;
import com.carrental.entity.CarType;
import com.carrental.enums.CarStatus;
import com.carrental.enums.CategoryStatus;
import com.carrental.repository.BrandRepository;
import com.carrental.repository.CarImageRepository;
import com.carrental.repository.CarRepository;
import com.carrental.repository.CarSpecificationRepository;
import com.carrental.repository.CarTypeRepository;
import com.carrental.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarImageRepository carImageRepository;
    private final BrandRepository brandRepository;
    private final CarTypeRepository carTypeRepository;

    /**
     * Tìm kiếm xe đang hoạt động (status = Active).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CarListResponse> searchActiveCars(String city, Long regionId, String fuelStr, String transmissionStr,
            Pageable pageable) {

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

        Page<Car> carPage = carRepository.searchCars(CarStatus.Active, city, regionId, fuel, transmission, null, pageable);

        if (carPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> carIds = carPage.getContent().stream()
                .map(Car::getCarId)
                .toList();

        List<CarImage> allImages = carImageRepository.findByCarCarIdIn(carIds);

        Map<Long, List<CarImage>> imagesByCarId = allImages.stream()
                .collect(Collectors.groupingBy(img -> img.getCar().getCarId()));

        List<CarListResponse> responses = carPage.getContent().stream()
                .map(car -> CarListResponse.from(
                        car,
                        imagesByCarId.getOrDefault(car.getCarId(), Collections.emptyList())))
                .toList();

        return new PageImpl<>(responses, pageable, carPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CarListResponse> searchActiveCarsWithFilter(
            String city,
            LocalDate dateFrom, LocalDate dateTo,
            Long brandId, Long carTypeId,
            String fuel, String transmission,
            Integer seats,
            Boolean deliveryAvailable,
            String sortBy,
            Pageable pageable) {

        Sort sort = switch (sortBy == null ? "" : sortBy) {
            case "price_asc" -> Sort.by("pricePerDay").ascending();
            case "price_desc" -> Sort.by("pricePerDay").descending();
            case "rating" -> Sort.by(
                    Sort.Order.desc("avgRating"),
                    Sort.Order.desc("createdAt"));
            default -> Sort.by("createdAt").descending();
        };

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), sort);

        Specification<Car> spec = Specification
                .where(CarSpecificationRepository.hasStatus(CarStatus.Active))
                .and(CarSpecificationRepository.hasCity(city))
                .and(CarSpecificationRepository.hasBrand(brandId))
                .and(CarSpecificationRepository.hasCarType(carTypeId))
                .and(CarSpecificationRepository.hasFuel(fuel))
                .and(CarSpecificationRepository.hasTransmission(transmission))
                .and(CarSpecificationRepository.hasSeats(seats))
                .and(CarSpecificationRepository.availableBetween(dateFrom, dateTo));

        Page<Car> carPage = carRepository.findAll(spec, sortedPageable);
        return mapToResponsePage(carPage, sortedPageable);
    }

    @Override
    public List<Brand> getActiveBrands() {
        return brandRepository.findByStatus(CategoryStatus.Active);
    }

    @Override
    public List<CarType> getActiveCarTypes() {
        return carTypeRepository.findByStatus(CategoryStatus.Active);
    }

    /**
     * Lấy chi tiết xe (dùng cho trang chi tiết xe)
     */
    @Override
    @Transactional(readOnly = true)
    public CarDetailResponse getCarById(Long carId) {
        Car car = carRepository.findByIdWithAssociations(carId)
                .filter(c -> c.getStatus() == CarStatus.Active)
                .orElseThrow(() -> new RuntimeException("Xe không tồn tại hoặc không còn khả dụng"));

        List<CarImage> images = carImageRepository.findByCarCarIdOrderBySortOrderAsc(carId);

        return CarDetailResponse.from(car, images);
    }

    /**
     * Lấy entity Car đầy đủ (dùng cho Booking - ĐÃ SỬA ĐỂ TRÁNH LAZY EXCEPTION)
     */
    @Override
    @Transactional(readOnly = true)
    public Car getCarEntityById(Long carId) {
        return carRepository.findByIdWithAssociations(carId)
                .filter(c -> c.getStatus() == CarStatus.Active)
                .orElse(null); // Trả về null thay vì throw exception để controller xử lý
    }

    private Page<CarListResponse> mapToResponsePage(Page<Car> carPage, Pageable pageable) {
        if (carPage.isEmpty())
            return Page.empty(pageable);

        List<Long> carIds = carPage.getContent().stream()
                .map(Car::getCarId).toList();

        List<CarImage> allImages = carImageRepository.findByCarCarIdIn(carIds);

        Map<Long, List<CarImage>> imagesByCarId = allImages.stream()
                .collect(Collectors.groupingBy(img -> img.getCar().getCarId()));

        List<CarListResponse> responses = carPage.getContent().stream()
                .map(car -> CarListResponse.from(
                        car,
                        imagesByCarId.getOrDefault(car.getCarId(), Collections.emptyList())))
                .toList();

        return new PageImpl<>(responses, pageable, carPage.getTotalElements());
    }
}
