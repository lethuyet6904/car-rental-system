package com.carrental.dto.response;

import com.carrental.entity.Region;
import lombok.Getter;

@Getter
public class AdminRegionResponse {
    private final Long id;
    private final String name;
    private final String status;
    private final long activeCarCount;

    private AdminRegionResponse(Region r, long activeCarCount) {
        this.id            = r.getRegionId();
        this.name          = r.getRegionName();
        this.status        = r.getStatus().name();
        this.activeCarCount = activeCarCount;
    }

    public static AdminRegionResponse from(Region r, long activeCarCount) {
        return new AdminRegionResponse(r, activeCarCount);
    }
}