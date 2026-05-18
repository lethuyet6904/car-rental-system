package com.carrental.entity;

import com.carrental.enums.CategoryStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Region")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "regionId")
    private Long regionId;

    // nvarchar(100) NOT NULL
    @Column(name = "regionName", nullable = false, length = 100)
    private String regionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CategoryStatus status;
}