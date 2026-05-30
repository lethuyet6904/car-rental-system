package com.carrental.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.carrental.enums.VerificationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "OwnerRegistration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registrationId")
    private Long registrationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identity_verification_id")
    private IdentityVerification identityVerification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VerificationStatus status;

    @Column(name = "rejectReason", length = 300)
    private String rejectReason;

    @CreationTimestamp
    @Column(name = "submittedAt", updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewedAt")
    private LocalDateTime reviewedAt;

    @Column(name = "bankName", length = 100)
    private String bankName;

    @Column(name = "bankAccount", length = 50)
    private String bankAccount;

    @Column(name = "accountHolder", length = 100)
    private String accountHolder;

    @PrePersist
    public void prePersist() {
        this.submittedAt = LocalDateTime.now();
        if (this.status == null) this.status = VerificationStatus.Pending;
    }
}