package com.carrental.controller;

import com.carrental.dto.response.CarListResponse;
import com.carrental.entity.User;
import com.carrental.service.CarService;
import com.carrental.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    // FIX: bỏ inject UserRepository trực tiếp — vi phạm layered architecture
    // Controller chỉ được gọi Service, không gọi Repository trực tiếp
    private final UserService userService;
    private final CarService carService;

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {

        // Nếu user đã đăng nhập mà session bị xóa (vd: server restart)
        // → tự động khôi phục user vào session từ JWT token
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            if (request.getSession().getAttribute("user") == null) {
                // FIX: gọi qua UserService thay vì UserRepository
                try {
                    User user = userService.findByPhone(auth.getName());
                    request.getSession().setAttribute("user", user);
                } catch (Exception ignored) {
                    // Token hợp lệ nhưng user bị xóa khỏi DB → bỏ qua, navbar sẽ ẩn info
                }
            }
        }

        // Hiển thị 8 xe nổi bật trên trang chủ (trang đầu tiên, sắp xếp mặc định)
        Page<CarListResponse> carPage = carService.searchActiveCars(null, null, null, null, PageRequest.of(0, 4));
        model.addAttribute("cars", carPage.getContent());

        return "home";
    }
}
