package com.carrental.controller;

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
@RequestMapping("/admin/owner-requests")
@PreAuthorize("hasRole('Admin')")
@RequiredArgsConstructor
public class AdminOwnerRequestController {

    private static final int PAGE_SIZE = 10;

    @Getter
    @AllArgsConstructor
    private static class MockOwnerRequest {
        private Long registrationId;
        private Long userId;
        private String fullName;
        private String phone;
        private LocalDateTime submittedAt;
        private VerificationStatus status;
        private String rejectReason;
        // CCCD info
        private String nationalId;
        private String nationalIdFrontImage;
        private String nationalIdBackImage;
    }

    // ── GET: Danh sách yêu cầu ───────────────────────────────────
    @GetMapping
    public String list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        List<MockOwnerRequest> mockList = List.of(
            new MockOwnerRequest(1L, 4L, "Phạm Tuấn",   "0933222333", LocalDateTime.of(2025,5,19,14,30), VerificationStatus.Pending,  null, "079303045678", null, null),
            new MockOwnerRequest(2L, 5L, "Hoàng Văn D", "0944333444", LocalDateTime.of(2025,5,17,9,15),  VerificationStatus.Pending,  null, "079404056789", null, null),
            new MockOwnerRequest(3L, 6L, "Nguyễn Thị E","0955444555", LocalDateTime.of(2025,5,12,11,0),  VerificationStatus.Approved, null, "079505067890", null, null),
            new MockOwnerRequest(4L, 7L, "Lý Văn F",    "0966555666", LocalDateTime.of(2025,5,10,8,30),  VerificationStatus.Rejected, "Thông tin CCCD không hợp lệ", "079606078901", null, null)
        );

        Page<MockOwnerRequest> pageResult = new PageImpl<>(
            mockList, PageRequest.of(page, PAGE_SIZE), mockList.size()
        );

        model.addAttribute("requests",    pageResult);
        model.addAttribute("status",      status);
        model.addAttribute("keyword",     keyword);
        model.addAttribute("statuses",    VerificationStatus.values());
        model.addAttribute("extraParams", buildExtraParams(status, keyword));

        return "pages/admin/owner-request-list";
    }

    // ── GET: Chi tiết yêu cầu ────────────────────────────────────
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("request", buildMockDetail(id));
        return "pages/admin/owner-request-detail";
    }

    // ── POST: Duyệt ──────────────────────────────────────────────
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        // TODO: adminOwnerRequestService.approve(id)
        ra.addFlashAttribute("successMessage", "Đã duyệt thành công. Người dùng đã được nâng lên Owner.");
        return "redirect:/admin/owner-requests/" + id;
    }

    // ── POST: Từ chối ─────────────────────────────────────────────
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam String rejectReason,
                         RedirectAttributes ra) {
        if (rejectReason == null || rejectReason.isBlank()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng nhập lý do từ chối");
            return "redirect:/admin/owner-requests/" + id;
        }
        // TODO: adminOwnerRequestService.reject(id, rejectReason)
        ra.addFlashAttribute("successMessage", "Đã từ chối yêu cầu");
        return "redirect:/admin/owner-requests/" + id;
    }

    // ── Mock helpers ──────────────────────────────────────────────
    private MockOwnerRequest buildMockDetail(Long id) {
        return switch (id.intValue()) {
            case 2 -> new MockOwnerRequest(
                2L, 5L, "Hoàng Văn D", "0944333444",
                LocalDateTime.of(2025,5,17,9,15),
                VerificationStatus.Pending, null,
                "079404056789", null, null
            );
            case 3 -> new MockOwnerRequest(
                3L, 6L, "Nguyễn Thị E", "0955444555",
                LocalDateTime.of(2025,5,12,11,0),
                VerificationStatus.Approved, null,
                "079505067890", null, null
            );
            case 4 -> new MockOwnerRequest(
                4L, 7L, "Lý Văn F", "0966555666",
                LocalDateTime.of(2025,5,10,8,30),
                VerificationStatus.Rejected,
                "Thông tin CCCD không hợp lệ",
                "079606078901", null, null
            );
            default -> new MockOwnerRequest(
                1L, 4L, "Phạm Tuấn", "0933222333",
                LocalDateTime.of(2025,5,19,14,30),
                VerificationStatus.Pending, null,
                "079303045678", null, null
            );
        };
    }

    private String buildExtraParams(String status, String keyword) {
        StringBuilder sb = new StringBuilder();
        if (status  != null && !status.isBlank())  sb.append("&status=").append(status);
        if (keyword != null && !keyword.isBlank()) sb.append("&keyword=").append(keyword);
        return sb.toString();
    }
}