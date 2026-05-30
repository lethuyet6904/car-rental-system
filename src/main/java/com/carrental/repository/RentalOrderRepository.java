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
import java.time.LocalDateTime;
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
    List<RentalOrder> findByCustomerUserIdOrderByCreatedAtDesc(Long userId);

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
    
    // Doanh thu theo ngày
    @Query(value = """
            SELECT CONVERT(varchar(10), o.createdAt, 120) AS dateLabel,
                   SUM(o.totalAmount) AS revenue
            FROM RentalOrder o
            WHERE o.status = 'Completed'
              AND o.createdAt >= :fromDate
            GROUP BY CONVERT(varchar(10), o.createdAt, 120)
            ORDER BY dateLabel
            """, nativeQuery = true)
    List<Object[]> findRevenueByDate(@Param("fromDate") LocalDateTime fromDate);

    // Số đơn theo ngày
    @Query(value = """
            SELECT CONVERT(varchar(10), o.createdAt, 120) AS dateLabel,
                   COUNT(o.orderId) AS orderCount
            FROM RentalOrder o
            WHERE o.createdAt >= :fromDate
            GROUP BY CONVERT(varchar(10), o.createdAt, 120)
            ORDER BY dateLabel
            """, nativeQuery = true)
    List<Object[]> findOrderCountByDate(@Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(o) FROM RentalOrder o WHERE o.status = :status AND o.createdAt >= :fromDate")
    long countByStatusAndFromDate(@Param("status") OrderStatus status,
                                   @Param("fromDate") LocalDateTime fromDate);
    
    // Group theo năm (dùng cho "Tất cả")
    @Query(value = """
            SELECT CAST(YEAR(o.createdAt) AS varchar) AS dateLabel,
                   SUM(o.totalAmount) AS revenue
            FROM RentalOrder o
            WHERE o.status = 'Completed'
            GROUP BY YEAR(o.createdAt)
            ORDER BY dateLabel
            """, nativeQuery = true)
    List<Object[]> findRevenueByYear();

    // Group theo tháng trong 1 năm (dùng cho "Năm này")
    @Query(value = """
            SELECT CONVERT(varchar(7), o.createdAt, 120) AS dateLabel,
                   SUM(o.totalAmount) AS revenue
            FROM RentalOrder o
            WHERE o.status = 'Completed'
              AND YEAR(o.createdAt) = YEAR(GETDATE())
            GROUP BY CONVERT(varchar(7), o.createdAt, 120)
            ORDER BY dateLabel
            """, nativeQuery = true)
    List<Object[]> findRevenueByMonthThisYear();

    @Query(value = """
            SELECT CAST(YEAR(o.createdAt) AS varchar) AS dateLabel,
                   COUNT(o.orderId) AS orderCount
            FROM RentalOrder o
            GROUP BY YEAR(o.createdAt)
            ORDER BY dateLabel
            """, nativeQuery = true)
    List<Object[]> findOrderCountByYear();

    @Query(value = """
            SELECT CONVERT(varchar(7), o.createdAt, 120) AS dateLabel,
                   COUNT(o.orderId) AS orderCount
            FROM RentalOrder o
            WHERE YEAR(o.createdAt) = YEAR(GETDATE())
            GROUP BY CONVERT(varchar(7), o.createdAt, 120)
            ORDER BY dateLabel
            """, nativeQuery = true)
    List<Object[]> findOrderCountByMonthThisYear();
    
    // Top 5 xe
    @Query("""
            SELECT c.modelName, c.licensePlate, COUNT(o.orderId)
            FROM RentalOrder o
            JOIN o.car c
            WHERE o.status = com.carrental.enums.OrderStatus.Completed
            GROUP BY c.carId, c.modelName, c.licensePlate
            ORDER BY COUNT(o.orderId) DESC
            """)
    List<Object[]> findTop5Cars(Pageable pageable);

    // Top 5 khu vực
    @Query("""
            SELECT c.region.regionName, COUNT(o.orderId)
            FROM RentalOrder o
            JOIN o.car c
            WHERE o.status = com.carrental.enums.OrderStatus.Completed
            GROUP BY c.region.regionId, c.region.regionName
            ORDER BY COUNT(o.orderId) DESC
            """)
    List<Object[]> findTop5Regions(Pageable pageable);
}