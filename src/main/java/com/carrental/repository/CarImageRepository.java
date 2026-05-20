package com.carrental.repository;

import com.carrental.entity.CarImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarImageRepository extends JpaRepository<CarImage, Long> {

    // Lấy tất cả ảnh của một xe, sắp xếp theo sortOrder tăng dần
    List<CarImage> findByCarCarIdOrderBySortOrderAsc(Long carId);

    // Lấy ảnh của nhiều xe cùng lúc — dùng khi load danh sách để tránh N+1
    List<CarImage> findByCarCarIdIn(List<Long> carIds);
}