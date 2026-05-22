package com.carrental.dto.request;

import com.carrental.enums.CategoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarTypeRequest {
    @NotBlank(message = "Tên loại xe không được để trống")
    private String typeName;

    private String description; // (Có thể cho phép null nếu không bắt buộc)

    @NotNull(message = "Trạng thái không được để trống")
    private CategoryStatus status;
}
