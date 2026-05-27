package com.carrental.service;

import com.carrental.dto.request.IdentityVerificationRequest;
import com.carrental.entity.IdentityVerification;
import com.carrental.entity.User;
import com.carrental.enums.VerificationStatus;

public interface IdentityVerificationService {

    /**
     * Nộp hồ sơ xác minh danh tính
     */
    IdentityVerification submitVerification(Long userId, IdentityVerificationRequest request);

    /**
     * Lấy hồ sơ xác minh mới nhất của user
     */
    IdentityVerification findLatestByUser(Long userId);

    /**
     * Kiểm tra user đã được xác minh danh tính chưa
     */
    boolean isIdentityVerified(Long userId);

    /**
     * Admin: Duyệt hồ sơ xác minh
     */
    IdentityVerification approveVerification(Long verificationId);

    /**
     * Admin: Từ chối hồ sơ xác minh
     */
    IdentityVerification rejectVerification(Long verificationId, String reason);

    /**
     * Lấy hồ sơ xác minh của user (kèm thông tin user)
     * Dùng cho OwnerRegistration fill tự động
     */
    IdentityVerification getVerifiedIdentityByUser(Long userId);
}