package com.carrental.repository;

import com.carrental.entity.CarType;
import com.carrental.enums.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarTypeRepository extends JpaRepository<CarType, Long> {
    List<CarType> findByStatus(CategoryStatus status);
    boolean existsByTypeName(String typeName);

    @Query("""
            SELECT t FROM CarType t
            WHERE (:keyword IS NULL
                   OR LOWER(t.typeName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY t.typeName ASC
            """)
    List<CarType> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT COUNT(c) FROM Car c WHERE c.carType.carTypeId = :typeId")
    long countCarsByTypeId(@Param("typeId") Long typeId);
}