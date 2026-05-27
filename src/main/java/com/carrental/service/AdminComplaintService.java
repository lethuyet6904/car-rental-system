package com.carrental.service;

import com.carrental.dto.response.AdminComplaintDetailResponse;
import com.carrental.dto.response.AdminComplaintListResponse;
import com.carrental.enums.ComplaintStatus;
import com.carrental.enums.ComplaintType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminComplaintService {

    Page<AdminComplaintListResponse> getComplaintList(
            ComplaintStatus status, ComplaintType type, String keyword, Pageable pageable);

    AdminComplaintDetailResponse getComplaintDetail(Long complaintId);

    void resolve(Long complaintId, ComplaintStatus newStatus, String resolution);
}