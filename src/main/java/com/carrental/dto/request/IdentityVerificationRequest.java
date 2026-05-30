package com.carrental.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * Dùng chung cho cả 2 bước xác minh:
 * - Bước 1 (/verification/cccd) → chỉ cần nationalId + 2 ảnh CCCD
 * - Bước 2 (/verification/license) → chỉ cần licenseNumber + 2 ảnh GPLX
 *
 * Validation thực hiện thủ công ở Controller để linh hoạt theo từng bước.
 */
@Data
public class IdentityVerificationRequest {

    // ── Bước 1: CCCD ──────────────────────────────────────────
    private String nationalId;
    private MultipartFile nationalIdFrontImage;
    private MultipartFile nationalIdBackImage;

    // ── Bước 2: GPLX ──────────────────────────────────────────
    private String licenseNumber;
    private MultipartFile frontImage; // GPLX mặt trước
    private MultipartFile backImage; // GPLX mặt sau
}