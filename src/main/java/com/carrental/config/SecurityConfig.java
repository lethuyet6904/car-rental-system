package com.carrental.config;

import com.carrental.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth

                // ── PUBLIC ── Ai cũng vào được
                .requestMatchers(
                    "/",
                    "/home",
                    "/auth/login",
                    "/auth/register",
                    "/auth/forgot-password",
                    "/auth/logout",
                    "/cars/**",
                    "/assets/**",
                    "/error",
                    "/uploads/**"
                    // LƯU Ý: BỎ /verification/** và /owner/owner-registration khỏi permitAll
                    // Các route đó cần đăng nhập, được cấu hình riêng bên dưới
                ).permitAll()

                // ── ADMIN ONLY ── Chỉ role Admin
                .requestMatchers("/admin/**").hasRole("Admin")

                // ── OWNER ONLY ── Chỉ role Owner
                // Lưu ý: /owner/owner-registration cho Customer truy cập (để đăng ký lên Owner)
                // nên không đặt toàn bộ /owner/** vào hasRole("Owner")
                .requestMatchers("/owner/dashboard").hasRole("Owner")
                .requestMatchers("/owner/cars/**").hasRole("Owner")
                .requestMatchers("/owner/orders/**").hasRole("Owner")

                // ── CUSTOMER & OWNER ── Cả hai role đều vào được
                .requestMatchers("/customer/**").hasAnyRole("Customer", "Owner")

                // Trang đăng ký làm chủ xe: Customer mới cần vào, Owner đã là chủ rồi
                .requestMatchers("/owner/owner-registration").hasAnyRole("Customer", "Owner")

                // Trang xác minh danh tính: Customer và Owner đều cần vào
                .requestMatchers("/verification/**").hasAnyRole("Customer", "Owner")

                // Đánh giá chuyến đi: chỉ Customer
                .requestMatchers("/review/**").hasRole("Customer")

                // Khiếu nại: Customer và Owner
                .requestMatchers("/complaint/**").hasAnyRole("Customer", "Owner")

                // Còn lại phải đăng nhập
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendRedirect("/auth/login"))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    response.sendRedirect("/error/403"))
            )
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}