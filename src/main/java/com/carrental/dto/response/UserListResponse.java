package com.carrental.dto.response;

import com.carrental.entity.User;
import com.carrental.enums.UserRole;
import com.carrental.enums.UserStatus;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class UserListResponse {

    private final Long userId;
    private final String fullName;
    private final String phone;
    private final String email;
    private final UserRole role;
    private final UserStatus status;
    private final LocalDateTime createdAt;

    // Dùng static factory method thay vì constructor thủ công
    // Lý do: gọi UserListResponse.from(user) rõ nghĩa hơn new UserListResponse(user)
    public static UserListResponse from(User user) {
        UserListResponse res = new UserListResponse(user);
        return res;
    }

    private UserListResponse(User user) {
        this.userId    = user.getUserId();
        this.fullName  = user.getFullName();
        this.phone     = user.getPhone();
        this.email     = user.getEmail();
        this.role      = user.getRole();
        this.status    = user.getStatus();
        this.createdAt = user.getCreatedAt();
    }
}