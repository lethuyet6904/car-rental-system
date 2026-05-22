package com.carrental.controller;

import com.carrental.dto.request.OwnerRegistrationRequest;
import com.carrental.entity.User;
import com.carrental.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final UserService userService;

    @GetMapping("/owner-registration")
    public String showOwnerRegistrationForm(Model model, HttpServletRequest request) {

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

        model.addAttribute("ownerRegistrationRequest", new OwnerRegistrationRequest());
        model.addAttribute("user", user);
        return "pages/owner/owner-registration";
    }

    @PostMapping("/owner-registration")
    public String submitOwnerRegistration(@ModelAttribute OwnerRegistrationRequest request,
                                          HttpServletRequest session, Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String phone = auth.getName();
            User user = userService.findByPhone(phone);
            
            userService.applyForOwner(user.getUserId(), request);
            model.addAttribute("success", "Đơn đăng ký trở thành chủ xe đã được gửi thành công!");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "pages/owner/owner-registration";

    }
}