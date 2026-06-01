-- Thêm cột licenseStatus cho bảng IdentityVerification
ALTER TABLE IdentityVerification
    ADD COLUMN licenseStatus VARCHAR(20) NOT NULL DEFAULT 'None';

-- Dữ liệu cũ: GPLX đã nộp nhưng chưa có trạng thái → chuyển sang Pending
UPDATE IdentityVerification
SET licenseStatus = 'Pending'
WHERE licenseNumber IS NOT NULL
  AND licenseNumber <> ''
  AND licenseStatus = 'None';
