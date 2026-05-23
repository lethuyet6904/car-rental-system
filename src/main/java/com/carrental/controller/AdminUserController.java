package com.carrental.controller;

import com.carrental.dto.request.LockAccountRequest;
import com.carrental.enums.UserRole;
import com.carrental.enums.UserStatus;
import com.carrental.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('Admin')")
@RequiredArgsConstructor
public class AdminUserController {

    private static final int PAGE_SIZE = 10;

    // ── Mock list DTO ─────────────────────────────────────────────
    @Getter
    @AllArgsConstructor
    private static class MockUserItem {
        private Long userId;
        private String fullName;
        private String phone;
        private String email;
        private UserRole role;
        private UserStatus status;
        private LocalDateTime createdAt;
    }

    // ── Mock detail DTO ───────────────────────────────────────────
    @Getter
    @AllArgsConstructor
    private static class MockUserDetail {
        private Long userId;
        private String fullName;
        private String phone;
        private String email;
        private String address;
        private String avatar;
        private UserRole role;
        private UserStatus status;
        private String lockReason;
        private LocalDateTime createdAt;
        // Identity
        private VerificationStatus identityStatus;
        private String nationalId;
        private String licenseNumber;
        private String nationalIdFrontImage;
        private String nationalIdBackImage;
        private String licenseFrontImage;
        private String licenseBackImage;
        // OwnerRegistration
        private VerificationStatus ownerRegStatus;
        private String ownerRegRejectReason;
        private LocalDateTime ownerRegSubmittedAt;
    }

    // ── GET: Danh sách ───────────────────────────────────────────
    @GetMapping
    public String userList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        List<MockUserItem> mockUsers = List.of(
            new MockUserItem(1L, "Nguyễn Anh", "0912345678", "anh@gmail.com",  UserRole.Customer, UserStatus.Active,  LocalDateTime.of(2025,1,10,0,0)),
            new MockUserItem(2L, "Trần Hùng",  "0987654321", "hung@gmail.com", UserRole.Owner,    UserStatus.Active,  LocalDateTime.of(2025,3,5,0,0)),
            new MockUserItem(3L, "Lê Mai",     "0971111222", "mai@gmail.com",  UserRole.Customer, UserStatus.Locked,  LocalDateTime.of(2025,4,20,0,0)),
            new MockUserItem(4L, "Phạm Tuấn",  "0933222333", "tuan@gmail.com", UserRole.Customer, UserStatus.Active,  LocalDateTime.of(2025,5,1,0,0)),
            new MockUserItem(5L, "Bùi Văn C",  "0944333444", "bui@gmail.com",  UserRole.Customer, UserStatus.Active,  LocalDateTime.of(2025,5,10,0,0))
        );

        Page<MockUserItem> pageResult = new PageImpl<>(
            mockUsers, PageRequest.of(page, PAGE_SIZE), mockUsers.size()
        );

        model.addAttribute("users",       pageResult);
        model.addAttribute("keyword",     keyword);
        model.addAttribute("role",        role);
        model.addAttribute("status",      status);
        model.addAttribute("roles",       UserRole.values());
        model.addAttribute("statuses",    UserStatus.values());
        model.addAttribute("extraParams", buildExtraParams(keyword, role, status));

