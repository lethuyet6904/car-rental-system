package com.carrental.repository;

import com.carrental.entity.User;
import com.carrental.enums.UserRole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);
    
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    
    Optional<User> findByEmail(String email);
    // Dùng cho Admin — lọc user theo role
    java.util.List<User> findByRole(UserRole role);
}