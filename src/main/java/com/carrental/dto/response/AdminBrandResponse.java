package com.carrental.dto.response;

import com.carrental.entity.Brand;
import lombok.Getter;

@Getter
public class AdminBrandResponse {
    private final Long id;
    private final String name;
    private final String logoUrl;
    private final String status;
    private final long carCount;

    private AdminBrandResponse(Brand b, long carCount) {
        this.id       = b.getBrandId();
        this.name     = b.getBrandName();
        this.logoUrl  = b.getLogo();
        this.status   = b.getStatus().name();
        this.carCount = carCount;
    }

    public static AdminBrandResponse from(Brand b, long carCount) {
        return new AdminBrandResponse(b, carCount);
    }
}