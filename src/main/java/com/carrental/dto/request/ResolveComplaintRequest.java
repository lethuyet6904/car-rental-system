package com.carrental.dto.request;

import com.carrental.enums.ComplaintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResolveComplaintRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private ComplaintStatus status; // Resolved hoặc Rejected

    @NotBlank(message = "Nội dung xử lý không được để trống")
    private String resolution;
}