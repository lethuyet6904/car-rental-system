package com.carrental.repository;

import com.carrental.entity.OwnerRegistration;
import com.carrental.enums.VerificationStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerRegistrationRepository extends JpaRepository<OwnerRegistration, Long> {

	// Kiểm tra user đã có đơn đăng ký với status nào đó chưa
    // Ví dụ: tránh submit 2 đơn Pending cùng lúc
    boolean existsByUser_UserIdAndStatus(Long userId, VerificationStatus status);
    
    // Lấy tất cả đơn đăng ký của 1 user (có thể có nhiều nếu bị reject rồi nộp lại)
    List<OwnerRegistration> findByUser_UserId(Long userId);
    // Admin: xem danh sách đơn theo status
    List<OwnerRegistration> findByStatus(VerificationStatus status);

    // Lấy đơn mới nhất của user
    Optional<OwnerRegistration> findTopByUserUserIdOrderBySubmittedAtDesc(Long userId);
}