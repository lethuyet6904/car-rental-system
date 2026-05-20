package com.carrental.repository;

import com.carrental.entity.Region;
import com.carrental.enums.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    List<Region> findByStatus(CategoryStatus status);
    boolean existsByRegionName(String regionName);
}