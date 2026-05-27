package com.carrental.enums;

public enum OrderStatus {
	 PENDING_PAYMENT,  // Chờ thanh toán cọc (mới thêm)
	Pending,      // Khách vừa đặt, chờ Owner xác nhận
    Confirmed,    // Owner đã xác nhận
    InProgress,   // Đang thuê (đã nhận xe)
    Completed,    // Đã trả xe xong
    Cancelled,    // Khách hoặc Owner hủy
    Rejected      // Owner từ chối
}
