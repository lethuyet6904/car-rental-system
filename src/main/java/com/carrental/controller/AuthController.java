package com.carrental.controller;

import com.carrental.dto.LoginRequest;
import com.carrental.dto.RegisterRequest;
import com.carrental.dto.ForgotPasswordRequest;
import com.carrental.entity.User;
import com.carrental.security.JwtTokenProvider;
import com.carrental.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

//    public AuthController(UserService userService) {
//        this.userService = userService;
//		this.jwtTokenProvider = new JwtTokenProvider();
//    }

    // ====================== ĐĂNG NHẬP ======================
    @GetMapping("/login")
    public String showLogin(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "pages/auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest request,
                        BindingResult result, Model model, HttpServletResponse response) {

        if (result.hasErrors()) {
            return "pages/auth/login";
        }

        try {
            User user = userService.login(request.getPhone(), request.getPassword());
            
            // Tạo JWT token
            String token = jwtTokenProvider.generateToken(
                    user.getPhone(),
                    user.getRole().name()
            );
            
            // Lưu token vào cookie
            Cookie cookie = new Cookie("JWT_TOKEN", token);
            cookie.setHttpOnly(true);   // JS không đọc được — tránh XSS
            cookie.setPath("/");        // Gửi cookie cho mọi request
            cookie.setMaxAge(24 * 60 * 60); // 24 giờ
            
            response.addCookie(cookie);

            // SỬA Ở ĐÂY: Dùng getRole() thay vì getVaiTro()
            return switch (user.getRole()) {
            case Admin    -> "redirect:/admin/dashboard";
            case Owner    -> "redirect:/owner/dashboard";
                default -> "redirect:/";        // Trang chủ
            };

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "pages/auth/login";
        }
    }

    // ====================== ĐĂNG KÝ ======================
    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "pages/auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest request,
                           BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "pages/auth/register";
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "pages/auth/register";
        }

        try {
            userService.register(request);
            model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "pages/auth/register";
        }
    }

    // ====================== QUÊN MẬT KHẨU ======================
    @GetMapping("/forgot-password")
    public String showForgotPassword(Model model) {
        model.addAttribute("forgotRequest", new ForgotPasswordRequest());
        return "pages/auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute("forgotRequest") ForgotPasswordRequest request,
                                 BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "pages/auth/forgot-password";
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("error", "Xác nhận mật khẩu không khớp");
            return "pages/auth/forgot-password";
        }

        try {
            userService.resetPassword(request.getEmail(), request.getNewPassword());
            model.addAttribute("success", "Đổi mật khẩu thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "pages/auth/forgot-password";
        }
    }

    // ====================== ĐĂNG XUẤT ======================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login?logout=true";
    }
}