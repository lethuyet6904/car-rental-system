package com.carrental.repository;

import com.carrental.entity.Car;
import com.carrental.entity.User;
import com.carrental.enums.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    // ✅ Fix: JOIN FETCH để tránh LazyInitializationException
    @Query("SELECT c FROM Car c " +
           "JOIN FETCH c.brand " +
           "JOIN FETCH c.carType " +
           "JOIN FETCH c.region " +
           "WHERE c.status = :status")
    List<Car> findByStatusWithDetails(@Param("status") CarStatus status);

    // ✅ Fix: Tìm theo city cũng JOIN FETCH
    @Query("SELECT c FROM Car c " +
           "JOIN FETCH c.brand " +
           "JOIN FETCH c.carType " +
           "JOIN FETCH c.region " +
           "WHERE c.status = :status " +
           "AND LOWER(c.region.regionName) LIKE LOWER(CONCAT('%', :city, '%'))")
    List<Car> findActiveCarsByCity(@Param("status") CarStatus status, @Param("city") String city);

    // ✅ Fix: Tìm theo regionId cũng JOIN FETCH
    @Query("SELECT c FROM Car c " +
           "JOIN FETCH c.brand " +
           "JOIN FETCH c.carType " +
           "JOIN FETCH c.region " +
           "WHERE c.status = :status AND c.region.regionId = :regionId")
    List<Car> findActiveCarsByRegion(@Param("status") CarStatus status, @Param("regionId") Long regionId);

    // Giữ lại các method cũ
    List<Car> findByOwner(User owner);
    long countByStatus(CarStatus status);
}