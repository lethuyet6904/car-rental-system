package com.carrental.controller;

import com.carrental.entity.User;
import com.carrental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final UserService userService;

    // ====================== TRANG THÔNG TIN CÁ NHÂN ======================
    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication auth) {
        String phone = auth.getName();
        User user = userService.findByPhone(phone);
        
        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("user", user);
        return "pages/customer/profile";
    }

    // ====================== CẬP NHẬT THÔNG TIN ======================
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User updatedUser, 
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        
        String phone = auth.getName();
        User currentUser = userService.findByPhone(phone);

        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        try {
            // Chỉ cho phép cập nhật một số trường an toàn
            currentUser.setFullName(updatedUser.getFullName());
            currentUser.setEmail(updatedUser.getEmail());
            currentUser.setAddress(updatedUser.getAddress());

            userService.updateUser(currentUser);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/customer/profile";
    }

    // ====================== ĐƠN HÀNG CỦA TÔI ======================
    @GetMapping("/my-orders")   // hoặc /booking/my-orders tùy bạn
    public String myOrders(Model model, Authentication auth) {
        String phone = auth.getName();
        User user = userService.findByPhone(phone);

        if (user == null) {
            return "redirect:/auth/login";
        }

        // Giả sử bạn đã có method này trong BookingService hoặc UserService
        model.addAttribute("orders", userService.getOrdersByCustomer(user.getUserId()));
        model.addAttribute("user", user);
        return "pages/customer/my-orders";
    }
}