package com.carrental.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class OwnerRegistrationRequest {

    @NotBlank(message = "Tên ngân hàng không được để trống")
    private String bankName;

    @NotBlank(message = "Số tài khoản không được để trống")
    private String bankAccount;

    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    private String accountHolder;
}