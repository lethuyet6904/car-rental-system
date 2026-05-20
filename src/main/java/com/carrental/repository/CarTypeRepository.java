package com.carrental.repository;

import com.carrental.entity.CarType;
import com.carrental.enums.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarTypeRepository extends JpaRepository<CarType, Long> {
    List<CarType> findByStatus(CategoryStatus status);
    boolean existsByTypeName(String typeName);
}