package com.carrental.service.impl;

import com.carrental.dto.request.LockAccountRequest;
import com.carrental.dto.response.UserListResponse;
import com.carrental.dto.response.UserDetailResponse;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final OwnerRegistrationRepository ownerRegistrationRepository;
    private final IdentityVerificationRepository identityVerificationRepository;

    @Override
    public Page<UserListResponse> getUserList(String keyword, UserRole role, UserStatus status, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return userRepository.searchUsers(kw, role, status, pageable).map(UserListResponse::from);
    }

    @Override
    public UserDetailResponse getUserDetail(Long userId) {
        User user = findUserOrThrow(userId);
        var identity = identityVerificationRepository.findTopByUserUserIdOrderBySubmittedAtDesc(userId).orElse(null);
        var ownerReg = ownerRegistrationRepository.findTopByUserUserIdOrderBySubmittedAtDesc(userId).orElse(null);
        return UserDetailResponse.from(user, identity, ownerReg);
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
    public void approveOwnerRegistration(Long registrationId) {
        OwnerRegistration reg = ownerRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn đăng ký"));

        if (!VerificationStatus.Pending.equals(reg.getStatus())) {
            throw new RuntimeException("Đơn này đã được xử lý rồi");
        }

        // Duyệt đơn
        reg.setStatus(VerificationStatus.Approved);
        reg.setReviewedAt(LocalDateTime.now());
        
        ownerRegistrationRepository.save(reg);

        // Nâng role user lên Owner
        User user = reg.getUser();
        user.setRole(UserRole.Owner);
        
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void rejectOwnerRegistration(Long registrationId, String rejectReason) {
        OwnerRegistration reg = ownerRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy đơn đăng ký"));

        if (!VerificationStatus.Pending.equals(reg.getStatus())) {
            throw new IllegalStateException("Đơn đã được xử lý rồi");
        }
        
        reg.setStatus(VerificationStatus.Rejected);
        reg.setRejectReason(rejectReason);
        reg.setReviewedAt(LocalDateTime.now());
        
        ownerRegistrationRepository.save(reg);
    }
    
    @Override
    @Transactional
    public void approveIdentityVerification(Long verificationId) {
    	IdentityVerification identity =
                identityVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy hồ sơ xác minh"));

        if (!VerificationStatus.Pending.equals(identity.getStatus())) {
            throw new IllegalStateException("Hồ sơ đã được xử lý rồi");
        }

        identity.setStatus(VerificationStatus.Approved);
        identity.setReviewedAt(LocalDateTime.now());

        identityVerificationRepository.save(identity);
    }
    
    @Override
    @Transactional
    public void rejectIdentityVerification(Long verificationId, String reason) {
    	IdentityVerification identity =
                identityVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy hồ sơ xác minh"));

        if (!VerificationStatus.Pending.equals(identity.getStatus())) {
            throw new IllegalStateException("Hồ sơ đã được xử lý rồi");
        }

        identity.setStatus(VerificationStatus.Rejected);
        identity.setRejectReason(reason);
        identity.setReviewedAt(LocalDateTime.now());

        identityVerificationRepository.save(identity);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng userId =" + userId));
    }
}