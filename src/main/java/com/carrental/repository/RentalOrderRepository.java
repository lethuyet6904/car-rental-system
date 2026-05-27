package com.carrental.repository;

import com.carrental.entity.RentalOrder;
import com.carrental.entity.User;
import com.carrental.enums.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {

    // Customer
    List<RentalOrder> findByCustomer(User customer);

    // Owner
    List<RentalOrder> findByCarOwner(User owner);

    // Admin
    List<RentalOrder> findByStatus(OrderStatus status);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM RentalOrder o WHERE o.status = 'Completed'")
    BigDecimal sumCompletedRevenue();

    // ==================== METHOD QUAN TRỌNG CHO DASHBOARD OWNER ====================
    @EntityGraph(attributePaths = {
        "customer", 
        "car", 
        "car.brand", 
        "car.carType"
    })
    @Query("SELECT o FROM RentalOrder o " +
           "WHERE o.car.owner.userId = :ownerUserId " +
           "ORDER BY o.createdAt DESC")
    List<RentalOrder> findByCarOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

    // Method lấy chi tiết đơn hàng (dùng cho order detail)
    @EntityGraph(attributePaths = {
        "customer", 
        "car", 
        "car.brand", 
        "car.carType", 
        "car.region"
    })
    @Query("SELECT o FROM RentalOrder o WHERE o.orderId = :orderId")
    Optional<RentalOrder> findDetailedById(Long orderId);

    // Dùng cho khách hàng xem đơn
    @EntityGraph(attributePaths = {"car", "car.brand"})
    List<RentalOrder> findByCustomerUserId(Long userId);

 // Admin filter
    @Query(value = """
            SELECT o FROM RentalOrder o
            JOIN FETCH o.customer
            JOIN FETCH o.car c
            JOIN FETCH c.brand
            WHERE (:status   IS NULL OR o.status = :status)
              AND (:fromDate  IS NULL OR o.pickupDate >= :fromDate)
              AND (:keyword   IS NULL
                   OR LOWER(c.modelName)        LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.customer.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR CAST(o.orderId AS string)  LIKE CONCAT('%', :keyword, '%'))
            """,
           countQuery = """
            SELECT COUNT(o) FROM RentalOrder o
            JOIN o.customer
            JOIN o.car c
            WHERE (:status   IS NULL OR o.status = :status)
              AND (:fromDate  IS NULL OR o.pickupDate >= :fromDate)
              AND (:keyword   IS NULL
                   OR LOWER(c.modelName)        LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.customer.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR CAST(o.orderId AS string)  LIKE CONCAT('%', :keyword, '%'))
            """)
    Page<RentalOrder> findByFilters(
            @Param("status")   OrderStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("keyword")  String keyword,
            Pageable pageable);

    @Query("""
            SELECT o FROM RentalOrder o
            JOIN FETCH o.customer
            JOIN FETCH o.car c
            JOIN FETCH c.brand
            JOIN FETCH c.owner
            WHERE o.orderId = :id
            """)
    Optional<RentalOrder> findWithDetailsById(@Param("id") Long id);
    
 // Top 5 xe được thuê nhiều nhất
    @Query("""
            SELECT c.modelName, c.licensePlate, COUNT(o.orderId) as orderCount
            FROM RentalOrder o
            JOIN o.car c
            WHERE o.status = 'Completed'
            GROUP BY c.carId, c.modelName, c.licensePlate
            ORDER BY COUNT(o.orderId) DESC
            """)
    List<Object[]> findTop5Cars(Pageable pageable);

    // Top 5 khu vực
    @Query("""
            SELECT c.region.regionName, COUNT(o.orderId) as orderCount
            FROM RentalOrder o
            JOIN o.car c
            WHERE o.status = 'Completed'
            GROUP BY c.region.regionId, c.region.regionName
            ORDER BY COUNT(o.orderId) DESC
            """)
    List<Object[]> findTop5Regions(Pageable pageable);
}