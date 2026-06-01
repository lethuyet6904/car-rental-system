package com.carrental.repository;

import com.carrental.entity.User;
import com.carrental.enums.UserRole;
import com.carrental.enums.UserStatus;
import com.carrental.enums.VerificationStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	// ---------- Auth ----------
    Optional<User> findByPhone(String phone);
    
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    
    Optional<User> findByEmail(String email);
    // ---------- Admin: tìm kiếm & lọc ----------
    
    /**
     * Tìm kiếm user theo keyword (phone / email / fullName) + lọc theo role + status.
     * Truyền null để bỏ qua filter đó.
     */
    /**
     * Tìm kiếm user theo keyword, role, status và identityStatus
     * Sử dụng Native Query để hỗ trợ COLLATE tiếng Việt và logic LEFT JOIN phức tạp
     */
    @Query(value = """
            SELECT u.* FROM [User] u
            LEFT JOIN IdentityVerification iv ON iv.userId = u.userId
              AND iv.submittedAt = (
                  SELECT MAX(iv2.submittedAt) FROM IdentityVerification iv2
                  WHERE iv2.userId = u.userId)
            WHERE (:keyword IS NULL
                   OR u.fullName COLLATE Vietnamese_CI_AI LIKE CONCAT(N'%', :keyword, N'%')
                   OR u.phone    LIKE CONCAT(N'%', :keyword, N'%')
                   OR u.email    COLLATE Vietnamese_CI_AI LIKE CONCAT(N'%', :keyword, N'%'))
              AND (:role   IS NULL OR u.role   = :role)
              AND (:status IS NULL OR u.status = :status)
              AND (:identityStatus IS NULL
                   OR (:identityStatus = 'None'             AND iv.userId IS NULL)
                   OR (:identityStatus = 'Pending'          AND iv.status = 'Pending')
                   OR (:identityStatus = 'Rejected'         AND iv.status = 'Rejected')
                   OR (:identityStatus = 'Approved'         AND iv.status = 'Approved' AND iv.licenseNumber IS NOT NULL AND iv.licenseNumber != '')
                   OR (:identityStatus = 'ApprovedNoLicense' AND iv.status = 'Approved' AND (iv.licenseNumber IS NULL OR iv.licenseNumber = '')))
            """,
           countQuery = """
            SELECT COUNT(*) FROM [User] u
            LEFT JOIN IdentityVerification iv ON iv.userId = u.userId
              AND iv.submittedAt = (
                  SELECT MAX(iv2.submittedAt) FROM IdentityVerification iv2
                  WHERE iv2.userId = u.userId)
            WHERE (:keyword IS NULL
                   OR u.fullName COLLATE Vietnamese_CI_AI LIKE CONCAT(N'%', :keyword, N'%')
                   OR u.phone    LIKE CONCAT(N'%', :keyword, N'%')
                   OR u.email    COLLATE Vietnamese_CI_AI LIKE CONCAT(N'%', :keyword, N'%'))
              AND (:role   IS NULL OR u.role   = :role)
              AND (:status IS NULL OR u.status = :status)
              AND (:identityStatus IS NULL
                   OR (:identityStatus = 'None'             AND iv.userId IS NULL)
                   OR (:identityStatus = 'Pending'          AND iv.status = 'Pending')
                   OR (:identityStatus = 'Rejected'         AND iv.status = 'Rejected')
                   OR (:identityStatus = 'Approved'         AND iv.status = 'Approved' AND iv.licenseNumber IS NOT NULL AND iv.licenseNumber != '')
                   OR (:identityStatus = 'ApprovedNoLicense' AND iv.status = 'Approved' AND (iv.licenseNumber IS NULL OR iv.licenseNumber = '')))
            """,
           nativeQuery = true)
    Page<User> searchUsers(
        @Param("keyword")        String keyword,
        @Param("role")           String role,
        @Param("status")         String status,
        @Param("identityStatus") String identityStatus,
        Pageable pageable);
    // ---------- Thống kê ----------
    long countByRole(UserRole role);
 
    long countByStatus(UserStatus status);
}