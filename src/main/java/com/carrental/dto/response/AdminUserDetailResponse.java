package com.carrental.dto.response;

import com.carrental.entity.IdentityVerification;
import com.carrental.entity.OwnerRegistration;
import com.carrental.entity.User;
import com.carrental.enums.LicenseStatus;
import com.carrental.enums.UserRole;
import com.carrental.enums.UserStatus;
import com.carrental.enums.VerificationStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminUserDetailResponse {

    // User
    private final Long userId;
    private final String fullName;
    private final String phone;
    private final String email;
    private final String address;
    private final String avatar;
    private final UserRole role;
    private final UserStatus status;
    private final String lockReason;
    private final LocalDateTime createdAt;

    // IdentityVerification (null nếu chưa nộp)
    private final VerificationStatus identityStatus;
    private final String nationalId;
    private final String licenseNumber;
    private final LicenseStatus licenseStatus;
    private final String nationalIdFrontImage;
    private final String nationalIdBackImage;
    private final String licenseFrontImage;  // = frontImage trong entity
    private final String licenseBackImage;   // = backImage trong entity
    private final String identityRejectReason;
    private final LocalDateTime identitySubmittedAt;

    // OwnerRegistration (null nếu chưa đăng ký)
    private final VerificationStatus ownerRegStatus;
    private final String ownerRegRejectReason;
    private final LocalDateTime ownerRegSubmittedAt;

    private AdminUserDetailResponse(User user,
                                    IdentityVerification identity,
                                    OwnerRegistration ownerReg) {
        // User
        this.userId    = user.getUserId();
        this.fullName  = user.getFullName();
        this.phone     = user.getPhone();
        this.email     = user.getEmail();
        this.address   = user.getAddress();
        this.avatar    = user.getAvatar();
        this.role      = user.getRole();
        this.status    = user.getStatus();
        this.lockReason = user.getLockReason();
        this.createdAt = user.getCreatedAt();

        // Identity
        if (identity != null) {
            this.identityStatus        = identity.getStatus();
            this.nationalId            = identity.getNationalId();
            this.licenseNumber         = identity.getLicenseNumber();
            this.licenseStatus         = identity.getLicenseStatus();
            this.nationalIdFrontImage  = identity.getNationalIdFrontImage();
            this.nationalIdBackImage   = identity.getNationalIdBackImage();
            this.licenseFrontImage     = identity.getFrontImage();
            this.licenseBackImage      = identity.getBackImage();
            this.identityRejectReason  = identity.getRejectReason();
            this.identitySubmittedAt   = identity.getSubmittedAt();
        } else {
            this.identityStatus = null;
            this.nationalId = this.licenseNumber = null;
            this.licenseStatus = null;
            this.nationalIdFrontImage = this.nationalIdBackImage = null;
            this.licenseFrontImage = this.licenseBackImage = null;
            this.identityRejectReason = null;
            this.identitySubmittedAt = null;
        }

        // OwnerReg
        if (ownerReg != null) {
            this.ownerRegStatus       = ownerReg.getStatus();
            this.ownerRegRejectReason = ownerReg.getRejectReason();
            this.ownerRegSubmittedAt  = ownerReg.getSubmittedAt();
        } else {
            this.ownerRegStatus = null;
            this.ownerRegRejectReason = null;
            this.ownerRegSubmittedAt = null;
        }
    }

    public static AdminUserDetailResponse from(User user,
                                               IdentityVerification identity,
                                               OwnerRegistration ownerReg) {
        return new AdminUserDetailResponse(user, identity, ownerReg);
    }
}