package com.carrental.dto.response;

import com.carrental.entity.Complaint;
import com.carrental.enums.ComplaintStatus;
import com.carrental.enums.ComplaintType;
import com.carrental.enums.SenderRole;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
public class AdminComplaintDetailResponse {

    private final Long complaintId;
    private final String senderName;
    private final String senderPhone;
    private final SenderRole senderRole;
    private final Long orderId;
    private final String carName;
    private final ComplaintType type;
    private final String content;
    private final List<String> evidenceImages;
    private final ComplaintStatus status;
    private final String resolution;
    private final LocalDateTime createdAt;
    private final LocalDateTime resolvedAt;

    private AdminComplaintDetailResponse(Complaint c) {
        this.complaintId    = c.getComplaintId();
        this.senderName     = c.getSender().getFullName();
        this.senderPhone    = c.getSender().getPhone();
        this.senderRole     = c.getSenderRole();
        this.orderId        = c.getRentalOrder().getOrderId();
        this.carName        = c.getRentalOrder().getCar().getModelName();
        this.type           = c.getType();
        this.content        = c.getContent();
        this.evidenceImages = (c.getEvidenceImages() != null && !c.getEvidenceImages().isBlank())
                ? Arrays.asList(c.getEvidenceImages().split(","))
                : List.of();
        this.status         = c.getStatus();
        this.resolution     = c.getResolution();
        this.createdAt      = c.getCreatedAt();
        this.resolvedAt     = c.getResolvedAt();
    }

    public static AdminComplaintDetailResponse from(Complaint c) {
        return new AdminComplaintDetailResponse(c);
    }
}