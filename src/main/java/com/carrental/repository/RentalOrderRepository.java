package com.carrental.repository;

import com.carrental.entity.RentalOrder;
import com.carrental.entity.User;
import com.carrental.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
}