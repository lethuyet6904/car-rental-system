package com.carrental.repository;

import com.carrental.entity.OwnerRegistration;
import com.carrental.enums.VerificationStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    Optional<OwnerRegistration> findTopByUserUserIdAndStatusOrderBySubmittedAtDesc(
            Long userId, VerificationStatus status);
    
    // Admin: filter theo status + keyword (tên, SĐT)
       @Query("""
               SELECT r FROM OwnerRegistration r
               JOIN r.user u
               WHERE (:status  IS NULL OR r.status = :status)
                 AND (:keyword IS NULL
                      OR u.phone LIKE CONCAT('%', :keyword, '%')
                      OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
               """)
       Page<OwnerRegistration> findByFilters(
               @Param("status")  VerificationStatus status,
               @Param("keyword") String keyword,
               Pageable pageable);
}