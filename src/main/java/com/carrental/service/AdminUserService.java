package com.carrental.service;

import com.carrental.dto.request.LockAccountRequest;
import com.carrental.dto.response.AdminUserDetailResponse;
import com.carrental.dto.response.AdminUserListResponse;
import com.carrental.enums.UserRole;
import com.carrental.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<AdminUserListResponse> getUserList(String keyword, UserRole role, UserStatus status, Pageable pageable);

    AdminUserDetailResponse getUserDetail(Long userId);
    
    // Khóa tài khoản
    void lockAccount(Long userId, LockAccountRequest request);

    // Mở khóa tài khoản
    void unlockAccount(Long userId);
    
 	// Duyệt xác minh CCCD/GPLX
    void approveIdentityVerification(Long userId);

    // Từ chối xác minh CCCD/GPLX
    void rejectIdentityVerification(Long userId, String reason);
}
