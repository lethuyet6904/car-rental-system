package com.carrental.controller;

import com.carrental.dto.request.IdentityVerificationRequest;
import com.carrental.entity.IdentityVerification;
import com.carrental.entity.User;
import com.carrental.enums.VerificationStatus;
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

    // ====================== GET: Trang xác minh danh tính ======================
    @GetMapping("/identity")
    public String showIdentityVerification(
            @RequestParam(required = false) String redirect,
            Model model,
            HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }

        String phone = auth.getName();
        User user = userService.findByPhone(phone);
        if (user == null) {
            return "redirect:/auth/login";
        }
        request.getSession().setAttribute("user", user);

        // Lưu redirect URL vào session nếu có (dùng khi verify xong cần về trang cũ)
        if (redirect != null && !redirect.isEmpty()) {
            request.getSession().setAttribute("redirectAfterVerification", redirect);
        }

        // Lấy hồ sơ xác minh mới nhất của user
        IdentityVerification existing = identityVerificationService.findLatestByUser(user.getUserId());

        // Nếu đã xác minh thành công → redirect về trang trước hoặc home
        if (existing != null && existing.getStatus() == VerificationStatus.Approved) {
            String redirectUrl = (String) request.getSession().getAttribute("redirectAfterVerification");
            if (redirectUrl != null) {
                request.getSession().removeAttribute("redirectAfterVerification");
                return "redirect:" + redirectUrl;
            }
            return "redirect:/";
        }

        model.addAttribute("verificationRequest", new IdentityVerificationRequest());
        model.addAttribute("user", user);

        // Hồ sơ đang chờ duyệt
        if (existing != null && existing.getStatus() == VerificationStatus.Pending) {
            model.addAttribute("pending", true);
            model.addAttribute("submittedAt", existing.getSubmittedAt());
        }

        // Hồ sơ bị từ chối → cho phép nộp lại
        if (existing != null && existing.getStatus() == VerificationStatus.Rejected) {
            model.addAttribute("rejected", true);
            model.addAttribute("rejectReason", existing.getRejectReason());
            model.addAttribute("rejectedAt", existing.getReviewedAt());
        }

        // Flash message từ redirect trước đó (success/error)
        // (Thymeleaf tự lấy từ model nếu dùng RedirectAttributes)

        return "pages/verification/identity-verification";
    }

    // ====================== POST: Nộp hồ sơ xác minh ======================
    @PostMapping("/identity")
    public String submitIdentityVerification(
            @ModelAttribute IdentityVerificationRequest request,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }

        String phone = auth.getName();
        User user = userService.findByPhone(phone);
        if (user == null) {
            return "redirect:/auth/login";
        }

        // Validate thủ công các trường bắt buộc
        if (request.getNationalId() == null || request.getNationalId().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Số CCCD/CMND không được để trống");
            return "redirect:/verification/identity";
        }
        if (!request.getNationalId().matches("^[0-9]{9,12}$")) {
            redirectAttributes.addFlashAttribute("error", "Số CCCD/CMND không hợp lệ (9-12 chữ số)");
            return "redirect:/verification/identity";
        }
        if (request.getLicenseNumber() == null || request.getLicenseNumber().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Số GPLX không được để trống");
            return "redirect:/verification/identity";
        }
        if (!request.getLicenseNumber().matches("^[A-Za-z0-9]{8,15}$")) {
            redirectAttributes.addFlashAttribute("error", "Số GPLX không hợp lệ (8-15 ký tự)");
            return "redirect:/verification/identity";
        }

        // Validate ảnh upload
        if (request.getNationalIdFrontImage() == null || request.getNationalIdFrontImage().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng upload ảnh CCCD mặt trước");
            return "redirect:/verification/identity";
        }
        if (request.getNationalIdBackImage() == null || request.getNationalIdBackImage().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng upload ảnh CCCD mặt sau");
            return "redirect:/verification/identity";
        }
        if (request.getFrontImage() == null || request.getFrontImage().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng upload ảnh GPLX mặt trước");
            return "redirect:/verification/identity";
        }
        if (request.getBackImage() == null || request.getBackImage().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng upload ảnh GPLX mặt sau");
            return "redirect:/verification/identity";
        }

        try {
            identityVerificationService.submitVerification(user.getUserId(), request);
            redirectAttributes.addFlashAttribute("success",
                "Hồ sơ xác minh danh tính đã được gửi thành công! Vui lòng chờ admin duyệt.");
            // Xóa redirect URL sau khi submit (không redirect về trang cũ, chờ admin duyệt)
            httpRequest.getSession().removeAttribute("redirectAfterVerification");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        // PRG Pattern: redirect về GET để tránh resubmit form khi F5
        return "redirect:/verification/identity";
    }
}