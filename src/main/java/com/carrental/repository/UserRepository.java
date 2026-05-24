package com.carrental.repository;

import com.carrental.entity.User;
import com.carrental.enums.UserRole;
import com.carrental.enums.UserStatus;

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
    @Query("""
            SELECT u FROM User u
            WHERE (:keyword IS NULL
                   OR u.phone LIKE CONCAT('%', :keyword, '%')
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:role   IS NULL OR u.role   = :role)
              AND (:status IS NULL OR u.status = :status)
            """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role")    UserRole role,
            @Param("status")  UserStatus status,
            Pageable pageable);
 
    // ---------- Thống kê ----------
    long countByRole(UserRole role);
 
    long countByStatus(UserStatus status);
}