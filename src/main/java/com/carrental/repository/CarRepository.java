package com.carrental.repository;

import com.carrental.entity.Car;
import com.carrental.entity.User;
import com.carrental.enums.CarStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    @EntityGraph(attributePaths = {"brand", "carType", "region"})
    @Query("""
            SELECT c FROM Car c
            WHERE c.status = :status
              AND (:city IS NULL OR LOWER(c.region.regionName) LIKE LOWER(CONCAT('%', :city, '%')))
              AND (:regionId IS NULL OR c.region.regionId = :regionId)
              AND (:fuel IS NULL OR c.fuel = :fuel)
              AND (:transmission IS NULL OR c.transmission = :transmission)
            """)
    Page<Car> searchCars(
            @Param("status") CarStatus status,
            @Param("city") String city,
            @Param("regionId") Long regionId,
            @Param("fuel") com.carrental.enums.FuelType fuel,
            @Param("transmission") com.carrental.enums.TransmissionType transmission,
            Pageable pageable);

    List<Car> findByOwner(User owner);
    
    long countByStatus(CarStatus status);
}