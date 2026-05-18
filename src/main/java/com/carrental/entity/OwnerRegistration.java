package com.carrental.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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

    @Column(name = "status", nullable = false, length = 20)
    private String status = "Pending";

    @Column(name = "rejectReason", length = 300)
    private String rejectReason;

    @CreationTimestamp
    @Column(name = "submittedAt", updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewedAt")
    private LocalDateTime reviewedAt;
}