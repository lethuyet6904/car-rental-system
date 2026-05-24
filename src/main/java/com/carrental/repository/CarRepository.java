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
import java.util.Optional;

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
    
 // Admin filter theo status + keyword (tên xe, biển số, tên chủ xe)
    @EntityGraph(attributePaths = {"owner", "brand", "carType", "region"})
    @Query("""
            SELECT c FROM Car c
            WHERE (:status  IS NULL OR c.status = :status)
              AND (:keyword IS NULL
                   OR LOWER(c.modelName)    LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.owner.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Car> findByFilters(
            @Param("status")  CarStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    // Lấy detail kèm đầy đủ relations
    @Query("SELECT c FROM Car c JOIN FETCH c.owner JOIN FETCH c.brand JOIN FETCH c.carType JOIN FETCH c.region WHERE c.carId = :carId")
    Optional<Car> findWithDetailsById(@Param("carId") Long carId);
    
    List<Car> findByOwner(User owner);
    
    long countByStatus(CarStatus status);
}