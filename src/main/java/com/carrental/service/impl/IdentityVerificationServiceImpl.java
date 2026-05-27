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

    // ====================== NỘP HỒ SƠ XÁC MINH ======================
    @Override
    @Transactional
    public IdentityVerification submitVerification(Long userId, IdentityVerificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra trạng thái hồ sơ hiện tại
        identityVerificationRepository
                .findTopByUserUserIdOrderBySubmittedAtDesc(userId)
                .ifPresent(iv -> {
                    switch (iv.getStatus()) {
                        case Pending -> throw new RuntimeException(
                                "Bạn đã có hồ sơ đang chờ duyệt! Vui lòng chờ admin xử lý.");
                        case Approved -> throw new RuntimeException(
                                "Danh tính của bạn đã được xác minh thành công!");
                        // Nếu Rejected → cho phép nộp lại bình thường, không throw
                        default -> {}
                    }
                });

        // Lưu ảnh CCCD và GPLX
        String nationalIdFrontPath = saveImage(request.getNationalIdFrontImage(), "cccd/front");
        String nationalIdBackPath  = saveImage(request.getNationalIdBackImage(),  "cccd/back");
        String frontImagePath      = saveImage(request.getFrontImage(),            "gplx/front");
        String backImagePath       = saveImage(request.getBackImage(),             "gplx/back");

        IdentityVerification verification = IdentityVerification.builder()
                .user(user)
                .nationalId(request.getNationalId())
                .nationalIdFrontImage(nationalIdFrontPath)
                .nationalIdBackImage(nationalIdBackPath)
                .licenseNumber(request.getLicenseNumber())
                .frontImage(frontImagePath)
                .backImage(backImagePath)
                .status(VerificationStatus.Pending)
                .submittedAt(LocalDateTime.now())
                .build();

        return identityVerificationRepository.save(verification);
    }

    // ====================== HELPER: Lưu ảnh lên server ======================
    private String saveImage(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Vui lòng upload đầy đủ ảnh (" + subDir + ")");
        }

        // Kiểm tra loại file
        String contentType = file.getContentType();
        if (contentType == null ||
            (!contentType.equals("image/jpeg") &&
             !contentType.equals("image/png") &&
             !contentType.equals("image/jpg"))) {
            throw new RuntimeException("Chỉ chấp nhận file ảnh JPG hoặc PNG");
        }

        // Kiểm tra kích thước (tối đa 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File ảnh không được vượt quá 5MB");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                throw new RuntimeException("Tên file không hợp lệ");
            }
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            String filename = UUID.randomUUID() + extension;

            // Dùng đường dẫn tuyệt đối (absolute path) để tránh lỗi với embedded Tomcat
            // Paths.get(uploadPath) nếu uploadPath là relative (vd: "uploads") sẽ resolve
            // từ thư mục làm việc hiện tại của JVM (project root), không phải temp của Tomcat
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize().resolve(subDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(filename);

            // Dùng Files.copy thay vì transferTo() để tránh lỗi FileNotFoundException
            // với embedded Tomcat trên Windows
            Files.copy(file.getInputStream(), filePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + subDir + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage());
        }
    }

    // ====================== QUERY ======================
    @Override
    public IdentityVerification findLatestByUser(Long userId) {
        return identityVerificationRepository
                .findTopByUserUserIdOrderBySubmittedAtDesc(userId)
                .orElse(null);
    }

    @Override
    public boolean isIdentityVerified(Long userId) {
        return identityVerificationRepository
                .findTopByUserUserIdOrderBySubmittedAtDesc(userId)
                .map(iv -> iv.getStatus() == VerificationStatus.Approved)
                .orElse(false);
    }

    @Override
    public IdentityVerification getVerifiedIdentityByUser(Long userId) {
        return identityVerificationRepository
                .findTopByUserUserIdOrderBySubmittedAtDesc(userId)
                .filter(iv -> iv.getStatus() == VerificationStatus.Approved)
                .orElse(null);
    }

    // ====================== ADMIN: Duyệt / Từ chối ======================
    @Override
    @Transactional
    public IdentityVerification approveVerification(Long verificationId) {
        IdentityVerification verification = identityVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ xác minh id=" + verificationId));

        if (verification.getStatus() != VerificationStatus.Pending) {
            throw new IllegalStateException("Hồ sơ này đã được xử lý rồi (trạng thái: "
                    + verification.getStatus() + ")");
        }

        verification.setStatus(VerificationStatus.Approved);
        verification.setReviewedAt(LocalDateTime.now());

        return identityVerificationRepository.save(verification);
    }

    @Override
    @Transactional
    public IdentityVerification rejectVerification(Long verificationId, String reason) {
        IdentityVerification verification = identityVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ xác minh id=" + verificationId));

        if (verification.getStatus() != VerificationStatus.Pending) {
            throw new IllegalStateException("Hồ sơ này đã được xử lý rồi (trạng thái: "
                    + verification.getStatus() + ")");
        }

        if (reason == null || reason.isBlank()) {
            throw new RuntimeException("Vui lòng nhập lý do từ chối");
        }

        verification.setStatus(VerificationStatus.Rejected);
        verification.setRejectReason(reason.trim());
        verification.setReviewedAt(LocalDateTime.now());

        return identityVerificationRepository.save(verification);
    }
}