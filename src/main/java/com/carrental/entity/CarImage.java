package com.carrental.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CarImage")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CarImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imageId")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carId", nullable = false)
    private Car car;

    @Column(name = "imageUrl", nullable = false, length = 200)
    private String imageUrl;

    // Thứ tự hiển thị ảnh (ảnh đầu tiên = ảnh bìa)
    @Column(name = "sortOrder", nullable = false)
    private Integer sortOrder;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}