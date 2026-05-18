package com.carrental.entity;

import com.carrental.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "IdentityVerification")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class IdentityVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verificationId")
    private Long verificationId;

    // FK → User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    // varchar(20) NOT NULL — số CCCD/CMND
    @Column(name = "nationalId", nullable = false, length = 20)
    private String nationalId;

    // Ảnh CCCD mặt trước
    @Column(name = "nationalIdFrontImage", nullable = false, length = 200)
    private String nationalIdFrontImage;

    // Ảnh CCCD mặt sau
    @Column(name = "nationalIdBackImage", nullable = false, length = 200)
    private String nationalIdBackImage;

    // Số GPLX
    @Column(name = "licenseNumber", nullable = false, length = 20)
    private String licenseNumber;

    // Ảnh GPLX mặt trước
    @Column(name = "frontImage", nullable = false, length = 200)
    private String frontImage;

    // Ảnh GPLX mặt sau
    @Column(name = "backImage", nullable = false, length = 200)
    private String backImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VerificationStatus status;

    // nvarchar(300) NULL — lý do từ chối
    @Column(name = "rejectReason", length = 300)
    private String rejectReason;

    // datetime NOT NULL — ngày nộp hồ sơ
    @Column(name = "submittedAt", nullable = false)
    private LocalDateTime submittedAt;

    // datetime NULL — ngày Admin xem xét
    @Column(name = "reviewedAt")
    private LocalDateTime reviewedAt;

    @PrePersist
    public void prePersist() {
        this.submittedAt = LocalDateTime.now();
        if (this.status == null) this.status = VerificationStatus.Pending;
    }
}