        return "pages/admin/user-list";
    }

    // ── GET: Chi tiết người dùng ─────────────────────────────────
    @GetMapping("/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        model.addAttribute("user", buildMockUserDetail(id));
        return "pages/admin/user-detail";
    }

    // ── GET: Xác minh danh tính ──────────────────────────────────
    @GetMapping("/{id}/identity")
    public String identityDetail(@PathVariable Long id, Model model) {
        model.addAttribute("user", buildMockUserDetail(id));
        return "pages/admin/identity-detail";
    }

    // ── POST: Khóa tài khoản ─────────────────────────────────────
    @PostMapping("/{id}/lock")
    public String lockAccount(@PathVariable Long id,
                              @ModelAttribute LockAccountRequest request,
                              RedirectAttributes ra) {
        // TODO: adminUserService.lockAccount(id, request)
        ra.addFlashAttribute("successMessage", "Đã khóa tài khoản thành công");
        return "redirect:/admin/users/" + id;
    }

    // ── POST: Mở khóa tài khoản ──────────────────────────────────
    @PostMapping("/{id}/unlock")
    public String unlockAccount(@PathVariable Long id, RedirectAttributes ra) {
        // TODO: adminUserService.unlockAccount(id)
        ra.addFlashAttribute("successMessage", "Đã mở khóa tài khoản thành công");
        return "redirect:/admin/users/" + id;
    }

    // ── POST: Duyệt xác minh danh tính ──────────────────────────
    @PostMapping("/{id}/identity/approve")
    public String approveIdentity(@PathVariable Long id, RedirectAttributes ra) {
        // TODO: adminUserService.approveIdentity(id)
        ra.addFlashAttribute("successMessage", "Đã duyệt xác minh danh tính thành công");
        return "redirect:/admin/users/" + id;
    }

    // ── POST: Từ chối xác minh danh tính ────────────────────────
    @PostMapping("/{id}/identity/reject")
    public String rejectIdentity(@PathVariable Long id,
                                 @RequestParam String rejectReason,
                                 RedirectAttributes ra) {
        if (rejectReason == null || rejectReason.isBlank()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng nhập lý do từ chối");
            return "redirect:/admin/users/" + id + "/identity";
        }
        // TODO: adminUserService.rejectIdentity(id, rejectReason)
        ra.addFlashAttribute("successMessage", "Đã từ chối xác minh danh tính");
        return "redirect:/admin/users/" + id;
    }

    // ── Mock helpers ─────────────────────────────────────────────
    private MockUserDetail buildMockUserDetail(Long id) {
        return switch (id.intValue()) {
            case 2 -> new MockUserDetail(
                2L, "Trần Hùng", "0987654321", "hung@gmail.com",
                "45 Lê Lợi, Q.1, TP.HCM", null,
                UserRole.Owner, UserStatus.Active, null,
                LocalDateTime.of(2025,3,5,0,0),
                // Đã xác minh → hiện CCCD/GPLX
                VerificationStatus.Approved,
                "079202012345", "GP-2024-056789",
                null, null, null, null,
                null, null, null
            );
            case 3 -> new MockUserDetail(
                3L, "Lê Mai", "0971111222", "mai@gmail.com",
                null, null,
                UserRole.Customer, UserStatus.Locked,
                "Vi phạm điều khoản sử dụng",
                LocalDateTime.of(2025,4,20,0,0),
                // Chưa xác minh
                null, null, null, null, null, null, null,
                null, null, null
            );
            case 4 -> new MockUserDetail(
                4L, "Phạm Tuấn", "0933222333", "tuan@gmail.com",
                "33 Hai Bà Trưng, Q.1, TP.HCM", null,
                UserRole.Customer, UserStatus.Active, null,
                LocalDateTime.of(2025,5,1,0,0),
                // Đã xác minh + có pending owner request
                VerificationStatus.Approved,
                "079303045678", "GP-2025-001122",
                null, null, null, null,
                VerificationStatus.Pending, null,
                LocalDateTime.of(2025,5,19,14,30)
            );
            // default = id 1 và các id khác
            default -> new MockUserDetail(
                id, "Nguyễn Anh", "0912345678", "anh@gmail.com",
                "12 Nguyễn Huệ, Q.1, TP.HCM", null,
                UserRole.Customer, UserStatus.Active, null,
                LocalDateTime.of(2025,1,10,0,0),
                // Chờ xác minh → hiện nút "Duyệt xác minh danh tính"
                VerificationStatus.Pending,
                null, null, null, null, null, null,
                null, null, null
            );
        };
    }

    private String buildExtraParams(String keyword, String role, String status) {
        StringBuilder sb = new StringBuilder();
        if (keyword != null && !keyword.isBlank()) sb.append("&keyword=").append(keyword);
        if (role    != null && !role.isBlank())    sb.append("&role=").append(role);
        if (status  != null && !status.isBlank())  sb.append("&status=").append(status);
        return sb.toString();
    }
}