package com.carrental.dto.response;

import com.carrental.entity.IdentityVerification;
import com.carrental.entity.OwnerRegistration;
import com.carrental.entity.User;
import com.carrental.enums.UserRole;
import com.carrental.enums.UserStatus;
import com.carrental.enums.VerificationStatus;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class UserDetailResponse {

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

    // Thông tin xác minh danh tính (nullable — user chưa nộp)
    private final VerificationStatus identityStatus;
    private final String nationalId;
    private final String nationalIdFrontImage;
    private final String nationalIdBackImage;
    private final String licenseNumber;

    // Thông tin đăng ký Owner (nullable)
    private final VerificationStatus ownerRegistrationStatus;
    private final String ownerRejectReason;

    public static UserDetailResponse from(User user,
                                          IdentityVerification identity,
                                          OwnerRegistration ownerReg) {
        return new UserDetailResponse(user, identity, ownerReg);
    }

    private UserDetailResponse(User user,
                                IdentityVerification identity,
                                OwnerRegistration ownerReg) {
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

        // Identity — null safe
        if (identity != null) {
            this.identityStatus       = identity.getStatus();
            this.nationalId           = identity.getNationalId();
            this.nationalIdFrontImage = identity.getNationalIdFrontImage();
            this.nationalIdBackImage  = identity.getNationalIdBackImage();
            this.licenseNumber        = identity.getLicenseNumber();
        } else {
            this.identityStatus       = null;
            this.nationalId           = null;
            this.nationalIdFrontImage = null;
            this.nationalIdBackImage  = null;
            this.licenseNumber        = null;
        }

        // OwnerRegistration — null safe
        if (ownerReg != null) {
            this.ownerRegistrationStatus = ownerReg.getStatus();
            this.ownerRejectReason       = ownerReg.getRejectReason();
        } else {
            this.ownerRegistrationStatus = null;
            this.ownerRejectReason       = null;
        }
    }
}