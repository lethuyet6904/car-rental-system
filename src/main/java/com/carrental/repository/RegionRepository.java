package com.carrental.repository;

import com.carrental.entity.Region;
import com.carrental.enums.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    List<Region> findByStatus(CategoryStatus status);
    boolean existsByRegionName(String regionName);

    @Query("""
            SELECT r FROM Region r
            WHERE (:keyword IS NULL
                   OR LOWER(r.regionName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY r.regionName ASC
            """)
    List<Region> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT COUNT(c) FROM Car c WHERE c.region.regionId = :regionId AND c.status = 'Active'")
    long countActiveCarsByRegionId(@Param("regionId") Long regionId);
}