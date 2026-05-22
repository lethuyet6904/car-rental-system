package com.carrental.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class OwnerRegistrationRequest {

    @NotBlank(message = "National ID is required")
    private String nationalId;   // tương ứng cccd

    private MultipartFile nationalIdFrontImage;
    private MultipartFile nationalIdBackImage;
}