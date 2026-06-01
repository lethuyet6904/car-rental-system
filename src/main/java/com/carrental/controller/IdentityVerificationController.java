package com.carrental.controller;

import com.carrental.dto.request.IdentityVerificationRequest;
import com.carrental.entity.IdentityVerification;
import com.carrental.entity.User;
import com.carrental.enums.VerificationStatus;
import com.carrental.enums.LicenseStatus;
import com.carrental.service.IdentityVerificationService;
import com.carrental.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/verification")
@RequiredArgsConstructor
public class IdentityVerificationController {

    private final IdentityVerificationService identityVerificationService;
    private final UserService userService;

    // ── Helper ──────────────────────────────────────────────────
    private User getAuthUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal()))
            return null;
        return userService.findByPhone(auth.getName());
    }

    // ════════════════════════════════════════════════════════════
    // BƯỚC 1: Xác minh CCCD → /verification/cccd
    // ════════════════════════════════════════════════════════════
    @GetMapping("/cccd")
    public String showCccd(Model model, HttpServletRequest req) {
        User user = getAuthUser();
        if (user == null)
            return "redirect:/auth/login";
        req.getSession().setAttribute("user", user);

        IdentityVerification iv = identityVerificationService.findLatestByUser(user.getUserId());

        // Đã xác minh CCCD rồi → chuyển sang bước 2 (GPLX)
        if (iv != null && iv.getStatus() == VerificationStatus.Approved) {
            return "redirect:/verification/license";
        }

        model.addAttribute("user", user);
        model.addAttribute("verificationRequest", new IdentityVerificationRequest());

        if (iv != null && iv.getStatus() == VerificationStatus.Pending) {
            model.addAttribute("pending", true);
            model.addAttribute("submittedAt", iv.getSubmittedAt());
        }
        if (iv != null && iv.getStatus() == VerificationStatus.Rejected) {
            model.addAttribute("rejected", true);
            model.addAttribute("rejectReason", iv.getRejectReason());
        }

        return "pages/verification/cccd-verification";
    }

    @PostMapping("/cccd")
    public String submitCccd(@ModelAttribute IdentityVerificationRequest request,
            RedirectAttributes ra) {
        User user = getAuthUser();
        if (user == null)
            return "redirect:/auth/login";

        if (request.getNationalId() == null || request.getNationalId().isBlank()) {
            ra.addFlashAttribute("error", "Số CCCD/CMND không được để trống");
            return "redirect:/verification/cccd";
        }
        if (!request.getNationalId().matches("^[0-9]{9,12}$")) {
            ra.addFlashAttribute("error", "Số CCCD/CMND không hợp lệ (9–12 chữ số)");
            return "redirect:/verification/cccd";
        }
        if (request.getNationalIdFrontImage() == null || request.getNationalIdFrontImage().isEmpty()) {
            ra.addFlashAttribute("error", "Vui lòng upload ảnh CCCD mặt trước");
            return "redirect:/verification/cccd";
        }
        if (request.getNationalIdBackImage() == null || request.getNationalIdBackImage().isEmpty()) {
            ra.addFlashAttribute("error", "Vui lòng upload ảnh CCCD mặt sau");
            return "redirect:/verification/cccd";
        }

        try {
            identityVerificationService.submitCccd(user.getUserId(), request);
            ra.addFlashAttribute("success",
                    "Hồ sơ CCCD đã được gửi thành công! Admin sẽ duyệt trong 1–3 ngày làm việc.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/verification/cccd";
    }

    // ════════════════════════════════════════════════════════════
    // BƯỚC 2: Bổ sung GPLX → /verification/license
    // (chỉ truy cập được sau khi CCCD Approved)
    // ════════════════════════════════════════════════════════════
    @GetMapping("/license")
    public String showLicense(Model model, HttpServletRequest req) {
        User user = getAuthUser();
        if (user == null)
            return "redirect:/auth/login";
        req.getSession().setAttribute("user", user);

        IdentityVerification iv = identityVerificationService.findLatestByUser(user.getUserId());

        // Chưa xác minh CCCD → quay về bước 1
        if (iv == null || iv.getStatus() != VerificationStatus.Approved) {
            return "redirect:/verification/cccd";
        }

        if (iv.hasLicense()) {
            // licenseStatus == Approved
            model.addAttribute("alreadyHasLicense", true);
        } else if (iv.getLicenseStatus() == LicenseStatus.Pending) {
            // Đã nộp, chờ admin duyệt
            model.addAttribute("licensePending", true);
        } else if (iv.getLicenseStatus() == LicenseStatus.Rejected) {
            // Bị từ chối → cho nộp lại
            model.addAttribute("licenseRejected", true);
            model.addAttribute("licenseRejectReason", iv.getRejectReason());
        }

        model.addAttribute("user", user);
        model.addAttribute("identity", iv);
        model.addAttribute("verificationRequest", new IdentityVerificationRequest());
        return "pages/verification/license-verification";
    }

    @PostMapping("/license")
    public String submitLicense(@ModelAttribute IdentityVerificationRequest request,
            RedirectAttributes ra) {
        User user = getAuthUser();
        if (user == null)
            return "redirect:/auth/login";

        if (request.getLicenseNumber() == null || request.getLicenseNumber().isBlank()) {
            ra.addFlashAttribute("error", "Số GPLX không được để trống");
            return "redirect:/verification/license";
        }
        if (!request.getLicenseNumber().matches("^[A-Za-z0-9\\-]{6,20}$")) {
            ra.addFlashAttribute("error", "Số GPLX không hợp lệ (6–20 ký tự)");
            return "redirect:/verification/license";
        }
        if (request.getFrontImage() == null || request.getFrontImage().isEmpty()) {
            ra.addFlashAttribute("error", "Vui lòng upload ảnh GPLX mặt trước");
            return "redirect:/verification/license";
        }
        if (request.getBackImage() == null || request.getBackImage().isEmpty()) {
            ra.addFlashAttribute("error", "Vui lòng upload ảnh GPLX mặt sau");
            return "redirect:/verification/license";
        }

        try {
            identityVerificationService.submitLicense(user.getUserId(), request);
            ra.addFlashAttribute("success", "GPLX đã được gửi thành công! Admin sẽ xét duyệt trong 1–3 ngày làm việc.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/verification/license";
    }

    // ── Redirect cũ sang route mới (backward compat) ────────────
    @GetMapping("/identity")
    public String redirectIdentity() {
        return "redirect:/verification/cccd";
    }

    @GetMapping("/full")
    public String redirectFull() {
        return "redirect:/verification/license";
    }
}