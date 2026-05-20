package com.carrental.repository;

import com.carrental.entity.Complaint;
import com.carrental.enums.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Admin: xem tất cả khiếu nại theo status
    List<Complaint> findByStatus(ComplaintStatus status);

    // Admin: xem tất cả, sắp xếp mới nhất trước
    List<Complaint> findAllByOrderByCreatedAtDesc();

    // Admin dashboard: đếm khiếu nại chờ xử lý
    long countByStatus(ComplaintStatus status);
}