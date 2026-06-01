package com.carrental.service.impl;

import com.carrental.entity.Car;
import com.carrental.entity.RentalOrder;
import com.carrental.entity.Review;
import com.carrental.entity.User;
import com.carrental.enums.OrderStatus;
import com.carrental.repository.CarRepository;
import com.carrental.repository.RentalOrderRepository;
import com.carrental.repository.ReviewRepository;
import com.carrental.service.ReviewService;
import com.carrental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RentalOrderRepository rentalOrderRepository;
    private final CarRepository carRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void submitReview(Long orderId, int rating, String comment, Authentication auth) {
        User user = userService.findByPhone(auth.getName());
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }

        RentalOrder order = rentalOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getCustomer().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Bạn không có quyền đánh giá đơn hàng này");
        }
        if (order.getStatus() != OrderStatus.Completed) {
            throw new RuntimeException("Chỉ có thể đánh giá sau khi hoàn thành chuyến đi");
        }
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Điểm đánh giá phải từ 1 đến 5");
        }
        if (reviewRepository.findByRentalOrderOrderId(orderId).isPresent()) {
            throw new RuntimeException("Bạn đã đánh giá đơn hàng này rồi");
        }

        Review review = Review.builder()
                .rentalOrder(order)
                .customer(user)
                .rating((byte) rating)
                .comment(comment != null && !comment.isBlank() ? comment.trim() : null)
                .build();
        reviewRepository.save(review);

        Car car = order.getCar();
        Double avg = reviewRepository.calculateAvgRatingByCarId(car.getCarId());
        car.setAvgRating(BigDecimal.valueOf(avg != null ? avg : 0).setScale(1, RoundingMode.HALF_UP));
        carRepository.save(car);
    }

    @Override
    public Optional<Review> findByOrderId(Long orderId) {
        return reviewRepository.findByRentalOrderOrderId(orderId);
    }
}
