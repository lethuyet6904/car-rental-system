package com.carrental.controller;

import com.carrental.entity.Car;
import com.carrental.entity.RentalOrder;
import com.carrental.entity.User;
import com.carrental.enums.PickupMethod;
import com.carrental.service.BookingService;
import com.carrental.service.CarService;
import com.carrental.service.IdentityVerificationService;
import com.carrental.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final CarService carService;
    private final BookingService bookingService;
    private final UserService userService;
    private final IdentityVerificationService identityVerificationService;

    @GetMapping("/{carId}")
    @Transactional(readOnly = true)
    public String showBookingForm(@PathVariable Long carId,
                                  @RequestParam(required = false) String pickupDate,
                                  @RequestParam(required = false) String returnDate,
                                  Model model,
                                  HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }

        String phone = auth.getName();
        User user;

        try {
            user = userService.findByPhone(phone);
            if (user == null) {
                return "redirect:/auth/login";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Không tìm thấy thông tin người dùng.");
            return "redirect:/";
        }

        request.getSession().setAttribute("user", user);

        // Kiểm tra xác minh danh tính
        if (!identityVerificationService.isIdentityVerified(user.getUserId())) {
            request.getSession().setAttribute("redirectAfterVerification", "/booking/" + carId);
            return "redirect:/verification/identity";
        }

        // Lấy xe với đầy đủ thông tin
        Car car = carService.getCarEntityById(carId);
        if (car == null) {
            model.addAttribute("error", "Không tìm thấy xe.");
            return "redirect:/cars";
        }

        if (!"Active".equals(car.getStatus().name())) {
            model.addAttribute("error", "Xe hiện không khả dụng để thuê.");
            return "redirect:/cars/" + carId;
        }

        model.addAttribute("car", car);
        model.addAttribute("user", user);

        // Ngày mặc định
        if (pickupDate == null) {
            pickupDate = LocalDate.now().plusDays(1).toString();
        }
        if (returnDate == null) {
            returnDate = LocalDate.now().plusDays(2).toString();
        }

        model.addAttribute("pickupDate", pickupDate);
        model.addAttribute("returnDate", returnDate);
        model.addAttribute("pickupTime", "09:00");
        model.addAttribute("returnTime", "09:00");

        return "pages/booking/booking-form";
    }

    @PostMapping("/create")
    public String createBooking(@RequestParam Long carId,
                                @RequestParam String pickupDate,
                                @RequestParam String pickupTime,
                                @RequestParam String returnDate,
                                @RequestParam String returnTime,
                                @RequestParam String pickupMethod,
                                @RequestParam(required = false) String deliveryAddress,
                                @RequestParam(required = false) String note,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        String phone = auth.getName();
        User user = null;
        try {
            user = userService.findByPhone(phone);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin người dùng.");
                return "redirect:/booking/" + carId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi xác thực người dùng.");
            return "redirect:/booking/" + carId;
        }

        try {
            RentalOrder order = bookingService.createOrder(
                user.getUserId(),
                carId,
                LocalDate.parse(pickupDate),
                LocalTime.parse(pickupTime),
                LocalDate.parse(returnDate),
                LocalTime.parse(returnTime),
                PickupMethod.valueOf(pickupMethod),
                deliveryAddress,
                note
            );

            redirectAttributes.addFlashAttribute("success", "Đặt xe thành công! Vui lòng chờ chủ xe xác nhận.");
            return "redirect:/booking/order/" + order.getOrderId();

        } catch (Exception e) {
            e.printStackTrace();   // ← Quan trọng để xem lỗi trong console
            redirectAttributes.addFlashAttribute("error", "Đặt xe thất bại: " + e.getMessage());
            return "redirect:/booking/" + carId;
        }
    }

    @GetMapping("/order/{orderId}")
    public String viewOrder(@PathVariable Long orderId, Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String phone = auth.getName();
        User user = userService.findByPhone(phone);

        RentalOrder order = bookingService.getOrderById(orderId);
        if (order == null || !order.getCustomer().getUserId().equals(user.getUserId())) {
            return "redirect:/";
        }

        request.getSession().setAttribute("user", user);
        model.addAttribute("order", order);
        model.addAttribute("user", user);

        return "pages/booking/order-detail";
    }

    @GetMapping("/my-orders")
    public String myOrders(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String phone = auth.getName();
        User user = userService.findByPhone(phone);

        request.getSession().setAttribute("user", user);
        model.addAttribute("orders", bookingService.getOrdersByCustomer(user.getUserId()));
        model.addAttribute("user", user);

        return "pages/booking/my-orders";
    }
}