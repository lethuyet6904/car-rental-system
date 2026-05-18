package com.carrental.entity;

import com.carrental.enums.RentalImageType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RentalImage")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RentalImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imageId")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    private RentalOrder rentalOrder;

    @Column(name = "imageUrl", nullable = false, length = 200)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "imageType", nullable = false, length = 20)
    private RentalImageType imageType;

    @Column(name = "sortOrder", nullable = false)
    private Integer sortOrder;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}