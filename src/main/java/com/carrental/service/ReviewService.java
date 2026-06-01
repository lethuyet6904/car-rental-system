package com.carrental.service;

import com.carrental.entity.Review;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface ReviewService {

    void submitReview(Long orderId, int rating, String comment, Authentication auth);

    Optional<Review> findByOrderId(Long orderId);
}
