package com.carrental.dto.response;

import com.carrental.entity.Complaint;
import com.carrental.enums.ComplaintStatus;
import com.carrental.enums.ComplaintType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
@Builder
public class ComplaintResponse {

    private final Long complaintId;
    private final Long orderId;
    private final String carName;
    private final ComplaintType type;
    private final String content;
    private final ComplaintStatus status;
    private final String resolution;
    private final LocalDateTime createdAt;
    private final LocalDateTime resolvedAt;
    private final List<String> evidenceImages;

    public static ComplaintResponse from(Complaint c) {
        return ComplaintResponse.builder()
                .complaintId(c.getComplaintId())
                .orderId(c.getRentalOrder().getOrderId())
                .carName(c.getRentalOrder().getCar().getModelName())
                .type(c.getType())
                .content(c.getContent())
                .status(c.getStatus())
                .resolution(c.getResolution())
                .createdAt(c.getCreatedAt())
                .resolvedAt(c.getResolvedAt())
                .evidenceImages(parseEvidenceImages(c.getEvidenceImages()))
                .build();
    }

    private static List<String> parseEvidenceImages(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.asList(raw.split(","));
    }
}
