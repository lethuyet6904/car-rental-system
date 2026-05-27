package com.carrental.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('Admin')")
@RequiredArgsConstructor
public class AdminController {

    // Redirect /admin → /admin/dashboard
    @GetMapping({"", "/"})
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    // Dashboard tạm — mock data, sẽ gắn service sau
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // TODO: thay bằng adminDashboardService.getStats()
        model.addAttribute("totalUsers",    1284L);
        model.addAttribute("totalOwners",   156L);
        model.addAttribute("pendingCars",   8L);
        model.addAttribute("pendingOwners", 12L);
        model.addAttribute("totalOrders",   3456L);
        model.addAttribute("activeOrders",  142L);

        return "pages/admin/dashboard";
    }
}	