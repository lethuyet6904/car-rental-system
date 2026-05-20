package com.carrental.dto.request;

import com.carrental.enums.CategoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Tên không được để trống")
    private String name;        // brandName / typeName / regionName

    private String description; // Chỉ CarType dùng, Brand và Region bỏ qua

    private String logo;        // Chỉ Brand dùng

    @NotNull(message = "Trạng thái không được để trống")
    private CategoryStatus status;
}