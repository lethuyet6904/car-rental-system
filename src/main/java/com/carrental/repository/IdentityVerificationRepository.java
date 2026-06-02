package com.carrental.repository;

import com.carrental.entity.IdentityVerification;
import com.carrental.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IdentityVerificationRepository extends JpaRepository<IdentityVerification, Long> {

    // Tìm hồ sơ xác minh mới nhất của user
    Optional<IdentityVerification> findTopByUserUserIdOrderBySubmittedAtDesc(Long userId);
    
    // Lấy tất cả hồ sơ xác minh của 1 user (có thể có nhiều nếu bị reject rồi nộp lại)
    List<IdentityVerification> findByUser_UserId(Long userId);

    // Admin: xem danh sách hồ sơ theo status
    List<IdentityVerification> findByStatus(VerificationStatus status);
    
	// Thêm hàm này vào dưới hàm countByStatus hiện tại
    @Query("""
	        SELECT COUNT(iv) FROM IdentityVerification iv 
	        WHERE iv.status = :pendingStatus 
	           OR (iv.status = :approvedStatus AND iv.licenseStatus = :pendingLicenseStatus)
	    """)
    long countTotalPendingVerifications(
            @Param("pendingStatus") VerificationStatus pendingStatus, 
            @Param("approvedStatus") VerificationStatus approvedStatus,
            @Param("pendingLicenseStatus") com.carrental.enums.LicenseStatus pendingLicenseStatus
    );
}