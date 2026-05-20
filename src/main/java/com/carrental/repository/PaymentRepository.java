package com.carrental.repository;

import com.carrental.entity.Payment;
import com.carrental.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByRentalOrderOrderId(Long orderId);

    // Tìm giao dịch đặt cọc của 1 đơn — chỉ có 1
    Optional<Payment> findByRentalOrderOrderIdAndTransactionType(
            Long orderId, TransactionType transactionType);
}