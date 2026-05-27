package com.carrental.dto.response;

import com.carrental.entity.Complaint;
import com.carrental.enums.ComplaintStatus;
import com.carrental.enums.ComplaintType;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class AdminComplaintListResponse {

    private final Long complaintId;
    private final String senderName;
    private final String senderPhone;
    private final ComplaintType type;
    private final Long orderId;
    private final String carName;
    private final LocalDateTime createdAt;
    private final ComplaintStatus status;

    private AdminComplaintListResponse(Complaint c) {
        this.complaintId  = c.getComplaintId();
        this.senderName   = c.getSender().getFullName();
        this.senderPhone  = c.getSender().getPhone();
        this.type         = c.getType();
        this.orderId      = c.getRentalOrder().getOrderId();
        this.carName      = c.getRentalOrder().getCar().getModelName();
        this.createdAt    = c.getCreatedAt();
        this.status       = c.getStatus();
    }

    public static AdminComplaintListResponse from(Complaint c) {
        return new AdminComplaintListResponse(c);
    }
}