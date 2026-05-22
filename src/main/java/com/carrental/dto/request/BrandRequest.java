package com.carrental.dto.request;

import com.carrental.enums.CategoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BrandRequest {
    @NotBlank(message = "Tên hãng xe không được để trống")
    private String brandName;

    @NotBlank(message = "Logo không được để trống")
    private String logo;

    @NotNull(message = "Trạng thái không được để trống")
    private CategoryStatus status;
}
