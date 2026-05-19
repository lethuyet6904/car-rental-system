package com.carrental.service.impl;

import com.carrental.dto.OwnerRegistrationRequest;
import com.carrental.dto.RegisterRequest;
import com.carrental.entity.OwnerRegistration;
import com.carrental.entity.User;
import com.carrental.enums.UserRole;
import com.carrental.enums.UserStatus;
import com.carrental.repository.OwnerRegistrationRepository;
import com.carrental.repository.UserRepository;
import com.carrental.service.UserService;
import com.carrental.util.PasswordUtil;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OwnerRegistrationRepository ownerRegistrationRepository;

    public UserServiceImpl(UserRepository userRepository, 
                           OwnerRegistrationRepository ownerRegistrationRepository) {
        this.userRepository = userRepository;
        this.ownerRegistrationRepository = ownerRegistrationRepository;
    }

    @Override
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
        user.setPassword(PasswordUtil.hashPassword(req.getPassword()));
        user.setRole(UserRole.Customer);
        user.setStatus(UserStatus.Active);

        return userRepository.save(user);
    }
    @Override
    public User login(String phone, String password) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Sai số điện thoại hoặc mật khẩu"));

        if ("Locked".equals(user.getStatus())) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        if (!PasswordUtil.checkPassword(password, user.getPassword())) {
            throw new RuntimeException("Sai số điện thoại hoặc mật khẩu");
        }
        return user;
    }

    @Override

    public void applyForOwner(Long userId, OwnerRegistrationRequest req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (ownerRegistrationRepository.existsByUser_UserIdAndStatus(userId, "Pending")) {
            throw new RuntimeException("Bạn đã có đơn đăng ký đang chờ duyệt!");
        }

        OwnerRegistration reg = new OwnerRegistration();
        reg.setUser(user);
        reg.setStatus("Pending");

        ownerRegistrationRepository.save(reg);
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        user.setPassword(PasswordUtil.hashPassword(newPassword));
        userRepository.save(user);
    }

	@Override
	public void registerAsOwner(Long userId, OwnerRegistrationRequest request) {
		// TODO Auto-generated method stub
		
	}
}