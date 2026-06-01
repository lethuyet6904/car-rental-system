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
     * Xử lý thanh toán cọc (mock — luôn thành công).
     * Tạo Payment record với paymentMethod do user chọn,
     * cập nhật Order status: Pending → PendingApproval.
     */
    boolean processDepositPayment(Long orderId, PaymentMethod method, String transactionId, RedirectAttributes ra);

    /**
     * Xử lý thanh toán nốt phần còn lại khi trả xe.
     */
    boolean processFinalPayment(Long orderId, PaymentMethod method, BigDecimal extraFee, String damages, RedirectAttributes ra);

    /**
     * Xử lý hoàn tiền khi khách hủy đơn đã cọc (mock).
     * Tạo Payment record loại Refund với paymentMethod lấy từ Deposit gốc.
     * refundPercent = 0 → không tạo record (mất cọc).
     * refundPercent = 50 hoặc 100 → tạo Refund record.
     */
    boolean processRefund(Long orderId, int refundPercent, RedirectAttributes ra);

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
     * Hiển thị trang thanh toán cọc
     */
    String showCheckoutPage(Long orderId, Model model, RedirectAttributes ra);

    /**
     * Hiển thị trang thanh toán nốt
     */
    String showFinalPaymentPage(Long orderId, BigDecimal extraFee, String damages, Model model);

    /**
     * Thêm thông tin tài khoản ngân hàng của chủ xe vào model thanh toán
     */
    void addOwnerBankInfoToModel(RentalOrder order, Model model);
}