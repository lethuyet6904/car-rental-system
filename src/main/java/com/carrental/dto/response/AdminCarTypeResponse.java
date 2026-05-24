package com.carrental.dto.response;

import com.carrental.entity.CarType;
import lombok.Getter;

@Getter
public class AdminCarTypeResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final String status;
    private final long carCount;

    private AdminCarTypeResponse(CarType t, long carCount) {
        this.id          = t.getCarTypeId();
        this.name        = t.getTypeName();
        this.description = t.getDescription();
        this.status      = t.getStatus().name();
        this.carCount    = carCount;
    }

    public static AdminCarTypeResponse from(CarType t, long carCount) {
        return new AdminCarTypeResponse(t, carCount);
    }
}