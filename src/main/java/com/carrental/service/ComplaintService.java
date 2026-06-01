package com.carrental.service;

import com.carrental.dto.response.ComplaintResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ComplaintService {

    Long submitComplaint(Long orderId, String type, String content,
                         List<MultipartFile> evidenceImages, Authentication auth);

    Page<ComplaintResponse> getMyComplaints(Authentication auth, Pageable pageable);

    ComplaintResponse getMyComplaintDetail(Long complaintId, Authentication auth);

    Optional<ComplaintResponse> findByOrderAndSender(Long orderId, Long userId);
}
