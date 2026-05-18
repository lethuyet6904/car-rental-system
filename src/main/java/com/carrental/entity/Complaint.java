package com.carrental.entity;

import com.carrental.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Complaint")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaintId")
    private Long complaintId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    private RentalOrder rentalOrder;

    // FK → User (người gửi khiếu nại — Customer hoặc Owner)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senderId", nullable = false)
    private User sender;

    // varchar(20) CHECK: Customer/Owner — lưu thêm để truy vấn nhanh
    @Enumerated(EnumType.STRING)
    @Column(name = "senderRole", nullable = false, length = 20)
    private SenderRole senderRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ComplaintType type;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    // varchar(500) NULL — URL ảnh bằng chứng, lưu dạng "url1,url2,url3"
    @Column(name = "evidenceImages", length = 500)
    private String evidenceImages;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ComplaintStatus status;

    // nvarchar(500) NULL — kết quả xử lý từ Admin
    @Column(name = "resolution", length = 500)
    private String resolution;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolvedAt")
    private LocalDateTime resolvedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = ComplaintStatus.Pending;
    }
}