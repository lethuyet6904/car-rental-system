package com.carrental.enums;

public enum ComplaintType {
    // Customer only
    VehicleCondition,   // Tình trạng xe không đúng mô tả
    OwnerBehavior,      // Thái độ chủ xe không tốt
    LatePickup,         // Giao xe trễ giờ
    // Owner only
    VehicleDamage,      // Khách hàng gây hư hỏng xe
    LateReturn,         // Khách hàng trả xe trễ
    // Both
    PricingIssue,       // Vấn đề về giá/phí phát sinh
    Other               // Vấn đề khác
}
