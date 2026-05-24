package com.carrental.dto.response;

import com.carrental.entity.OwnerRegistration;
import com.carrental.enums.VerificationStatus;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class AdminOwnerRequestListResponse {

    private final Long registrationId;
    private final Long userId;
    private final String fullName;
    private final String phone;
    private final LocalDateTime submittedAt;
    private final VerificationStatus status;

    private AdminOwnerRequestListResponse(OwnerRegistration r) {
        this.registrationId = r.getRegistrationId();
        this.userId         = r.getUser().getUserId();
        this.fullName       = r.getUser().getFullName();
        this.phone          = r.getUser().getPhone();
        this.submittedAt    = r.getSubmittedAt();
        this.status         = r.getStatus();
    }

    public static AdminOwnerRequestListResponse from(OwnerRegistration r) {
        return new AdminOwnerRequestListResponse(r);
    }
}