package com.carrental.service.impl;

import com.carrental.dto.request.OwnerRegistrationRequest;
import com.carrental.dto.request.RegisterRequest;
import com.carrental.entity.IdentityVerification;
import com.carrental.entity.OwnerRegistration;
import com.carrental.entity.RentalOrder;
import com.carrental.entity.User;
import com.carrental.enums.UserRole;
import com.carrental.enums.UserStatus;
import com.carrental.enums.VerificationStatus;
import com.carrental.repository.IdentityVerificationRepository;
import com.carrental.repository.OwnerRegistrationRepository;
import com.carrental.repository.RentalOrderRepository;
import com.carrental.repository.UserRepository;
import com.carrental.service.IdentityVerificationService;
import com.carrental.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository                 userRepository;
    private final OwnerRegistrationRepository    ownerRegistrationRepository;
    private final IdentityVerificationRepository identityVerificationRepository;
    private final RentalOrderRepository          rentalOrderRepository;
    private final PasswordEncoder                passwordEncoder;
    private final IdentityVerificationService    identityVerificationService;

    // ====================== ĐĂNG KÝ ======================
    @Override
    @Transactional
    public User register(RegisterRequest req) {
        if (userRepository.existsByPhone(req.getPhone())) {
            throw new RuntimeException("Số điện thoại đã được đăng ký");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        User user = new User();
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setAddress(req.getAddress());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(UserRole.Customer);
        user.setStatus(UserStatus.Active);

        return userRepository.save(user);
    }

    // ====================== ĐĂNG NHẬP ======================
    @Override
    @Transactional
    public User login(String phone, String password) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Sai số điện thoại hoặc mật khẩu"));

        if (UserStatus.Locked.equals(user.getStatus())) {
            throw new RuntimeException("Tài khoản đã bị khóa. Lý do: " + user.getLockReason());
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Sai số điện thoại hoặc mật khẩu");
        }
        return user;
    }

    // ====================== ĐĂNG KÝ LÀM CHỦ XE ======================
    @Override
    @Transactional
    public void applyForOwner(Long userId, OwnerRegistrationRequest req) {
        if (!identityVerificationService.isCccdApproved(userId)) {
            throw new RuntimeException("Bạn cần xác minh CCCD trước khi đăng ký làm chủ xe");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra đã là Owner chưa
        if (UserRole.Owner.equals(user.getRole())) {
            throw new RuntimeException("Bạn đã là chủ xe rồi!");
        }

        // Kiểm tra đang có đơn Pending không
        if (ownerRegistrationRepository.existsByUser_UserIdAndStatus(userId, VerificationStatus.Pending)) {
            throw new RuntimeException("Bạn đã có đơn đăng ký đang chờ duyệt! Vui lòng chờ admin xử lý.");
        }

        // KHÔNG tạo IdentityVerification ở đây nữa.
        // Flow đúng: user phải xác minh danh tính qua /verification/identity trước,
        // sau đó mới được phép truy cập trang đăng ký chủ xe.
        // OwnerController.submitOwnerRegistration() đã kiểm tra isIdentityVerified() rồi.
        IdentityVerification currentVerification = identityVerificationRepository
                .findTopByUserUserIdOrderBySubmittedAtDesc(userId)
                .orElse(null);
                
        if (currentVerification == null || !com.carrental.enums.VerificationStatus.Approved.equals(currentVerification.getStatus())) {
            throw new RuntimeException("Bạn cần hoàn tất xác minh danh tính (CCCD đã được duyệt) trước khi đăng ký chủ xe.");
        }

        OwnerRegistration reg = new OwnerRegistration();
        reg.setUser(user);
        reg.setIdentityVerification(currentVerification);
        reg.setBankName(req.getBankName().trim());
        reg.setBankAccount(req.getBankAccount().trim());
        reg.setAccountHolder(req.getAccountHolder().trim());
        ownerRegistrationRepository.save(reg);
    }

    // ====================== QUÊN MẬT KHẨU ======================
    @Override
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ====================== QUERY ======================
    @Override
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với phone: " + phone));
    }

    @Override
    public User getUserWithIdentity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    // ====================== CẬP NHẬT ======================
    @Override
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // ====================== ĐƠN HÀNG CỦA CUSTOMER ======================
    @Override
    public List<RentalOrder> getOrdersByCustomer(Long customerId) {
        User customer = userRepository.findById(customerId).orElse(null);
        if (customer == null) {
            return new ArrayList<>();
        }
        return rentalOrderRepository.findByCustomer(customer);
    }
}
