package com.carrental.repository;

import com.carrental.entity.Brand;
import com.carrental.enums.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findByStatus(CategoryStatus status);
    boolean existsByBrandName(String brandName);

    @Query("""
            SELECT b FROM Brand b
            WHERE (:keyword IS NULL
                   OR LOWER(b.brandName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY b.brandName ASC
            """)
    List<Brand> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT COUNT(c) FROM Car c WHERE c.brand.brandId = :brandId")
    long countCarsByBrandId(@Param("brandId") Long brandId);
}