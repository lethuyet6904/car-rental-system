package com.carrental.service;

import com.carrental.dto.request.IdentityVerificationRequest;
import com.carrental.entity.IdentityVerification;

public interface IdentityVerificationService {

    /**
     * Bước 1: Nộp hồ sơ CCCD (bắt buộc trước)
     * → Trạng thái: Pending (chờ Admin duyệt CCCD)
     */
    IdentityVerification submitCccd(Long userId, IdentityVerificationRequest request);

    /**
     * Bước 2: Bổ sung GPLX (chỉ sau khi CCCD đã Approved)
     * → Giữ nguyên record, cập nhật thêm GPLX fields
     * → Trạng thái vẫn là Approved (hoặc tùy business)
     */
    IdentityVerification submitLicense(Long userId, IdentityVerificationRequest request);

    /**
     * Lấy hồ sơ xác minh mới nhất của user
     */
    IdentityVerification findLatestByUser(Long userId);

    /**
     * Kiểm tra CCCD đã được Admin duyệt chưa
     * (điều kiện để đăng ký chủ xe)
     */
    boolean isCccdApproved(Long userId);

    /**
     * Kiểm tra đã có cả CCCD + GPLX được duyệt chưa
     * (điều kiện để thuê xe)
     */
    boolean isFullyVerified(Long userId);

    /**
     * Admin: Duyệt CCCD (theo verificationId)
     */
    IdentityVerification approveCccd(Long verificationId);

    /**
     * Admin: Từ chối CCCD
     */
    IdentityVerification rejectCccd(Long verificationId, String reason);

    /**
     * Lấy hồ sơ đã xác minh (dùng khi fill thông tin owner registration)
     */
    IdentityVerification getApprovedByUser(Long userId);
}