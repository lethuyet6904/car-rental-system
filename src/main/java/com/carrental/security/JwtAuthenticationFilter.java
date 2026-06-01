package com.carrental.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
        // OncePerRequestFilter đảm bảo filter chỉ chạy 1 lần mỗi request

        private final JwtTokenProvider jwtTokenProvider;
        private final CustomUserDetailsService customUserDetailsService;

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain)
                        throws ServletException, IOException {

                // Bước 1: Đọc token từ cookie
                String token = jwtTokenProvider.getTokenFromCookie(request);

                // Bước 2: Nếu có token và token hợp lệ
                if (token != null && jwtTokenProvider.validateToken(token)) {

                        // Chỉ dùng JWT để xác định phone; role/authorities lấy từ DB (tránh token cũ sau khi Admin đổi role)
                        String phone = jwtTokenProvider.getPhoneFromToken(token);

                        try {
                                UserDetails userDetails = customUserDetailsService.loadUserByUsername(phone);

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                userDetails.getAuthorities());
                                authentication.setDetails(
                                        new WebAuthenticationDetailsSource().buildDetails(request));

                                SecurityContextHolder.getContext().setAuthentication(authentication);
                        } catch (UsernameNotFoundException ignored) {
                                // Token hợp lệ nhưng user đã bị xóa — không set authentication
                        }
                }

                // Bước 6: Tiếp tục chain — cho request đi tiếp dù có token hay không
                filterChain.doFilter(request, response);
        }
}