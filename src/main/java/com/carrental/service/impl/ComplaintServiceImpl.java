package com.carrental.service.impl;

import com.carrental.dto.response.ComplaintResponse;
import com.carrental.entity.Complaint;
import com.carrental.entity.RentalOrder;
import com.carrental.entity.User;
import com.carrental.enums.ComplaintStatus;
import com.carrental.enums.ComplaintType;
import com.carrental.enums.OrderStatus;
import com.carrental.enums.SenderRole;
import com.carrental.repository.ComplaintRepository;
import com.carrental.repository.RentalOrderRepository;
import com.carrental.service.ComplaintService;
import com.carrental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final RentalOrderRepository rentalOrderRepository;
    private final UserService userService;

    @Value("${upload.path:/uploads}")
    private String uploadPath;

    @Override
    @Transactional
    public Long submitComplaint(Long orderId, String type, String content,
                                List<MultipartFile> evidenceImages, Authentication auth) {
        User user = userService.findByPhone(auth.getName());
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }

        RentalOrder order = rentalOrderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        SenderRole senderRole = resolveSenderRole(order, user);

        validateOrderStatus(order.getStatus());

        complaintRepository
                .findByOrderAndSenderOrderByCreatedAtDesc(orderId, user.getUserId())
                .stream()
                .findFirst()
                .ifPresent(existing -> {
                    if (existing.getStatus() != ComplaintStatus.Rejected) {
                        throw new RuntimeException("Bạn đã gửi khiếu nại cho đơn hàng này rồi");
                    }
                });

        ComplaintType complaintType;
        try {
            complaintType = ComplaintType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Loại khiếu nại không hợp lệ");
        }

        if (content == null || content.isBlank()) {
            throw new RuntimeException("Vui lòng nhập nội dung khiếu nại");
        }
        if (content.length() > 500) {
            throw new RuntimeException("Nội dung khiếu nại không được vượt quá 500 ký tự");
        }

        String evidencePaths = saveEvidenceImages(evidenceImages, orderId);

        Complaint complaint = Complaint.builder()
                .rentalOrder(order)
                .sender(user)
                .senderRole(senderRole)
                .type(complaintType)
                .content(content.trim())
                .evidenceImages(evidencePaths)
                .status(ComplaintStatus.Pending)
                .build();

        return complaintRepository.save(complaint).getComplaintId();
    }

    @Override
    public Page<ComplaintResponse> getMyComplaints(Authentication auth, Pageable pageable) {
        User user = requireUser(auth);
        return complaintRepository.findBySenderUserId(user.getUserId(), pageable)
                .map(ComplaintResponse::from);
    }

    @Override
    public ComplaintResponse getMyComplaintDetail(Long complaintId, Authentication auth) {
        User user = requireUser(auth);
        Complaint complaint = complaintRepository.findWithDetailsByIdAndSender(complaintId, user.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khiếu nại hoặc bạn không có quyền xem"));
        return ComplaintResponse.from(complaint);
    }

    @Override
    public Optional<ComplaintResponse> findByOrderAndSender(Long orderId, Long userId) {
        return complaintRepository
                .findByOrderAndSenderOrderByCreatedAtDesc(orderId, userId)
                .stream()
                .findFirst()
                .map(ComplaintResponse::from);
    }

    private User requireUser(Authentication auth) {
        User user = userService.findByPhone(auth.getName());
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }
        return user;
    }

    private SenderRole resolveSenderRole(RentalOrder order, User user) {
        if (order.getCustomer().getUserId().equals(user.getUserId())) {
            return SenderRole.Customer;
        }
        if (order.getCar().getOwner().getUserId().equals(user.getUserId())) {
            return SenderRole.Owner;
        }
        throw new RuntimeException("Bạn không có quyền khiếu nại đơn hàng này");
    }

    private void validateOrderStatus(OrderStatus status) {
        if (status == OrderStatus.InProgress || status == OrderStatus.Completed) {
            return;
        }
        if (status == OrderStatus.Cancelled || status == OrderStatus.Rejected) {
            throw new RuntimeException("Không thể khiếu nại đơn hàng đã huỷ");
        }
        throw new RuntimeException("Chỉ có thể khiếu nại khi chuyến đi đang diễn ra hoặc đã kết thúc");
    }

    private String saveEvidenceImages(List<MultipartFile> files, Long orderId) {
        if (files == null || files.isEmpty()) {
            return null;
        }

        List<String> paths = new ArrayList<>();
        int count = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            if (count >= 3) {
                break;
            }
            paths.add(saveImage(file, "complaint/" + orderId));
            count++;
        }
        return paths.isEmpty() ? null : String.join(",", paths);
    }

    private String saveImage(MultipartFile file, String subDir) {
        String ct = file.getContentType();
        if (ct == null || (!ct.equals("image/jpeg") && !ct.equals("image/png") && !ct.equals("image/jpg"))) {
            throw new RuntimeException("Chỉ chấp nhận file JPG hoặc PNG");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File ảnh không được vượt quá 5MB");
        }

        try {
            String original = file.getOriginalFilename();
            if (original == null || !original.contains(".")) {
                throw new RuntimeException("Tên file không hợp lệ");
            }

            String ext = original.substring(original.lastIndexOf(".")).toLowerCase();
            String filename = UUID.randomUUID() + ext;

            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize().resolve(subDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Files.copy(file.getInputStream(),
                    uploadDir.resolve(filename),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + subDir + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage());
        }
    }
}
