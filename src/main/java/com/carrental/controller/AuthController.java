package com.carrental.controller;

import com.carrental.dto.request.ForgotPasswordRequest;
import com.carrental.dto.request.LoginRequest;
import com.carrental.dto.request.RegisterRequest;
import com.carrental.entity.User;
import com.carrental.security.JwtTokenProvider;
import com.carrental.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
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
    public String showLogin(Model model, HttpServletResponse response) {
        // Nếu đã đăng nhập rồi → redirect về trang chủ
        if (isAuthenticated()) {
            return "redirect:/";
        }
        // Không cho trình duyệt cache trang login
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        model.addAttribute("loginRequest", new LoginRequest());
        return "pages/auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest request,
                        BindingResult result, Model model,
                        HttpServletRequest httpRequest, HttpServletResponse response) {

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
            
            // Lưu user vào session để navbar fragment hiển thị đúng
            httpRequest.getSession().setAttribute("user", user);

            // SỬA Ở ĐÂY: Dùng getRole() thay vì getVaiTro()
            return switch (user.getRole()) {
            case Admin -> "redirect:/admin/dashboard";
            case Owner -> "redirect:/owner/dashboard";
            	default -> "redirect:/";
            };

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "pages/auth/login";
        }
    }

    // ====================== ĐĂNG KÝ ======================
    @GetMapping("/register")
    public String showRegister(Model model, HttpServletResponse response) {
        if (isAuthenticated()) {
            return "redirect:/";
        }
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        model.addAttribute("registerRequest", new RegisterRequest());
        return "pages/auth/register";
    }

    // Kiểm tra user đã đăng nhập chưa
    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest request,
                           BindingResult result, Model model) {

        if (result.hasErrors()) {
            // Gom tất cả lỗi validation thành chuỗi để hiển thị
            StringBuilder sb = new StringBuilder();
            result.getAllErrors().forEach(err -> {
                sb.append(err.getDefaultMessage()).append(". ");
            });
            model.addAttribute("error", sb.toString().trim());
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
    public String logout(HttpServletRequest httpRequest, HttpServletResponse response) {
    	// Xóa cookie JWT_TOKEN bằng cách set maxAge = 0
        Cookie cookie = new Cookie("JWT_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // ← maxAge = 0 → browser xóa cookie ngay
        response.addCookie(cookie);
        
        // Xóa session
        httpRequest.getSession().invalidate();
        
        return "redirect:/auth/login?logout=true";
    }
}