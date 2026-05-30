package com.carrental.enums;

public enum OrderStatus {
    Pending, // Chờ khách thanh toán cọc
    PendingApproval, // Đã cọc, chờ Owner duyệt
    Confirmed, // Owner đã duyệt, chờ giao xe
    InProgress, // Owner đã bấm "Đã giao xe"
    Completed, // Khách trả xe + thanh toán nốt
    Cancelled, // Đã hủy (có hoặc không hoàn cọc)
    Rejected // Owner từ chối (hoàn 100% cọc)
}