package com.carrental.repository;

import com.carrental.entity.Car;
import com.carrental.entity.User;
import com.carrental.enums.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    // Admin: xem xe đang chờ duyệt
    List<Car> findByStatus(CarStatus status);

    // Owner: xem xe của mình
    List<Car> findByOwner(User owner);

    // Admin: đếm xe theo status — dùng cho dashboard
    long countByStatus(CarStatus status);

    // Guest: tìm xe active theo region
    @Query("SELECT c FROM Car c WHERE c.status = 'Active' AND c.region.regionId = :regionId")
    List<Car> findActiveCarsByRegion(Long regionId);
}