package com.carrental.repository;

import com.carrental.entity.RentalOrder;
import com.carrental.entity.User;
import com.carrental.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {

    // Customer: xem đơn của mình
    List<RentalOrder> findByCustomer(User customer);

    // Owner: xem đơn của xe mình
    List<RentalOrder> findByCarOwner(User owner);

    // Admin: lọc theo status
    List<RentalOrder> findByStatus(OrderStatus status);

    // Admin dashboard: đếm đơn theo status
    long countByStatus(OrderStatus status);

    // Admin dashboard: tổng doanh thu
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM RentalOrder o WHERE o.status = 'Completed'")
    java.math.BigDecimal sumCompletedRevenue();
}