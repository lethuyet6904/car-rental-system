package com.carrental.controller;

import com.carrental.entity.IdentityVerification;
import com.carrental.entity.User;
import com.carrental.service.IdentityVerificationService;
import com.carrental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final UserService userService;
    private final IdentityVerificationService identityVerificationService;
    private final PasswordEncoder passwordEncoder;

    // ====================== HELPER: Lấy user hiện tại ======================
    private User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return null;
        return userService.findByPhone(auth.getName());
    }

    // ====================== TRANG HỒ SƠ CÁ NHÂN ======================
    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        if (user == null)
            return "redirect:/auth/login";

        // Lấy thông tin xác minh danh tính mới nhất
        IdentityVerification identity = identityVerificationService.findLatestByUser(user.getUserId());

        model.addAttribute("user", user);
        model.addAttribute("identity", identity);
        model.addAttribute("identityStatus", identity != null ? identity.getStatus() : null);

        return "pages/customer/profile";
    }

    // ====================== CẬP NHẬT THÔNG TIN CÁ NHÂN ======================
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(auth);
        if (user == null)
            return "redirect:/auth/login";

        try {
            // Validate email format nếu có nhập
            if (email != null && !email.isBlank()) {
                if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    redirectAttributes.addFlashAttribute("error", "Email không đúng định dạng");
                    return "redirect:/customer/profile";
                }
                user.setEmail(email.trim());
            }
            if (fullName != null && !fullName.isBlank()) {
                user.setFullName(fullName.trim());
            }
            if (address != null) {
                user.setAddress(address.trim());
            }

            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/customer/profile";
    }

    // ====================== TRANG ĐỔI MẬT KHẨU ======================
    @GetMapping("/change-password")
    public String showChangePassword(Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        if (user == null)
            return "redirect:/auth/login";

        IdentityVerification identity = identityVerificationService.findLatestByUser(user.getUserId());
        model.addAttribute("user", user);
        model.addAttribute("identityStatus", identity != null ? identity.getStatus() : null);
        return "pages/customer/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(auth);
        if (user == null)
            return "redirect:/auth/login";

        try {
            // Kiểm tra mật khẩu hiện tại
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng");
                return "redirect:/customer/change-password";
            }
            // Kiểm tra định dạng mật khẩu mới
            if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$")) {
                redirectAttributes.addFlashAttribute("error",
                        "Mật khẩu phải có ít nhất 6 ký tự, gồm chữ hoa, chữ thường và số");
                return "redirect:/customer/change-password";
            }
            // Kiểm tra xác nhận
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không trùng khớp");
                return "redirect:/customer/change-password";
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/customer/change-password";
    }

    // ====================== ĐƠN HÀNG CỦA TÔI ======================
    @GetMapping("/my-orders")
    public String myOrders(Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        if (user == null)
            return "redirect:/auth/login";

        model.addAttribute("orders", userService.getOrdersByCustomer(user.getUserId()));
        model.addAttribute("user", user);
        return "pages/customer/my-orders";
    }
}