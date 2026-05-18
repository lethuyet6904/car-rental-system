package com.carrental.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        // Truyền currentUser vào để navbar hoạt động
        if (session.getAttribute("currentUser") != null) {
            model.addAttribute("currentUser", session.getAttribute("currentUser"));
        }
        return "home";   // tên file là home.html
    }
}