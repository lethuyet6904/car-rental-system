package com.carrental.repository;

import com.carrental.entity.CarImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarImageRepository extends JpaRepository<CarImage, Long> {
    List<CarImage> findByCarCarIdOrderBySortOrderAsc(Long carId);
    void deleteByCarCarId(Long carId);
    List<CarImage> findByCarCarIdIn(List<Long> carIds);
}