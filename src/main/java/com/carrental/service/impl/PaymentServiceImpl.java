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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalOrderRepository rentalOrderRepository;

    @Override
    public boolean processDepositPayment(Long orderId, PaymentMethod method,
            String transactionId, RedirectAttributes ra) {
        RentalOrder order = rentalOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Chỉ cho thanh toán khi đang ở trạng thái chờ cọc
        if (order.getStatus() != OrderStatus.Pending) {
            ra.addFlashAttribute("error", "Đơn hàng không ở trạng thái chờ thanh toán cọc");
            return false;
        }

        // Tạo payment record nếu chưa có — paymentMethod KHÔNG bao giờ null (DB NOT NULL)
        Payment payment = paymentRepository
                .findByRentalOrderOrderIdAndTransactionType(orderId, TransactionType.Deposit)
                .orElseGet(() -> Payment.builder()
                        .rentalOrder(order)
                        .transactionType(TransactionType.Deposit)
                        .amount(order.getDepositAmount())
                        .paymentMethod(method)
                        .status(PaymentStatus.Processing)
                        .isPaid(false)
                        .createdAt(LocalDateTime.now())
                        .transactionCode("DEPOSIT_" + UUID.randomUUID().toString())
                        .build());

        // Mock: luôn thành công
        payment.setPaymentMethod(method);
        payment.setStatus(PaymentStatus.Success);
        payment.setIsPaid(true);
        paymentRepository.save(payment);

        // Cọc xong → chờ Owner duyệt
        order.setStatus(OrderStatus.PendingApproval);
        rentalOrderRepository.save(order);

        ra.addFlashAttribute("success", "Đặt cọc thành công! Vui lòng chờ chủ xe xác nhận.");
        return true;
    }

    @Override
    public boolean processFinalPayment(Long orderId, PaymentMethod method,
            BigDecimal extraFee, String damages, RedirectAttributes ra) {
        RentalOrder order = rentalOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() != OrderStatus.InProgress) {
            ra.addFlashAttribute("error", "Đơn hàng không ở trạng thái đang thuê");
            return false;
        }

        BigDecimal remainingAmount = calculateRemainingAmount(order, extraFee);

        Payment finalPayment = Payment.builder()
                .rentalOrder(order)
                .transactionType(TransactionType.FinalPayment)
                .amount(remainingAmount)
                .paymentMethod(method)
                .status(PaymentStatus.Success)
                .isPaid(true)
                .createdAt(LocalDateTime.now())
                .transactionCode("FINAL_" + UUID.randomUUID().toString())
                .build();
        paymentRepository.save(finalPayment);

        order.setStatus(OrderStatus.Completed);
        order.setActualReturnTime(LocalDateTime.now());
        if (damages != null && !damages.isEmpty()) {
            order.setReturnChecklistNote("Hư hỏng/phụ thu: " + damages + " | Số tiền: " + extraFee + "đ");
        }
        rentalOrderRepository.save(order);

        ra.addFlashAttribute("success", "Thanh toán thành công! Cảm ơn bạn đã sử dụng dịch vụ.");
        return true;
    }

    /**
     * Hoàn tiền khi hủy đơn (mock).
     * - refundPercent = 0   → không tạo Refund record (mất cọc, ghi nhận lý do)
     * - refundPercent > 0   → tạo Refund record, amount = depositAmount * refundPercent%
     * paymentMethod lấy từ Deposit gốc để đảm bảo DB NOT NULL.
     */
    @Override
    public boolean processRefund(Long orderId, int refundPercent, RedirectAttributes ra) {
        if (refundPercent == 0) {
            // Mất cọc — không tạo Refund record
            return true;
        }

        // Lấy Payment cọc gốc để biết paymentMethod (DB NOT NULL)
        Payment deposit = paymentRepository
                .findByRentalOrderOrderIdAndTransactionType(orderId, TransactionType.Deposit)
                .orElse(null);

        if (deposit == null) {
            // Chưa có deposit record → không cần hoàn tiền
            return true;
        }

        RentalOrder order = rentalOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        BigDecimal refundAmount = deposit.getAmount()
                .multiply(BigDecimal.valueOf(refundPercent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        Payment refund = Payment.builder()
                .rentalOrder(order)
                .transactionType(TransactionType.Refund)
                .amount(refundAmount)
                .paymentMethod(deposit.getPaymentMethod()) // lấy từ Deposit gốc — không null
                .status(PaymentStatus.Success)
                .isPaid(true)
                .createdAt(LocalDateTime.now())
                .transactionCode("REFUND_" + UUID.randomUUID().toString())
                .build();
        paymentRepository.save(refund);

        return true;
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
        return paymentRepository.findByRentalOrderOrderIdAndTransactionType(orderId, type).orElse(null);
    }

    @Override
    public boolean isDepositPaid(Long orderId) {
        return paymentRepository.findByRentalOrderOrderIdAndTransactionType(orderId, TransactionType.Deposit)
                .map(p -> p.getIsPaid() && p.getStatus() == PaymentStatus.Success)
                .orElse(false);
    }

    @Override
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment"));
        payment.setStatus(status);
        if (status == PaymentStatus.Success)
            payment.setIsPaid(true);
        return paymentRepository.save(payment);
    }

    @Override
    public String showCheckoutPage(Long orderId, Model model, RedirectAttributes ra) {
        RentalOrder order = rentalOrderRepository.findDetailedById(orderId).orElse(null);
        if (order == null) {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/";
        }
        if (order.getStatus() != OrderStatus.Pending) {
            ra.addFlashAttribute("error", "Đơn hàng không cần thanh toán hoặc đã được thanh toán");
            return "redirect:/booking/order/" + orderId;
        }
        model.addAttribute("order", order);
        model.addAttribute("amountToPay", order.getDepositAmount());
        model.addAttribute("paymentMethods", com.carrental.enums.PaymentMethod.values());
        return "pages/payment/checkout";
    }

    @Override
    public String showFinalPaymentPage(Long orderId, BigDecimal extraFee, String damages, Model model) {
        RentalOrder order = rentalOrderRepository.findDetailedById(orderId).orElse(null);
        if (order == null)
            return "redirect:/owner/orders";

        BigDecimal remainingAmount = calculateRemainingAmount(order, extraFee);
        model.addAttribute("order", order);
        model.addAttribute("remainingAmount", remainingAmount);
        model.addAttribute("extraFee", extraFee != null ? extraFee : BigDecimal.ZERO);
        model.addAttribute("damages", damages);
        model.addAttribute("paymentMethods", com.carrental.enums.PaymentMethod.values());
        return "pages/payment/final-payment";
    }
}