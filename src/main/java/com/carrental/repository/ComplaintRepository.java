package com.carrental.repository;

import com.carrental.entity.Complaint;
import com.carrental.enums.ComplaintStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Admin: xem tất cả khiếu nại theo status
    List<Complaint> findByStatus(ComplaintStatus status);

    // Admin: xem tất cả, sắp xếp mới nhất trước
    List<Complaint> findAllByOrderByCreatedAtDesc();

    // Admin dashboard: đếm khiếu nại chờ xử lý
    long countByStatus(ComplaintStatus status);
    
    // Admin filter
    @Query(value = """
            SELECT c.* FROM Complaint c
            JOIN [User] s ON c.senderId = s.userId
            JOIN RentalOrder r ON c.orderId = r.orderId
            JOIN Car ca ON r.carId = ca.carId
            WHERE (:status  IS NULL OR c.status = :status)
              AND (:type    IS NULL OR c.type   = :type)
              AND (:keyword IS NULL
                   OR s.fullName LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_CI_AI
                   OR s.phone    LIKE CONCAT('%', :keyword, '%'))
            """,
           countQuery = """
            SELECT COUNT(*) FROM Complaint c
            JOIN [User] s ON c.senderId = s.userId
            JOIN RentalOrder r ON c.orderId = r.orderId
            WHERE (:status  IS NULL OR c.status = :status)
              AND (:type    IS NULL OR c.type   = :type)
              AND (:keyword IS NULL
                   OR s.fullName LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_CI_AI
                   OR s.phone    LIKE CONCAT('%', :keyword, '%'))
            """,
           nativeQuery = true)
    Page<Complaint> findByFilters(
            @Param("status")  String status,
            @Param("type")    String type,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("""
            SELECT c FROM Complaint c
            JOIN FETCH c.sender
            JOIN FETCH c.rentalOrder r
            JOIN FETCH r.car
            WHERE c.complaintId = :id
            """)
    Optional<Complaint> findWithDetailsById(@Param("id") Long id);

    @Query("""
            SELECT c FROM Complaint c
            JOIN FETCH c.rentalOrder r
            JOIN FETCH r.car
            WHERE c.rentalOrder.orderId = :orderId
            AND c.sender.userId = :senderId
            ORDER BY c.createdAt DESC
            """)
    List<Complaint> findByOrderAndSenderOrderByCreatedAtDesc(
            @Param("orderId") Long orderId,
            @Param("senderId") Long senderId);

    @Query("""
            SELECT c FROM Complaint c
            JOIN FETCH c.rentalOrder r
            JOIN FETCH r.car
            WHERE c.sender.userId = :senderId
            """)
    Page<Complaint> findBySenderUserId(@Param("senderId") Long senderId, Pageable pageable);

    @Query("""
            SELECT c FROM Complaint c
            JOIN FETCH c.sender
            JOIN FETCH c.rentalOrder r
            JOIN FETCH r.car
            WHERE c.complaintId = :id
            """)
    Optional<Complaint> findWithDetailsByIdAndSender(@Param("id") Long id, @Param("senderId") Long senderId);
}