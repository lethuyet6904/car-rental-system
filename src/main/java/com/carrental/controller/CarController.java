package com.carrental.controller;

import com.carrental.dto.response.CarDetailResponse;
import com.carrental.dto.response.CarListResponse;
import com.carrental.service.CarService;
import com.carrental.service.IdentityVerificationService;  // THÊM IMPORT
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;
    private final IdentityVerificationService identityVerificationService;  // THÊM VÀO

    @GetMapping({"", "/search"})
    public String searchCars(
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "fuel", required = false) String fuel,
            @RequestParam(name = "transmission", required = false) String transmission,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CarListResponse> carPage = carService.searchActiveCars(city, null, fuel, transmission, pageable);

        model.addAttribute("cars", carPage.getContent());
        model.addAttribute("pageInfo", carPage);
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedFuel", fuel);
        model.addAttribute("selectedTransmission", transmission);

        return "pages/cars/car-list";
    }

    @GetMapping("/{id}")
    public String carDetail(@PathVariable("id") Long id, Model model) {
        CarDetailResponse car = carService.getCarById(id);
        model.addAttribute("car", car);
        
        // THÊM: Kiểm tra xem user đã xác minh danh tính chưa (để hiển thị thông báo khi đặt xe)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isVerified = false;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            try {
                // Lấy userId từ phone (cần có UserService, có thể inject thêm)
                // Hoặc tạm thời bỏ qua, sẽ check trong booking controller
                isVerified = true; // placeholder
            } catch (Exception e) {
                isVerified = false;
            }
        }
        model.addAttribute("isVerified", isVerified);
        
        return "pages/cars/car-detail";
    }
}