package com.carrental.dto.response;

import com.carrental.entity.IdentityVerification;
import com.carrental.entity.OwnerRegistration;
import com.carrental.enums.VerificationStatus;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class AdminOwnerRequestDetailResponse {

    private final Long registrationId;
    private final Long userId;
    private final String fullName;
    private final String phone;
    private final LocalDateTime submittedAt;
    private final LocalDateTime reviewedAt;
    private final VerificationStatus status;
    private final String rejectReason;

    // Thông tin ngân hàng
    private final String bankName;
    private final String bankAccount;
    private final String accountHolder;

    // Từ IdentityVerification (nullable — user có thể chưa xác minh)
    private final String nationalId;
    private final String nationalIdFrontImage;
    private final String nationalIdBackImage;
    private final VerificationStatus identityStatus;

    private AdminOwnerRequestDetailResponse(OwnerRegistration r, IdentityVerification identity) {
        this.registrationId = r.getRegistrationId();
        this.userId         = r.getUser().getUserId();
        this.fullName       = r.getUser().getFullName();
        this.phone          = r.getUser().getPhone();
        this.submittedAt    = r.getSubmittedAt();
        this.reviewedAt     = r.getReviewedAt();
        this.status         = r.getStatus();
        this.rejectReason   = r.getRejectReason();
        this.bankName       = r.getBankName();
        this.bankAccount    = r.getBankAccount();
        this.accountHolder  = r.getAccountHolder();

        if (identity != null) {
            this.nationalId            = identity.getNationalId();
            this.nationalIdFrontImage  = identity.getNationalIdFrontImage();
            this.nationalIdBackImage   = identity.getNationalIdBackImage();
            this.identityStatus        = identity.getStatus();
        } else {
            this.nationalId            = null;
            this.nationalIdFrontImage  = null;
            this.nationalIdBackImage   = null;
            this.identityStatus        = null;
        }
    }

    public static AdminOwnerRequestDetailResponse from(OwnerRegistration r,
                                                        IdentityVerification identity) {
        return new AdminOwnerRequestDetailResponse(r, identity);
    }
}