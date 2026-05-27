package com.carrental.controller;

import com.carrental.dto.request.CarRequest;
import com.carrental.dto.request.OwnerRegistrationRequest;
import com.carrental.entity.*;
import com.carrental.enums.CarStatus;
import com.carrental.enums.OrderStatus;
import com.carrental.enums.UserRole;
import com.carrental.enums.VerificationStatus;
import com.carrental.repository.OwnerRegistrationRepository;
import com.carrental.service.CarService;
import com.carrental.service.IdentityVerificationService;
import com.carrental.service.OwnerService;
import com.carrental.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final UserService userService;
    private final OwnerService ownerService;
    private final CarService carService;
    private final IdentityVerificationService identityVerificationService;
    private final OwnerRegistrationRepository ownerRegistrationRepository;

    // ====================== HELPER: Lấy user hiện tại ======================
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userService.findByPhone(auth.getName());
    }

    // ====================== DASHBOARD ======================
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/auth/login";

        request.getSession().setAttribute("user", user);

        long totalCars        = ownerService.countCarsByOwner(user.getUserId());
        long activeCars       = ownerService.countCarsByOwnerAndStatus(user.getUserId(), CarStatus.Active);
        long pendingOrders    = ownerService.countOrdersByOwnerAndStatus(user.getUserId(), OrderStatus.Pending);
        long completedOrders  = ownerService.countOrdersByOwnerAndStatus(user.getUserId(), OrderStatus.Completed);
        long totalRevenue     = ownerService.getTotalRevenueByOwner(user.getUserId());
        List<RentalOrder> recentOrders = ownerService.getRecentOrdersByOwner(user.getUserId(), 5);

        model.addAttribute("totalCars",       totalCars);
        model.addAttribute("activeCars",      activeCars);
        model.addAttribute("pendingOrders",   pendingOrders);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("totalRevenue",    totalRevenue);
        model.addAttribute("recentOrders",    recentOrders != null ? recentOrders : new ArrayList<>());
        model.addAttribute("user",            user);

        return "pages/owner/dashboard";
    }

    // ====================== QUẢN LÝ XE ======================
    @GetMapping("/cars")
    public String manageCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Model model,
            HttpServletRequest request) {

        User user = getCurrentUser();
        if (user == null) return "redirect:/auth/login";
        request.getSession().setAttribute("user", user);

        Pageable pageable = PageRequest.of(page, size);
        Page<Car> carPage = ownerService.getCarsByOwner(user.getUserId(), status, pageable);

        model.addAttribute("cars",           carPage != null ? carPage.getContent() : new ArrayList<>());
        model.addAttribute("pageInfo",       carPage != null ? carPage : Page.empty(pageable));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("user",           user);
        return "pages/owner/cars";
    }

    @GetMapping("/cars/create")
    public String showCreateCarForm(Model model, HttpServletRequest request) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/auth/login";
        request.getSession().setAttribute("user", user);

        model.addAttribute("carRequest", new CarRequest());
        model.addAttribute("brands",    ownerService.getAllActiveBrands());
        model.addAttribute("carTypes",  ownerService.getAllActiveCarTypes());
        model.addAttribute("regions",   ownerService.getAllActiveRegions());
        model.addAttribute("carImages", new ArrayList<>());
        model.addAttribute("user",      user);
        return "pages/owner/car-form";
    }

    @PostMapping("/cars/create")
    public String createCar(@Valid @ModelAttribute CarRequest carRequest,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes,
                            HttpServletRequest request) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/auth/login";

        if (result.hasErrors()) {
            // PHẢI load lại dữ liệu dropdown, không redirect
            model.addAttribute("brands",    ownerService.getAllActiveBrands());
            model.addAttribute("carTypes",  ownerService.getAllActiveCarTypes());
            model.addAttribute("regions",   ownerService.getAllActiveRegions());
            model.addAttribute("carImages", new ArrayList<>());
            model.addAttribute("user",      user);
            model.addAttribute("error", "Vui lòng kiểm tra lại thông tin xe");
            request.getSession().setAttribute("user", user);
            return "pages/owner/car-form";   // trả về view, không redirect
        }
        try {
            ownerService.createCar(user.getUserId(), carRequest);
            redirectAttributes.addFlashAttribute("success", "Thêm xe thành công! Vui lòng chờ admin duyệt.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/cars";
    }

    @GetMapping("/cars/{carId}/edit")
    public String showEditCarForm(@PathVariable Long carId, Model model, HttpServletRequest request) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/auth/login";

        Car car = ownerService.getCarByIdAndOwner(carId, user.getUserId());
        if (car == null) return "redirect:/owner/cars";

        request.getSession().setAttribute("user", user);

        CarRequest carRequest = CarRequest.from(car);
        model.addAttribute("carRequest", carRequest);
        model.addAttribute("carId",      carId);
        model.addAttribute("brands",     ownerService.getAllActiveBrands());
        model.addAttribute("carTypes",   ownerService.getAllActiveCarTypes());
        model.addAttribute("regions",    ownerService.getAllActiveRegions());
        model.addAttribute("carImages",  new ArrayList<>());
        model.addAttribute("user",       user);
        return "pages/owner/car-form";
    }

    @PostMapping("/cars/{carId}/edit")
    public String updateCar(@PathVariable Long carId,
                            @Valid @ModelAttribute CarRequest carRequest,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/auth/login";

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin xe");
            return "redirect:/owner/cars/" + carId + "/edit";
        }
        try {
            carRequest.setCarId(carId);
            ownerService.updateCar(carId, user.getUserId(), carRequest);
            redirectAttributes.addFlashAttribute("success", "Cập nhật xe thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/cars";
    }

    @PostMapping("/cars/{carId}/toggle-status")
    public String toggleCarStatus(@PathVariable Long carId, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/auth/login";

        try {
            ownerService.toggleCarStatus(carId, user.getUserId());
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái xe thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/cars";
    }

    @PostMapping("/cars/{carId}/delete")
    public String deleteCar(@PathVariable Long carId, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/auth/login";

        try {
            ownerService.deleteCar(carId, user.getUserId());
            redirectAttributes.addFlashAttribute("success", "Xóa xe thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/cars";
    }

    // ====================== QUẢN LÝ ĐƠN HÀNG ======================
    @GetMapping("/orders")
    @Transactional(readOnly = true)
    public String manageOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Model model,
            HttpServletRequest request) {

        User user = getCurrentUser();
        if (user == null) return "redirect:/auth/login";
        request.getSession().setAttribute("user", user);

        Pageable pageable = PageRequest.of(page, size);
        OrderStatus orderStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                orderStatus = OrderStatus.valueOf(status);
            } catch (IllegalArgumentException ignored) {}
        }

        Page<RentalOrder> orderPage = ownerService.getOrdersByOwner(user.getUserId(), orderStatus, pageable);

        model.addAttribute("orders", orderPage != null ? orderPage.getContent() : new ArrayList<>());
        model.addAttribute("pageInfo", orderPage != null ? orderPage : Page.empty(pageable));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("user", user);
        return "pages/owner/orders";
    }

    @PostMapping("/orders/{orderId}/confirm")
    public String confirmOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/auth/login";

        try {
            ownerService.confirmOrder(orderId, user.getUserId());
            redirectAttributes.addFlashAttribute("success", "Đã xác nhận đơn hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/orders";
    }

    @PostMapping("/orders/{orderId}/reject")
    public String rejectOrder(@PathVariable Long orderId,
                              @RequestParam String reason,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/auth/login";

        if (reason == null || reason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập lý do từ chối!");
            return "redirect:/owner/orders";
        }

        try {
            ownerService.rejectOrder(orderId, user.getUserId(), reason.trim());
            redirectAttributes.addFlashAttribute("success", "Đã từ chối đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/orders";
    }

    @PostMapping("/orders/{orderId}/complete")
    public String completeOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/auth/login";

        try {
            ownerService.completeOrder(orderId, user.getUserId());
            redirectAttributes.addFlashAttribute("success", "Đã hoàn thành đơn hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/orders";
    }

    // ====================== ĐĂNG KÝ LÀM CHỦ XE ======================
    @GetMapping("/owner-registration")
    public String showOwnerRegistrationForm(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }

        String phone = auth.getName();
        User user = userService.findByPhone(phone);
        if (user == null) return "redirect:/auth/login";

        request.getSession().setAttribute("user", user);

        // Nếu đã là Owner rồi → redirect về dashboard
        if (user.getRole() == UserRole.Owner) {
            return "redirect:/owner/dashboard";
        }

        // Kiểm tra danh tính đã được xác minh chưa
        IdentityVerification verifiedIdentity =
                identityVerificationService.getVerifiedIdentityByUser(user.getUserId());
        boolean hasVerifiedIdentity = (verifiedIdentity != null);

        // Nếu chưa xác minh → thông báo và không cho submit
        if (!hasVerifiedIdentity) {
            model.addAttribute("requiresVerification", true);
            model.addAttribute("user", user);
            model.addAttribute("ownerRegistrationRequest", new OwnerRegistrationRequest());
            model.addAttribute("hasVerifiedIdentity", false);
            return "pages/owner/owner-registration";
        }

        // Kiểm tra đơn đang chờ duyệt
        OwnerRegistration pendingReg = ownerRegistrationRepository
                .findTopByUserUserIdOrderBySubmittedAtDesc(user.getUserId())
                .orElse(null);

        if (pendingReg != null) {
            model.addAttribute("existingRegistration", pendingReg);
            model.addAttribute("regStatus", pendingReg.getStatus());

            if (pendingReg.getStatus() == VerificationStatus.Pending) {
                model.addAttribute("hasPendingApplication", true);
            } else if (pendingReg.getStatus() == VerificationStatus.Rejected) {
                model.addAttribute("wasRejected", true);
                model.addAttribute("rejectReason", pendingReg.getRejectReason());
            }
        }

        OwnerRegistrationRequest ownerRequest = new OwnerRegistrationRequest();
        // Pre-fill nationalId từ CCCD đã được xác minh
        ownerRequest.setNationalId(verifiedIdentity.getNationalId());

        model.addAttribute("ownerRegistrationRequest", ownerRequest);
        model.addAttribute("user",                user);
        model.addAttribute("hasVerifiedIdentity", true);
        model.addAttribute("verifiedIdentity",    verifiedIdentity);
        return "pages/owner/owner-registration";
    }

    @PostMapping("/owner-registration")
    public String submitOwnerRegistration(
            @ModelAttribute OwnerRegistrationRequest request,
            RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }

        String phone = auth.getName();
        User user = userService.findByPhone(phone);
        if (user == null) return "redirect:/auth/login";

        // Kiểm tra đã là Owner chưa
        if (user.getRole() == UserRole.Owner) {
            redirectAttributes.addFlashAttribute("error", "Bạn đã là chủ xe rồi!");
            return "redirect:/owner/dashboard";
        }

        // BẮT BUỘC phải xác minh danh tính trước mới được đăng ký chủ xe
        if (!identityVerificationService.isIdentityVerified(user.getUserId())) {
            redirectAttributes.addFlashAttribute("error",
                "Bạn cần xác minh danh tính (CCCD/GPLX) trước khi đăng ký làm chủ xe.");
            return "redirect:/verification/identity?redirect=/owner/owner-registration";
        }

        try {
            userService.applyForOwner(user.getUserId(), request);
            redirectAttributes.addFlashAttribute("success",
                "Đơn đăng ký trở thành chủ xe đã được gửi thành công! Vui lòng chờ admin duyệt.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/owner/owner-registration";
    }
}