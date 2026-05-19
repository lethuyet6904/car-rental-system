package com.carrental.security;

import com.carrental.entity.User;
import com.carrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        // Spring Security gọi method này khi cần xác thực user
        // "username" ở đây là số điện thoại (phone) — field dùng để đăng nhập
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy user với phone: " + phone));

        return new org.springframework.security.core.userdetails.User(
                user.getPhone(),
                user.getPassword(),
                // ROLE_ prefix là bắt buộc của Spring Security
                // Ví dụ: role = "Admin" → authority = "ROLE_Admin"
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}