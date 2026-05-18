package com.carrental.entity;

import com.carrental.enums.CategoryStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Brand")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brandId")
    private Long brandId;

    // nvarchar(50) NOT NULL → nullable = false, length = 50
    @Column(name = "brandName", nullable = false, length = 50)
    private String brandName;

    // varchar(200) NULL → nullable = true (default)
    @Column(name = "logo", length = 200)
    private String logo;

    // varchar(20) NOT NULL, CHECK: Active/Hidden
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CategoryStatus status;
}