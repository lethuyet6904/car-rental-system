package com.carrental.service;

import com.carrental.dto.request.LockAccountRequest;
import com.carrental.dto.response.UserListResponse;
import com.carrental.dto.response.UserDetailResponse;
import com.carrental.enums.UserRole;
import com.carrental.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<UserListResponse> getUserList(String keyword, UserRole role, UserStatus status, Pageable pageable);

    UserDetailResponse getUserDetail(Long userId);
    
    // Khóa tài khoản
    void lockAccount(Long userId, LockAccountRequest request);

    // Mở khóa tài khoản
    void unlockAccount(Long userId);
    
    // Duyệt đăng ký Owner
    void approveOwnerRegistration(Long registrationId);

    // Từ chối đăng ký Owner
    void rejectOwnerRegistration(Long registrationId, String rejectReason);
    
 	// Duyệt xác minh CCCD/GPLX
    void approveIdentityVerification(Long verificationId);

    // Từ chối xác minh CCCD/GPLX
    void rejectIdentityVerification(Long verificationId, String rejectReason);
}
