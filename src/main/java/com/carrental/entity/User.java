package com.carrental.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.carrental.enums.UserRole;
import com.carrental.enums.UserStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "[User]")   // ← Quan trọng nhất
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long userId;

    @Column(name = "fullName", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone", nullable = false, unique = true, length = 15)
    private String phone;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "address", length = 255)
    private String address;

//    @Column(name = "nationalId", length = 20)
//    private String nationalId;

    @Column(name = "avatar", length = 500)
    private String avatar;

    // varchar(20) NOT NULL, CHECK: Customer/Owner/Admin
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    // varchar(20) NOT NULL, CHECK: Active/Locked
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "lockReason", length = 255)
    private String lockReason;

    @CreationTimestamp
    @Column(name = "createdAt")
    private LocalDateTime createdAt;
    
 // Tự động gán createdAt trước khi INSERT
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        // Default status khi mới tạo
        if (this.status == null) this.status = UserStatus.Active;
        // Default role khi đăng ký bình thường
        if (this.role == null) this.role = UserRole.Customer;
    }
}