package com.carrental.entity;

import com.carrental.enums.LicenseStatus;
import com.carrental.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "IdentityVerification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verificationId")
    private Long verificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    // ── CCCD / CMND (bắt buộc) ──────────────────────────────────
    @Column(name = "nationalId", nullable = false, length = 20)
    private String nationalId;

    @Column(name = "nationalIdFrontImage", nullable = false, length = 200)
    private String nationalIdFrontImage;

    @Column(name = "nationalIdBackImage", nullable = false, length = 200)
    private String nationalIdBackImage;

    // ── GPLX (bổ sung sau — nullable) ───────────────────────────
    // Bước 2: customer bổ sung GPLX sau khi CCCD được duyệt
    @Column(name = "licenseNumber", nullable = true, length = 20)
    private String licenseNumber;

    @Column(name = "frontImage", nullable = true, length = 200)
    private String frontImage;

    @Column(name = "backImage", nullable = true, length = 200)
    private String backImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "licenseStatus", nullable = false, length = 20)
    private LicenseStatus licenseStatus;

    // ── Trạng thái & metadata ────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VerificationStatus status;

    @Column(name = "rejectReason", length = 300)
    private String rejectReason;

    @Column(name = "submittedAt", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewedAt")
    private LocalDateTime reviewedAt;

    @PrePersist
    public void prePersist() {
        this.submittedAt = LocalDateTime.now();
        if (this.status == null)
            this.status = VerificationStatus.Pending;
        if (this.licenseStatus == null)
            this.licenseStatus = LicenseStatus.None;
    }

    @PostLoad
    public void postLoad() {
        if (this.licenseStatus == null)
            this.licenseStatus = LicenseStatus.None;
    }

    // ── Helper methods ───────────────────────────────────────────
    /** CCCD đã được duyệt */
    public boolean isCccdApproved() {
        return VerificationStatus.Approved.equals(this.status);
    }

    /** GPLX đã được admin duyệt */
    public boolean hasLicense() {
        return LicenseStatus.Approved.equals(this.licenseStatus);
    }
}