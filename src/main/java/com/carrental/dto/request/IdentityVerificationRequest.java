package com.carrental.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class IdentityVerificationRequest {

    @NotBlank(message = "Số CCCD/CMND không được để trống")
    @Pattern(regexp = "^[0-9]{9,12}$", message = "Số CCCD/CMND không hợp lệ (9-12 số)")
    private String nationalId;

    @NotBlank(message = "Số GPLX không được để trống")
    @Pattern(regexp = "^[A-Za-z0-9]{8,15}$", message = "Số GPLX không hợp lệ")
    private String licenseNumber;

    // Ảnh CCCD mặt trước
    private MultipartFile nationalIdFrontImage;

    // Ảnh CCCD mặt sau
    private MultipartFile nationalIdBackImage;

    // Ảnh GPLX mặt trước
    private MultipartFile frontImage;

    // Ảnh GPLX mặt sau
    private MultipartFile backImage;
}