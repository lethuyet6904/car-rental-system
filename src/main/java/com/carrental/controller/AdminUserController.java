package com.carrental.controller;

import com.carrental.dto.request.LockAccountRequest;
import com.carrental.enums.UserRole;
import com.carrental.enums.UserStatus;
import com.carrental.enums.VerificationStatus;
import com.carrental.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('Admin')")
@RequiredArgsConstructor
public class AdminUserController {

    private static final int PAGE_SIZE = 10;
    private final AdminUserService adminUserService;

    // ── GET: Danh sách ───────────────────────────────────────────
    @GetMapping
    public String userList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String identityStatus,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        UserRole   roleEnum   = parseEnum(UserRole.class,   role);
        UserStatus statusEnum = parseEnum(UserStatus.class, status);
        // identityStatus giữ nguyên String vì có giá trị đặc biệt: None, ApprovedNoLicense
        String identityStatusStr = (identityStatus != null && !identityStatus.isBlank()) ? identityStatus : null;

        var pageResult = adminUserService.getUserList(
                keyword, roleEnum, statusEnum, identityStatusStr,
                PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending()));

        model.addAttribute("users",       pageResult);
        model.addAttribute("keyword",     keyword);
        model.addAttribute("role",        role);
        model.addAttribute("status",      status);
        model.addAttribute("identityStatus", identityStatus);
        model.addAttribute("roles",       UserRole.values());
        model.addAttribute("statuses",    UserStatus.values());
        model.addAttribute("extraParams", buildExtraParams(keyword, role, status, identityStatus));

        return "pages/admin/user-list";
    }

    // ── GET: Chi tiết ────────────────────────────────────────────
    @GetMapping("/{id}")
    public String userDetail(@PathVariable Long id, @RequestParam(defaultValue = "0") int returnPage, Model model) {
        model.addAttribute("user", adminUserService.getUserDetail(id));
        model.addAttribute("returnPage", returnPage);
        return "pages/admin/user-detail";
    }

    // ── GET: Xác minh danh tính ──────────────────────────────────
    @GetMapping("/{id}/identity")
    public String identityDetail(@PathVariable Long id, @RequestParam(defaultValue = "0") int returnPage, Model model) {
        model.addAttribute("user", adminUserService.getUserDetail(id));
        model.addAttribute("returnPage", returnPage);
        return "pages/admin/identity-detail";
    }

    // ── POST: Khóa tài khoản ─────────────────────────────────────
    @PostMapping("/{id}/lock")
    public String lockAccount(@PathVariable Long id,
                              @ModelAttribute LockAccountRequest request,
                              RedirectAttributes ra) {
        try {
            adminUserService.lockAccount(id, request);
            ra.addFlashAttribute("successMessage", "Đã khóa tài khoản thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    // ── POST: Mở khóa ────────────────────────────────────────────
    @PostMapping("/{id}/unlock")
    public String unlockAccount(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminUserService.unlockAccount(id);
            ra.addFlashAttribute("successMessage", "Đã mở khóa tài khoản thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    // ── POST: Duyệt xác minh ─────────────────────────────────────
    @PostMapping("/{id}/identity/approve")
    public String approveIdentity(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminUserService.approveIdentityVerification(id);
            ra.addFlashAttribute("successMessage", "Đã duyệt xác minh danh tính thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    // ── POST: Từ chối xác minh ───────────────────────────────────
    @PostMapping("/{id}/identity/reject")
    public String rejectIdentity(@PathVariable Long id,
                                 @RequestParam String rejectReason,
                                 RedirectAttributes ra) {
        if (rejectReason == null || rejectReason.isBlank()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng nhập lý do từ chối");
            return "redirect:/admin/users/" + id + "/identity";
        }
        try {
            adminUserService.rejectIdentityVerification(id, rejectReason);
            ra.addFlashAttribute("successMessage", "Đã từ chối xác minh danh tính");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }
    
    // ── POST: Duyệt GPLX ─────────────────────────────────────────
    @PostMapping("/{id}/approve-license")
    public String approveLicense(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminUserService.approveLicenseVerification(id);
            ra.addFlashAttribute("successMessage", "Đã duyệt GPLX thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id + "/identity";
    }

    // ── POST: Từ chối GPLX ───────────────────────────────────────
    @PostMapping("/{id}/reject-license")
    public String rejectLicense(@PathVariable Long id,
                                @RequestParam String rejectReason,
                                RedirectAttributes ra) {
        if (rejectReason == null || rejectReason.isBlank()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng nhập lý do từ chối");
            return "redirect:/admin/users/" + id + "/identity";
        }
        try {
            adminUserService.rejectLicenseVerification(id, rejectReason);
            ra.addFlashAttribute("successMessage", "Đã từ chối GPLX");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id + "/identity";
    }
    
    // ── Helpers ───────────────────────────────────────────────────
    private <E extends Enum<E>> E parseEnum(Class<E> type, String value) {
        if (value == null || value.isBlank()) return null;
        try { return Enum.valueOf(type, value); }
        catch (IllegalArgumentException e) { return null; }
    }

    private String buildExtraParams(String keyword, String role, String status, String identityStatus) {
        StringBuilder sb = new StringBuilder();
        if (keyword != null && !keyword.isBlank()) sb.append("&keyword=").append(keyword);
        if (role    != null && !role.isBlank())    sb.append("&role=").append(role);
        if (status  != null && !status.isBlank())  sb.append("&status=").append(status);
        if (identityStatus != null && !identityStatus.isBlank()) sb.append("&identityStatus=").append(identityStatus);
        return sb.toString();
    }
}