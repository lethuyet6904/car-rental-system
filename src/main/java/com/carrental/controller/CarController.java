package com.carrental.controller;

import com.carrental.entity.Car;
import com.carrental.entity.Region;
import com.carrental.enums.CarStatus;
import com.carrental.enums.CategoryStatus;
import com.carrental.repository.CarRepository;
import com.carrental.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarRepository carRepository;
    private final RegionRepository regionRepository;

    @GetMapping
    public String listCars(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            Model model) {
        
        log.info("========== CAR CONTROLLER DEBUG ==========");
        
        // Cách 1: Lấy tất cả xe không có điều kiện để test
        List<Car> allCars = carRepository.findAll();
        log.info("Tổng số xe trong database: {}", allCars.size());
        
        // Cách 2: Lọc xe Active
        List<Car> activeCars = carRepository.findByStatus(CarStatus.Active);
        log.info("Số xe có status ACTIVE: {}", activeCars.size());
        
        // In chi tiết từng xe
        for (Car car : activeCars) {
            log.info("Xe: id={}, model={}, brand={}, status={}, location={}", 
                car.getCarId(),
                car.getModelName(),
                car.getBrand() != null ? car.getBrand().getBrandName() : "NULL",
                car.getStatus(),
                car.getPickupLocation());
        }
        
        // Lọc theo thành phố nếu có
        if (city != null && !city.isEmpty()) {
            activeCars = activeCars.stream()
                    .filter(car -> car.getPickupLocation() != null && 
                                   car.getPickupLocation().toLowerCase().contains(city.toLowerCase()))
                    .toList();
            log.info("Sau khi lọc theo city '{}': {} xe", city, activeCars.size());
        }
        
        // Lấy danh sách khu vực
        List<Region> regions = regionRepository.findByStatus(CategoryStatus.Active);
        log.info("Số khu vực active: {}", regions.size());
        
        log.info("==========================================");
        
        model.addAttribute("cars", activeCars);
        model.addAttribute("regions", regions);
        model.addAttribute("selectedCity", city);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        
        return "pages/cars/list";
    }
    
    @GetMapping("/{carId}")
    public String carDetail(@PathVariable Long carId, Model model) {
        log.info("Chi tiết xe ID: {}", carId);
        
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe với ID: " + carId));
        
        log.info("Tìm thấy xe: {} - {}", car.getCarId(), car.getModelName());
        
        model.addAttribute("car", car);
        return "pages/cars/detail";
    }
    
    // API test để kiểm tra dữ liệu
    @GetMapping("/test")
    @ResponseBody
    public List<Car> testApi() {
        log.info("API test được gọi");
        return carRepository.findAll();
    }
    
    // API test đếm số xe
    @GetMapping("/count")
    @ResponseBody
    public String count() {
        long total = carRepository.count();
        long active = carRepository.findByStatus(CarStatus.Active).size();
        return "Tổng số xe: " + total + " | Số xe Active: " + active;
    }
}