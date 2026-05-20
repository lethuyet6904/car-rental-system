package com.carrental.repository;

import com.carrental.entity.CarSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CarScheduleRepository extends JpaRepository<CarSchedule, Long> {

    List<CarSchedule> findByCarCarId(Long carId);

    // Kiểm tra xe có bị block trong khoảng ngày không
    // Dùng khi Customer đặt xe — tránh double booking
    @Query("""
        SELECT COUNT(s) > 0 FROM CarSchedule s
        WHERE s.car.carId = :carId
        AND s.startDate <= :endDate
        AND s.endDate >= :startDate
    """)
    boolean existsConflict(Long carId, LocalDate startDate, LocalDate endDate);
}