package com.carrental.enums;

public enum CarStatus {
	Pending,    // Xe vừa đăng, chờ Admin duyệt
    Active,     // Xe đang hoạt động, có thể thuê
    Inactive,   // Chủ xe tạm ngừng
    Rejected    // Admin từ chối
}
