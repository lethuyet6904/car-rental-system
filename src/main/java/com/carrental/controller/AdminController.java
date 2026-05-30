package com.carrental.controller;

import com.carrental.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('Admin')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping({"", "/"})
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "all") String period,
                            Model model) {
        model.addAttribute("stats",  adminDashboardService.getStats(period));
        model.addAttribute("period", period);
        return "pages/admin/dashboard";
    }
}