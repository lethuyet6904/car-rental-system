package com.carrental.controller;

import com.carrental.dto.OwnerRegistrationRequest;
import com.carrental.entity.User;
import com.carrental.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class OwnerController {

    private final UserService userService;

    public OwnerController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/owner-registration")

    public String showOwnerRegistrationForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        
        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("ownerRegistrationRequest", new OwnerRegistrationRequest());
        model.addAttribute("user", user);
        return "pages/owner/owner-registration";
    }

    @PostMapping("/owner-registration")

    public String submitOwnerRegistration(@ModelAttribute OwnerRegistrationRequest request,
                                          HttpSession session, Model model) {
        try {
            User user = (User) session.getAttribute("currentUser");

            userService.applyForOwner(user.getUserId(), request);   // ← Sửa thành applyForOwner

            model.addAttribute("success", "Đơn đăng ký trở thành chủ xe đã được gửi thành công!");

           

            return "pages/owner/owner-registration";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "pages/owner/owner-registration";
        }
    }
}