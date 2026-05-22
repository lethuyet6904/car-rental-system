package com.carrental.service;

import com.carrental.dto.request.OwnerRegistrationRequest;
import com.carrental.dto.request.RegisterRequest;
import com.carrental.entity.User;

public interface UserService {
    User register(RegisterRequest request);
    User login(String phone, String password);
    void resetPassword(String email, String newPassword);

    User findByPhone(String phone);

    void applyForOwner(Long userId, OwnerRegistrationRequest request);
}