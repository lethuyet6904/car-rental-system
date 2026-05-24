package com.carrental.service.impl;

import com.carrental.dto.response.AdminOwnerRequestDetailResponse;
import com.carrental.dto.response.AdminOwnerRequestListResponse;
import com.carrental.entity.IdentityVerification;
import com.carrental.entity.OwnerRegistration;
import com.carrental.entity.User;
import com.carrental.enums.UserRole;
import com.carrental.enums.VerificationStatus;
import com.carrental.repository.IdentityVerificationRepository;
import com.carrental.repository.OwnerRegistrationRepository;
import com.carrental.repository.UserRepository;
import com.carrental.service.AdminOwnerRequestService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOwnerRequestServiceImpl implements AdminOwnerRequestService {

    private final OwnerRegistrationRepository ownerRegistrationRepository;
    private final IdentityVerificationRepository identityVerificationRepository;
    private final UserRepository userRepository;

    @Override
    public Page<AdminOwnerRequestListResponse> getRequestList(
            VerificationStatus status, String keyword, Pageable pageable) {

        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return ownerRegistrationRepository
                .findByFilters(status, kw, pageable)
                .map(AdminOwnerRequestListResponse::from);
    }

    @Override
    public AdminOwnerRequestDetailResponse getRequestDetail(Long registrationId) {
        OwnerRegistration reg = findRegOrThrow(registrationId);
        IdentityVerification identity = identityVerificationRepository
                .findTopByUserUserIdOrderBySubmittedAtDesc(reg.getUser().getUserId())
                .orElse(null);
        return AdminOwnerRequestDetailResponse.from(reg, identity);
    }

    @Override
    @Transactional
    public void approveOwnerRequest(Long registrationId) {
        OwnerRegistration reg = findRegOrThrow(registrationId);

        if (!VerificationStatus.Pending.equals(reg.getStatus())) {
            throw new IllegalStateException("Yêu cầu đã được xử lý rồi");
        }

        reg.setStatus(VerificationStatus.Approved);
        reg.setReviewedAt(LocalDateTime.now());
        ownerRegistrationRepository.save(reg);

        // Nâng role lên Owner
        User user = reg.getUser();
        user.setRole(UserRole.Owner);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void rejectOwnerRequest(Long registrationId, String rejectReason) {
        OwnerRegistration reg = findRegOrThrow(registrationId);

        if (!VerificationStatus.Pending.equals(reg.getStatus())) {
            throw new IllegalStateException("Yêu cầu đã được xử lý rồi");
        }

        reg.setStatus(VerificationStatus.Rejected);
        reg.setRejectReason(rejectReason);
        reg.setReviewedAt(LocalDateTime.now());
        ownerRegistrationRepository.save(reg);
    }

    private OwnerRegistration findRegOrThrow(Long registrationId) {
        return ownerRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy yêu cầu registrationId=" + registrationId));
    }
}