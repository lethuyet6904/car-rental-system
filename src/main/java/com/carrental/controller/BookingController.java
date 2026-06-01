package com.carrental.controller;

import com.carrental.entity.RentalOrder;
import com.carrental.entity.User;
import com.carrental.enums.PickupMethod;
import com.carrental.enums.ComplaintType;
import com.carrental.enums.RentalImageType;
import com.carrental.enums.TransactionType;
import com.carrental.repository.RentalImageRepository;
import com.carrental.service.BookingService;
import com.carrental.service.ComplaintService;
import com.carrental.service.IdentityVerificationService;
import com.carrental.service.PaymentService;
import com.carrental.service.ReviewService;
import com.carrental.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final IdentityVerificationService identityVerificationService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final ComplaintService complaintService;
    private final RentalImageRepository rentalImageRepository;

    @PostMapping("/create")
    public String createBooking(
            @RequestParam Long carId,
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
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }

        User user;
        try {
            user = userService.findByPhone(auth.getName());
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin người dùng.");
                return "redirect:/cars/" + carId;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xác thực người dùng.");
            return "redirect:/cars/" + carId;
        }

        // Double-check xác minh danh tính phía server (tránh bypass JS)
        if (!identityVerificationService.isFullyVerified(user.getUserId())) {
            request.getSession().setAttribute("redirectAfterVerification", "/cars/" + carId);
            return "redirect:/verification/cccd";
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
                    note);

            // Đặt xe xong → chuyển ngay sang trang thanh toán cọc
            return "redirect:/payment/checkout/" + order.getOrderId();

        } catch (Exception e) {
            log.error("Đặt xe thất bại carId={}", carId, e);
            redirectAttributes.addFlashAttribute("error", "Đặt xe thất bại: " + e.getMessage());
            return "redirect:/cars/" + carId;
        }
    }

    @GetMapping("/order/{orderId}")
    public String viewOrder(@PathVariable Long orderId, Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            return "redirect:/auth/login";

        User user = userService.findByPhone(auth.getName());
        RentalOrder order = bookingService.getOrderById(orderId);

        if (order == null || !order.getCustomer().getUserId().equals(user.getUserId())) {
            return "redirect:/";
        }

        request.getSession().setAttribute("user", user);
        model.addAttribute("order", order);
        model.addAttribute("user", user);
        model.addAttribute("depositPayment",
                paymentService.getPaymentByOrderAndType(orderId, TransactionType.Deposit));
        model.addAttribute("finalPayment",
                paymentService.getPaymentByOrderAndType(orderId, TransactionType.FinalPayment));
        model.addAttribute("review", reviewService.findByOrderId(orderId).orElse(null));
        model.addAttribute("existingComplaint",
                complaintService.findByOrderAndSender(orderId, user.getUserId()).orElse(null));
        model.addAttribute("complaintTypes", new ComplaintType[]{
                ComplaintType.VehicleCondition,
                ComplaintType.OwnerBehavior,
                ComplaintType.LatePickup,
                ComplaintType.PricingIssue,
                ComplaintType.Other
        });
        model.addAttribute("pickupImages",
                rentalImageRepository.findByRentalOrderOrderIdAndImageType(orderId, RentalImageType.Pickup));
        model.addAttribute("returnImages",
                rentalImageRepository.findByRentalOrderOrderIdAndImageType(orderId, RentalImageType.Return));

        return "pages/booking/order-detail";
    }

    @GetMapping("/my-orders")
    public String myOrders(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            return "redirect:/auth/login";

        User user = userService.findByPhone(auth.getName());
        request.getSession().setAttribute("user", user);
        model.addAttribute("orders", bookingService.getOrdersByCustomer(user.getUserId()));
        model.addAttribute("user", user);

        return "pages/booking/my-orders";
    }

    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(
            @PathVariable Long orderId,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            return "redirect:/auth/login";

        User user = userService.findByPhone(auth.getName());
        if (user == null)
            return "redirect:/auth/login";

        try {
            // Lấy policy TRƯỚC khi hủy để tránh bị lệch status
            BookingService.RefundPolicy policy = bookingService.getRefundPolicy(orderId);

            bookingService.cancelOrder(orderId, user.getUserId(), reason);

            if (policy.getRefundPercent() > 0) {
                redirectAttributes.addFlashAttribute("success",
                        "Hủy đơn thành công. Bạn được hoàn " + policy.getRefundPercent()
                                + "% tiền cọc. (" + policy.getMessage() + ")");
            } else {
                redirectAttributes.addFlashAttribute("success",
                        "Hủy đơn thành công. " + policy.getMessage());
            }

        } catch (Exception e) {
            log.error("Hủy đơn thất bại orderId={}", orderId, e);
            redirectAttributes.addFlashAttribute("error", "Lỗi hủy đơn: " + e.getMessage());
        }

        return "redirect:/booking/order/" + orderId;
    }
}