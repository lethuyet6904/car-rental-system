package com.carrental.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LockAccountRequest {

    @NotBlank(message = "Lý do khóa không được để trống")
    private String reason;
}