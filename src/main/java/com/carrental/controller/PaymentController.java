package com.carrental.controller;

import com.carrental.entity.RentalOrder;
import com.carrental.entity.User;
import com.carrental.enums.OrderStatus;
import com.carrental.enums.PaymentMethod;
import com.carrental.service.BookingService;
import com.carrental.service.PaymentService;
import com.carrental.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final UserService userService;

    /**
     * GET: Hiển thị trang thanh toán cọc
     */
    @GetMapping("/checkout/{orderId}")
    public String showCheckout(@PathVariable Long orderId, Model model, RedirectAttributes ra, HttpServletRequest request) {
        // Kiểm tra đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }

        User user = userService.findByPhone(auth.getName());
        if (user == null) {
            return "redirect:/auth/login";
        }
        request.getSession().setAttribute("user", user);

        RentalOrder order = bookingService.getOrderById(orderId);
        if (order == null) {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/booking/my-orders";
        }

        // SỬA: Cho phép cả PENDING_PAYMENT và Confirmed đều vào được checkout
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT && order.getStatus() != OrderStatus.Confirmed) {
            ra.addFlashAttribute("error", "Đơn hàng không cần thanh toán hoặc đã được thanh toán");
            return "redirect:/booking/order/" + orderId;
        }

        // Tính số tiền cần thanh toán
        BigDecimal amountToPay;
        if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            amountToPay = order.getDepositAmount(); // Cọc 30%
        } else {
            amountToPay = order.getTotalAmount().subtract(order.getDepositAmount()); // Còn lại 70%
        }

        model.addAttribute("order", order);
        model.addAttribute("amountToPay", amountToPay);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "pages/payment/checkout";
    }

    /**
     * POST: Xử lý thanh toán cọc
     */
    @PostMapping("/process-deposit")
    public String processDeposit(@RequestParam Long orderId,
                                  @RequestParam PaymentMethod paymentMethod,
                                  @RequestParam(required = false) String transactionId,
                                  RedirectAttributes ra) {

        boolean success = paymentService.processDepositPayment(orderId, paymentMethod, transactionId, ra);

        if (success) {
            return "redirect:/booking/order/" + orderId;
        } else {
            return "redirect:/payment/checkout/" + orderId;
        }
    }

    /**
     * GET: Hiển thị trang thanh toán nốt (khi trả xe)
     */
    @GetMapping("/final/{orderId}")
    public String showFinalPayment(@PathVariable Long orderId,
                                    @RequestParam(required = false) BigDecimal extraFee,
                                    @RequestParam(required = false) String damages,
                                    Model model,
                                    RedirectAttributes ra) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        RentalOrder order = bookingService.getOrderById(orderId);
        if (order == null) {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/owner/orders";
        }

        // Kiểm tra quyền (chỉ customer mới được thanh toán)
        String phone = auth.getName();
        User user = userService.findByPhone(phone);
        if (user == null || !order.getCustomer().getUserId().equals(user.getUserId())) {
            ra.addFlashAttribute("error", "Bạn không có quyền thanh toán đơn hàng này");
            return "redirect:/";
        }

        return paymentService.showFinalPaymentPage(orderId, extraFee, damages, model);
    }

    /**
     * POST: Xử lý thanh toán nốt
     */
    @PostMapping("/process-final")
    public String processFinalPayment(@RequestParam Long orderId,
                                       @RequestParam PaymentMethod paymentMethod,
                                       @RequestParam(required = false) BigDecimal extraFee,
                                       @RequestParam(required = false) String damages,
                                       @RequestParam(required = false) String transactionId,
                                       RedirectAttributes ra) {

        boolean success = paymentService.processFinalPayment(orderId, paymentMethod, extraFee, damages, ra);

        if (success) {
            return "redirect:/booking/order/" + orderId;
        } else {
            return "redirect:/payment/final/" + orderId + "?extraFee=" + (extraFee != null ? extraFee : 0);
        }
    }

    /**
     * GET: Trang thanh toán thành công (callback)
     */
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam(required = false) Long orderId,
                                  @RequestParam(required = false) String paymentMethod,
                                  Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("paymentMethod", paymentMethod);
        return "pages/payment/success";
    }

    /**
     * GET: Hủy thanh toán
     */
    @GetMapping("/cancel")
    public String paymentCancel(@RequestParam Long orderId, RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Bạn đã hủy thanh toán");
        return "redirect:/payment/checkout/" + orderId;
    }
}