package com.carrental.repository;

import com.carrental.entity.OwnerRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OwnerRegistrationRepository extends JpaRepository<OwnerRegistration, Long> {

    boolean existsByUser_UserIdAndStatus(Long userId, String status);

    List<OwnerRegistration> findByUser_UserId(Long userId);
}