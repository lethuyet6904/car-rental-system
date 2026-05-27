package com.carrental.service.impl;

import com.carrental.dto.response.AdminComplaintDetailResponse;
import com.carrental.dto.response.AdminComplaintListResponse;
import com.carrental.entity.Complaint;
import com.carrental.enums.ComplaintStatus;
import com.carrental.enums.ComplaintType;
import com.carrental.repository.ComplaintRepository;
import com.carrental.service.AdminComplaintService;
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
public class AdminComplaintServiceImpl implements AdminComplaintService {

    private final ComplaintRepository complaintRepository;

    @Override
    public Page<AdminComplaintListResponse> getComplaintList(
            ComplaintStatus status, ComplaintType type, String keyword, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String statusStr = status != null ? status.name() : null;
        String typeStr   = type   != null ? type.name()   : null;
        return complaintRepository.findByFilters(statusStr, typeStr, kw, pageable)
                .map(AdminComplaintListResponse::from);
    }

    @Override
    public AdminComplaintDetailResponse getComplaintDetail(Long complaintId) {
        Complaint complaint = complaintRepository.findWithDetailsById(complaintId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy khiếu nại id=" + complaintId));
        return AdminComplaintDetailResponse.from(complaint);
    }

    @Override
    @Transactional
    public void resolve(Long complaintId, ComplaintStatus newStatus, String resolution) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy khiếu nại id=" + complaintId));

        if (ComplaintStatus.Resolved.equals(complaint.getStatus())
                || ComplaintStatus.Rejected.equals(complaint.getStatus())) {
            throw new IllegalStateException("Khiếu nại đã được xử lý rồi");
        }

        complaint.setStatus(newStatus);
        complaint.setResolution(resolution);
        complaint.setResolvedAt(LocalDateTime.now());
        complaintRepository.save(complaint);
    }
}