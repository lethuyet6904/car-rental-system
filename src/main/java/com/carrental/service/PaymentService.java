package com.carrental.service;

import com.carrental.entity.Payment;
import com.carrental.entity.RentalOrder;
import com.carrental.enums.PaymentMethod;
import com.carrental.enums.PaymentStatus;
import com.carrental.enums.TransactionType;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

public interface PaymentService {

    /**
     * Tạo payment record cho đặt cọc
     */
    Payment createDepositPayment(RentalOrder order);

    /**
     * Xử lý thanh toán cọc
     */
    boolean processDepositPayment(Long orderId, PaymentMethod method, String transactionId, RedirectAttributes ra);

    /**
     * Xử lý thanh toán nốt phần còn lại khi trả xe
     */
    boolean processFinalPayment(Long orderId, PaymentMethod method, BigDecimal extraFee, String damages, RedirectAttributes ra);

    /**
     * Tính số tiền còn lại cần thanh toán
     */
    BigDecimal calculateRemainingAmount(RentalOrder order, BigDecimal extraFee);

    /**
     * Lấy payment của đơn hàng theo loại giao dịch
     */
    Payment getPaymentByOrderAndType(Long orderId, TransactionType type);

    /**
     * Kiểm tra đã thanh toán cọc chưa
     */
    boolean isDepositPaid(Long orderId);

    /**
     * Cập nhật trạng thái payment
     */
    Payment updatePaymentStatus(Long paymentId, PaymentStatus status, String transactionId);

    /**
     * Hiển thị trang thanh toán
     */
    String showCheckoutPage(Long orderId, Model model, RedirectAttributes ra);

    /**
     * Hiển thị trang thanh toán nốt
     */
    String showFinalPaymentPage(Long orderId, BigDecimal extraFee, String damages, Model model);
}