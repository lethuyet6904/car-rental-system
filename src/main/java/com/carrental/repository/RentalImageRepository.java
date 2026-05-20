package com.carrental.repository;

import com.carrental.entity.RentalImage;
import com.carrental.enums.RentalImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RentalImageRepository extends JpaRepository<RentalImage, Long> {
    List<RentalImage> findByRentalOrderOrderIdAndImageType(Long orderId, RentalImageType imageType);
}