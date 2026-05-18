package com.carrental.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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

    @Column(name = "role", nullable = false, length = 20)
    private String role = "Customer";

    @Column(name = "status", nullable = false, length = 20)
    private String status = "Active";

    @Column(name = "lockReason", length = 255)
    private String lockReason;

    @CreationTimestamp
    @Column(name = "createdAt")
    private LocalDateTime createdAt;
}