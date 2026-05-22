package com.carrental.dto.request;

import com.carrental.enums.CategoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegionRequest {
    @NotBlank(message = "Tên khu vực không được để trống")
    private String regionName;

    @NotNull(message = "Trạng thái không được để trống")
    private CategoryStatus status;
}
