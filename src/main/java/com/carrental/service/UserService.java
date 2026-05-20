package com.carrental.service;

import com.carrental.dto.OwnerRegistrationRequest;
import com.carrental.dto.RegisterRequest;
import com.carrental.entity.User;

public interface UserService {
    User register(RegisterRequest request);
    User login(String phone, String password);
    void resetPassword(String email, String newPassword);

    void applyForOwner(Long userId, OwnerRegistrationRequest request);
}