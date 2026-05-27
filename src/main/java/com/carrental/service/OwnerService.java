package com.carrental.service;

import com.carrental.dto.request.CarRequest;
import com.carrental.entity.Brand;
import com.carrental.entity.Car;
import com.carrental.entity.CarType;
import com.carrental.entity.RentalOrder;
import com.carrental.entity.Region;
import com.carrental.enums.CarStatus;
import com.carrental.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OwnerService {

    // ===== Thống kê =====
    long countCarsByOwner(Long ownerId);
    long countCarsByOwnerAndStatus(Long ownerId, CarStatus status);
    long countOrdersByOwnerAndStatus(Long ownerId, OrderStatus status);
    long getTotalRevenueByOwner(Long ownerId);
    List<RentalOrder> getRecentOrdersByOwner(Long ownerId, int i);

    // ===== Quản lý xe =====
    Page<Car> getCarsByOwner(Long ownerId, String status, Pageable pageable);
    Car getCarByIdAndOwner(Long carId, Long ownerId);
    Car createCar(Long ownerId, CarRequest request);
    Car updateCar(Long carId, Long ownerId, CarRequest request);
    void toggleCarStatus(Long carId, Long ownerId);
    void deleteCar(Long carId, Long ownerId);

    // ===== Quản lý đơn hàng =====
    Page<RentalOrder> getOrdersByOwner(Long ownerId, OrderStatus status, Pageable pageable);
    RentalOrder getOrderByIdAndOwner(Long orderId, Long ownerId);
    void confirmOrder(Long orderId, Long ownerId);
    void rejectOrder(Long orderId, Long ownerId, String reason);
    void completeOrder(Long orderId, Long ownearId);

    // ===== Dữ liệu danh mục =====
    List<Brand> getAllActiveBrands();
    List<CarType> getAllActiveCarTypes();
    List<Region> getAllActiveRegions();
    
    
}