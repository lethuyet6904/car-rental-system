package com.carrental.controller;

import com.carrental.repository.UserRepository;
import com.carrental.service.CarService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserRepository userRepository;
    private final CarService carService;

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        // Lấy user từ SecurityContext (JWT) thay vì session
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            // Nếu session chưa có user (VD: server restart), set lại từ JWT
            if (request.getSession().getAttribute("user") == null) {
                String phone = auth.getName();
                userRepository.findByPhone(phone).ifPresent(user ->
                    request.getSession().setAttribute("user", user)
                );
            }
        }

        model.addAttribute("cars", carService.getActiveCars());

        return "home";
    }
}