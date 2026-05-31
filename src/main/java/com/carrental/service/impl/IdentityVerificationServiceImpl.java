package com.carrental.service.impl;

import com.carrental.dto.request.IdentityVerificationRequest;
import com.carrental.entity.IdentityVerification;
import com.carrental.entity.User;
import com.carrental.enums.VerificationStatus;
import com.carrental.repository.IdentityVerificationRepository;
import com.carrental.repository.UserRepository;
import com.carrental.service.IdentityVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdentityVerificationServiceImpl implements IdentityVerificationService {

    private final IdentityVerificationRepository identityVerificationRepository;
    private final UserRepository userRepository;

    @Value("${upload.path:/uploads}")
    private String uploadPath;

    // ════════════════════════════════════════════════════════════
    // BƯỚC 1: Nộp CCCD
    // ════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public IdentityVerification submitCccd(Long userId, IdentityVerificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra hồ sơ hiện tại
        identityVerificationRepository
                .findTopByUserUserIdOrderBySubmittedAtDesc(userId)
                .ifPresent(iv -> {
                    if (iv.getStatus() == VerificationStatus.Pending) {
                        throw new RuntimeException("Hồ sơ CCCD của bạn đang chờ Admin duyệt, vui lòng chờ.");
                    }
                    if (iv.getStatus() == VerificationStatus.Approved) {
                        throw new RuntimeException("CCCD của bạn đã được xác minh thành công rồi.");
                    }
                    // Rejected → xóa record cũ để tạo mới
                    identityVerificationRepository.delete(iv);
                    identityVerificationRepository.flush();
                });

        String frontPath = saveImage(request.getNationalIdFrontImage(), "cccd/front");
        String backPath = saveImage(request.getNationalIdBackImage(), "cccd/back");

        IdentityVerification iv = IdentityVerification.builder()
                .user(user)
                .nationalId(request.getNationalId().trim())
                .nationalIdFrontImage(frontPath)
                .nationalIdBackImage(backPath)
                // GPLX để null — sẽ bổ sung ở bước 2
                .licenseNumber(null)
                .frontImage(null)
                .backImage(null)
                .status(VerificationStatus.Pending)
                .submittedAt(LocalDateTime.now())
                .build();

        return identityVerificationRepository.save(iv);
    }

    // ════════════════════════════════════════════════════════════
    // BƯỚC 2: Bổ sung GPLX (chỉ sau khi CCCD đã Approved)
    // ════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public IdentityVerification submitLicense(Long userId, IdentityVerificationRequest request) {
        IdentityVerification iv = identityVerificationRepository
                .findTopByUserUserIdOrderBySubmittedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("Bạn cần xác minh CCCD trước khi bổ sung GPLX."));

        if (iv.getStatus() != VerificationStatus.Approved) {
            throw new RuntimeException("CCCD của bạn chưa được Admin duyệt. Vui lòng chờ kết quả trước.");
        }
        if (iv.hasLicense()) {
            throw new RuntimeException("Bạn đã nộp GPLX rồi.");
        }

        String frontPath = saveImage(request.getFrontImage(), "gplx/front");
        String backPath = saveImage(request.getBackImage(), "gplx/back");

        iv.setLicenseNumber(request.getLicenseNumber().trim());
        iv.setFrontImage(frontPath);
        iv.setBackImage(backPath);

        return identityVerificationRepository.save(iv);
    }

    // ════════════════════════════════════════════════════════════
    // QUERY
    // ════════════════════════════════════════════════════════════
    @Override
    public IdentityVerification findLatestByUser(Long userId) {
        return identityVerificationRepository
                .findTopByUserUserIdOrderBySubmittedAtDesc(userId)
                .orElse(null);
    }

    @Override
    public boolean isCccdApproved(Long userId) {
        IdentityVerification iv = findLatestByUser(userId);
        return iv != null && iv.getStatus() == VerificationStatus.Approved;
    }

    @Override
    public boolean isFullyVerified(Long userId) {
        IdentityVerification iv = findLatestByUser(userId);
        return iv != null
                && iv.getStatus() == VerificationStatus.Approved
                && iv.hasLicense();
    }

    @Override
    public IdentityVerification getApprovedByUser(Long userId) {
        IdentityVerification iv = findLatestByUser(userId);
        return (iv != null && iv.getStatus() == VerificationStatus.Approved) ? iv : null;
    }

    // ════════════════════════════════════════════════════════════
    // ADMIN: Duyệt / Từ chối CCCD
    // ════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public IdentityVerification approveCccd(Long verificationId) {
        IdentityVerification iv = findByIdOrThrow(verificationId);
        assertPending(iv);
        iv.setStatus(VerificationStatus.Approved);
        iv.setReviewedAt(LocalDateTime.now());
        return identityVerificationRepository.save(iv);
    }

    @Override
    @Transactional
    public IdentityVerification rejectCccd(Long verificationId, String reason) {
        if (reason == null || reason.isBlank())
            throw new RuntimeException("Vui lòng nhập lý do từ chối");

        IdentityVerification iv = findByIdOrThrow(verificationId);
        assertPending(iv);
        iv.setStatus(VerificationStatus.Rejected);
        iv.setRejectReason(reason.trim());
        iv.setReviewedAt(LocalDateTime.now());
        return identityVerificationRepository.save(iv);
    }

    // ════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════
    private IdentityVerification findByIdOrThrow(Long id) {
        return identityVerificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ xác minh id=" + id));
    }

    private void assertPending(IdentityVerification iv) {
        if (iv.getStatus() != VerificationStatus.Pending) {
            throw new IllegalStateException("Hồ sơ đã được xử lý (trạng thái: " + iv.getStatus() + ")");
        }
    }

    private String saveImage(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty())
            throw new RuntimeException("Vui lòng upload đầy đủ ảnh (" + subDir + ")");

        String ct = file.getContentType();
        if (ct == null || (!ct.equals("image/jpeg") && !ct.equals("image/png") && !ct.equals("image/jpg")))
            throw new RuntimeException("Chỉ chấp nhận file JPG hoặc PNG");

        if (file.getSize() > 5 * 1024 * 1024)
            throw new RuntimeException("File ảnh không được vượt quá 5MB");

        try {
            String original = file.getOriginalFilename();
            if (original == null || !original.contains("."))
                throw new RuntimeException("Tên file không hợp lệ");

            String ext = original.substring(original.lastIndexOf(".")).toLowerCase();
            String filename = UUID.randomUUID() + ext;

            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize().resolve(subDir);
            if (!Files.exists(uploadDir))
                Files.createDirectories(uploadDir);

            Files.copy(file.getInputStream(),
                    uploadDir.resolve(filename),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + subDir + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage());
        }
    }
}