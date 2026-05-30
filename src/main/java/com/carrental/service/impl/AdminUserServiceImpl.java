package com.carrental.service.impl;

import com.carrental.dto.request.LockAccountRequest;
import com.carrental.dto.response.AdminUserDetailResponse;
import com.carrental.dto.response.AdminUserListResponse;
import com.carrental.entity.IdentityVerification;
import com.carrental.entity.OwnerRegistration;
import com.carrental.entity.User;
import com.carrental.enums.UserRole;
import com.carrental.enums.UserStatus;
import com.carrental.enums.VerificationStatus;
import com.carrental.repository.IdentityVerificationRepository;
import com.carrental.repository.OwnerRegistrationRepository;
import com.carrental.repository.UserRepository;
import com.carrental.service.AdminUserService;
import com.carrental.service.IdentityVerificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final OwnerRegistrationRepository ownerRegistrationRepository;
    private final IdentityVerificationRepository identityVerificationRepository;
    private final IdentityVerificationService identityVerificationService;

    @Override
    public Page<AdminUserListResponse> getUserList(String keyword, UserRole role, UserStatus status,
            Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return userRepository.searchUsers(kw, role, status, pageable).map(AdminUserListResponse::from);
    }

    @Override
    public AdminUserDetailResponse getUserDetail(Long userId) {
        User user = findUserOrThrow(userId);
        IdentityVerification identity = identityVerificationRepository.findTopByUserUserIdOrderBySubmittedAtDesc(userId)
                .orElse(null);
        OwnerRegistration ownerReg = ownerRegistrationRepository.findTopByUserUserIdOrderBySubmittedAtDesc(userId)
                .orElse(null);
        return AdminUserDetailResponse.from(user, identity, ownerReg);
    }

    @Override
    @Transactional
    public void lockAccount(Long userId, LockAccountRequest request) {
        User user = findUserOrThrow(userId);

        if (UserStatus.Locked.equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản đã bị khóa");
        }

        user.setStatus(UserStatus.Locked);
        user.setLockReason(request.getReason());

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unlockAccount(Long userId) {
        User user = findUserOrThrow(userId);

        if (UserStatus.Active.equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản đang hoạt động");
        }

        user.setStatus(UserStatus.Active);
        user.setLockReason(null);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void approveIdentityVerification(Long userId) {
        // Lấy verificationId từ hồ sơ mới nhất, sau đó delegate sang service
        IdentityVerification identity = identityVerificationRepository
                .findTopByUserUserIdOrderBySubmittedAtDesc(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hồ sơ xác minh"));
        identityVerificationService.approveCccd(identity.getVerificationId());
    }

    @Override
    @Transactional
    public void rejectIdentityVerification(Long userId, String reason) {
        IdentityVerification identity = identityVerificationRepository
                .findTopByUserUserIdOrderBySubmittedAtDesc(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hồ sơ xác minh"));
        identityVerificationService.rejectCccd(identity.getVerificationId(), reason);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng userId =" + userId));
    }
}