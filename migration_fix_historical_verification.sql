-- Migration script: Add identity_verification_id to OwnerRegistration

ALTER TABLE OwnerRegistration 
ADD identity_verification_id bigint NULL;

ALTER TABLE OwnerRegistration 
ADD CONSTRAINT FK_OwnerRegistration_IdentityVerification 
FOREIGN KEY (identity_verification_id) REFERENCES IdentityVerification(verificationId);

-- Backfill data (Optional)
-- Lấy IdentityVerification mới nhất của mỗi user gán vào những bản ghi OwnerRegistration hiện có để không bị crash khi view.
UPDATE or_table
SET or_table.identity_verification_id = latest_iv.verificationId
FROM OwnerRegistration or_table
CROSS APPLY (
    SELECT TOP 1 iv.verificationId
    FROM IdentityVerification iv
    WHERE iv.userId = or_table.userId
    ORDER BY iv.submittedAt DESC
) latest_iv
WHERE or_table.identity_verification_id IS NULL;
