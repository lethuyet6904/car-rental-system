package com.carrental.service;

import com.carrental.dto.response.AdminOrderDetailResponse;
import com.carrental.dto.response.AdminOrderListResponse;
import com.carrental.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminOrderService {

    Page<AdminOrderListResponse> getOrderList( OrderStatus status, String timeRange, String keyword, Pageable pageable);
    AdminOrderDetailResponse getOrderDetail(Long orderId);
}