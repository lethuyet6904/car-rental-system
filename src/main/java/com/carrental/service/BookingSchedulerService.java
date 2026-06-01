package com.carrental.service;

import com.carrental.entity.RentalOrder;
import com.carrental.enums.OrderStatus;
import com.carrental.repository.CarScheduleRepository;
import com.carrental.repository.RentalOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingSchedulerService {

    private final RentalOrderRepository rentalOrderRepository;
    private final CarScheduleRepository carScheduleRepository;

    @Scheduled(fixedDelay = 300000) // runs every 5 minutes
    @Transactional
    public void cancelAbandonedBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);

        List<RentalOrder> abandonedOrders = rentalOrderRepository
                .findAbandonedPendingOrders(cutoff);

        for (RentalOrder order : abandonedOrders) {
            order.setStatus(OrderStatus.Cancelled);
            order.setCancelReason("Tự động hủy do không thanh toán cọc trong 30 phút");
            rentalOrderRepository.save(order);

            carScheduleRepository.findByRentalOrderOrderId(order.getOrderId())
                    .ifPresent(carScheduleRepository::delete);
        }

        if (!abandonedOrders.isEmpty()) {
            log.info("[Scheduler] Auto-cancelled {} abandoned orders", abandonedOrders.size());
        }
    }
}
