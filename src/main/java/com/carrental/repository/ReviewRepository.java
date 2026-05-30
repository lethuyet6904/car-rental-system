package com.carrental.repository;

import com.carrental.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Xem review của 1 xe (join qua RentalOrder)
    @Query("SELECT r FROM Review r JOIN FETCH r.customer WHERE r.rentalOrder.car.carId = :carId")
    List<Review> findByCarId(Long carId);

    // Kiểm tra đơn đã được review chưa — mỗi đơn chỉ review 1 lần
    Optional<Review> findByRentalOrderOrderId(Long orderId);

    // Tính avgRating của xe — gọi sau khi có review mới
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r WHERE r.rentalOrder.car.carId = :carId")
    Double calculateAvgRatingByCarId(Long carId);
}