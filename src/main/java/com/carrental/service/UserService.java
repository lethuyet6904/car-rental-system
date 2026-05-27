package com.carrental.service;

import java.util.List;

import com.carrental.dto.request.OwnerRegistrationRequest;
import com.carrental.dto.request.RegisterRequest;
import com.carrental.entity.RentalOrder;
import com.carrental.entity.User;

public interface UserService {
    User register(RegisterRequest request);
    User login(String phone, String password);
    void resetPassword(String email, String newPassword);

    User findByPhone(String phone);

    void applyForOwner(Long userId, OwnerRegistrationRequest request);
    
    // THÊM PHƯƠNG THỨC MỚI
    User getUserWithIdentity(Long userId);
    User updateUser(User user);                    // ← THÊM
    
    List<RentalOrder> getOrdersByCustomer(Long customerId);
}