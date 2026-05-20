package com.carrental.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // OncePerRequestFilter đảm bảo filter chỉ chạy 1 lần mỗi request

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Bước 1: Đọc token từ cookie
        String token = jwtTokenProvider.getTokenFromCookie(request);

        // Bước 2: Nếu có token và token hợp lệ
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // Bước 3: Lấy phone từ token
            String phone = jwtTokenProvider.getPhoneFromToken(token);

            // Bước 4: Load UserDetails từ DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(phone);

            // Bước 5: Tạo Authentication object và set vào SecurityContext
            // Từ đây Spring Security biết request này đến từ ai, có role gì
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,                          // credentials = null (đã xác thực rồi)
                            userDetails.getAuthorities()   // ["ROLE_Admin"] hoặc ["ROLE_Customer"]
                    );
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Bước 6: Tiếp tục chain — cho request đi tiếp dù có token hay không
        filterChain.doFilter(request, response);
    }
}