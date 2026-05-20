package com.carrental.repository;

import com.carrental.entity.Brand;
import com.carrental.enums.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findByStatus(CategoryStatus status);
    boolean existsByBrandName(String brandName);
}