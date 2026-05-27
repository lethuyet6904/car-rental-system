package com.carrental.service.impl;

import com.carrental.entity.Payment;
import com.carrental.entity.RentalOrder;
import com.carrental.enums.OrderStatus;
import com.carrental.enums.PaymentMethod;
import com.carrental.enums.PaymentStatus;
import com.carrental.enums.TransactionType;
import com.carrental.repository.PaymentRepository;
import com.carrental.repository.RentalOrderRepository;
import com.carrental.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalOrderRepository rentalOrderRepository;

    @Override
    public Payment createDepositPayment(RentalOrder order) {
        // Kiểm tra đã có payment cọc chưa - DÙNG ĐÚNG METHOD CỦA BẠN
        if (paymentRepository.findByRentalOrderOrderIdAndTransactionType(order.getOrderId(), TransactionType.Deposit).isPresent()) {
            throw new RuntimeException("Đơn hàng đã có payment cọc");
        }

        Payment payment = Payment.builder()
                .rentalOrder(order)
                .transactionType(TransactionType.Deposit)
                .amount(order.getDepositAmount())
                .paymentMethod(null) // Sẽ cập nhật sau khi khách chọn
                .status(PaymentStatus.Processing)
                .isPaid(false)
                .createdAt(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    @Override
    public boolean processDepositPayment(Long orderId, PaymentMethod method, String transactionId, RedirectAttributes ra) {
        RentalOrder order = rentalOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // SỬA: Cho phép cả PENDING_PAYMENT và Confirmed
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT && order.getStatus() != OrderStatus.Confirmed) {
            ra.addFlashAttribute("error", "Đơn hàng không ở trạng thái chờ thanh toán");
            return false;
        }

        // Tìm payment cọc - nếu không có thì tạo mới
        Payment payment = paymentRepository.findByRentalOrderOrderIdAndTransactionType(orderId, TransactionType.Deposit)
                .orElse(null);
        
        // Nếu chưa có payment (trường hợp Confirmed chưa thanh toán)
        if (payment == null) {
            payment = Payment.builder()
                    .rentalOrder(order)
                    .transactionType(TransactionType.Deposit)
                    .amount(order.getDepositAmount())
                    .paymentMethod(method)
                    .status(PaymentStatus.Processing)
                    .isPaid(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            payment = paymentRepository.save(payment);
        }

        // Giả lập thanh toán thành công
        boolean paymentSuccess = true; // Giả lập thành công

        if (paymentSuccess) {
            payment.setPaymentMethod(method);
            payment.setStatus(PaymentStatus.Success);
            payment.setIsPaid(true);
            paymentRepository.save(payment);

            // Nếu đang ở trạng thái PENDING_PAYMENT -> chuyển thành Pending
            if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                order.setStatus(OrderStatus.Pending);
            } 
            // Nếu đang ở trạng thái Confirmed -> chuyển thành Completed
            else if (order.getStatus() == OrderStatus.Confirmed) {
                order.setStatus(OrderStatus.Completed);
            }
            
            rentalOrderRepository.save(order);

            ra.addFlashAttribute("success", "Thanh toán thành công!");
            return true;
        } else {
            payment.setStatus(PaymentStatus.Failed);
            paymentRepository.save(payment);
            ra.addFlashAttribute("error", "Thanh toán thất bại. Vui lòng thử lại.");
            return false;
        }
    }
    @Override
    public boolean processFinalPayment(Long orderId, PaymentMethod method, BigDecimal extraFee, String damages, RedirectAttributes ra) {
        RentalOrder order = rentalOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() != OrderStatus.InProgress) {
            ra.addFlashAttribute("error", "Đơn hàng không ở trạng thái đang thuê");
            return false;
        }

        BigDecimal remainingAmount = calculateRemainingAmount(order, extraFee);

        // Giả lập thanh toán
        boolean paymentSuccess = simulatePayment(method);

        if (paymentSuccess) {
            // Tạo payment cho thanh toán lần cuối
            Payment finalPayment = Payment.builder()
                    .rentalOrder(order)
                    .transactionType(TransactionType.FinalPayment)
                    .amount(remainingAmount)
                    .paymentMethod(method)
                    .status(PaymentStatus.Success)
                    .isPaid(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(finalPayment);

            // Cập nhật đơn hàng thành Completed
            order.setStatus(OrderStatus.Completed);
            if (damages != null && !damages.isEmpty()) {
                order.setReturnChecklistNote("Hư hỏng/phụ thu: " + damages + " | Số tiền: " + extraFee + "đ");
            }
            rentalOrderRepository.save(order);

            ra.addFlashAttribute("success", "Thanh toán thành công! Cảm ơn bạn đã sử dụng dịch vụ.");
            return true;
        } else {
            ra.addFlashAttribute("error", "Thanh toán thất bại. Vui lòng thử lại.");
            return false;
        }
    }

    @Override
    public BigDecimal calculateRemainingAmount(RentalOrder order, BigDecimal extraFee) {
        BigDecimal remaining = order.getTotalAmount().subtract(order.getDepositAmount());
        if (extraFee != null && extraFee.compareTo(BigDecimal.ZERO) > 0) {
            remaining = remaining.add(extraFee);
        }
        return remaining;
    }

    @Override
    public Payment getPaymentByOrderAndType(Long orderId, TransactionType type) {
        // DÙNG ĐÚNG METHOD CỦA BẠN
        return paymentRepository.findByRentalOrderOrderIdAndTransactionType(orderId, type).orElse(null);
    }

    @Override
    public boolean isDepositPaid(Long orderId) {
        // DÙNG ĐÚNG METHOD CỦA BẠN
        return paymentRepository.findByRentalOrderOrderIdAndTransactionType(orderId, TransactionType.Deposit)
                .map(p -> p.getIsPaid() && p.getStatus() == PaymentStatus.Success)
                .orElse(false);
    }

    @Override
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment"));
        payment.setStatus(status);
        if (status == PaymentStatus.Success) {
            payment.setIsPaid(true);
        }
        return paymentRepository.save(payment);
    }

    @Override
    public String showCheckoutPage(Long orderId, Model model, RedirectAttributes ra) {
        RentalOrder order = rentalOrderRepository.findDetailedById(orderId).orElse(null);
        if (order == null) {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/";
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            ra.addFlashAttribute("error", "Đơn hàng không cần thanh toán hoặc đã được thanh toán");
            return "redirect:/booking/order/" + orderId;
        }

        // DÙNG ĐÚNG METHOD CỦA BẠN
        Payment depositPayment = paymentRepository.findByRentalOrderOrderIdAndTransactionType(orderId, TransactionType.Deposit)
                .orElse(null);
                
        if (depositPayment == null) {
            ra.addFlashAttribute("error", "Có lỗi xảy ra, vui lòng liên hệ hỗ trợ");
            return "redirect:/booking/order/" + orderId;
        }

        model.addAttribute("order", order);
        model.addAttribute("payment", depositPayment);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "pages/payment/checkout";
    }

    @Override
    public String showFinalPaymentPage(Long orderId, BigDecimal extraFee, String damages, Model model) {
        RentalOrder order = rentalOrderRepository.findDetailedById(orderId).orElse(null);
        if (order == null) {
            return "redirect:/owner/orders";
        }

        BigDecimal remainingAmount = calculateRemainingAmount(order, extraFee);

        model.addAttribute("order", order);
        model.addAttribute("remainingAmount", remainingAmount);
        model.addAttribute("extraFee", extraFee != null ? extraFee : BigDecimal.ZERO);
        model.addAttribute("damages", damages);
        model.addAttribute("paymentMethods", PaymentMethod.values());

        return "pages/payment/final-payment";
    }

    /**
     * Giả lập thanh toán - trong thực tế sẽ gọi API của Momo/VNPay
     */
    private boolean simulatePayment(PaymentMethod method) {
        // Giả lập luôn thành công
        return true;
    }
}