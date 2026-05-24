package com.carrental.service;

import com.carrental.dto.response.AdminOwnerRequestDetailResponse;
import com.carrental.dto.response.AdminOwnerRequestListResponse;
import com.carrental.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminOwnerRequestService {

    Page<AdminOwnerRequestListResponse> getRequestList(
            VerificationStatus status, String keyword, Pageable pageable);

    AdminOwnerRequestDetailResponse getRequestDetail(Long registrationId);

    void approveOwnerRequest(Long registrationId);

    void rejectOwnerRequest(Long registrationId, String rejectReason);
}