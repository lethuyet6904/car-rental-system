package com.carrental.entity;

import com.carrental.enums.CategoryStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CarType")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CarType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carTypeId")
    private Long carTypeId;

    @Column(name = "typeName", nullable = false, length = 50)
    private String typeName;

    // nvarchar(200) NULL
    @Column(name = "description", length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CategoryStatus status;
}