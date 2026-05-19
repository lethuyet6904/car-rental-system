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
            .csrf(csrf -> csrf.disable())
            // Dùng STATELESS vì JWT tự mang thông tin auth, không cần server lưu session
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── PUBLIC ── Ai cũng vào được
                .requestMatchers(
                    "/",
                    "/home",
                    "/auth/login",
                    "/auth/register",
                    "/auth/forgot-password",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/error"
                ).permitAll()

                // ── ADMIN ONLY ── Chỉ role Admin
                .requestMatchers("/admin/**").hasRole("Admin")

                // ── OWNER ONLY ── Chỉ role Owner
                .requestMatchers("/owner/**").hasRole("Owner")

                // ── CUSTOMER ── Customer và Owner đều vào được
                .requestMatchers("/customer/**").hasAnyRole("Customer", "Owner")

                // Còn lại phải đăng nhập
                .anyRequest().authenticated()
            )
            // Khi chưa đăng nhập mà vào trang cần auth → redirect về login
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendRedirect("/auth/login"))
                // Khi đã đăng nhập nhưng không đủ quyền → redirect về 403
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    response.sendRedirect("/error/403"))
            )
            // Thêm JWT filter vào TRƯỚC filter mặc định của Spring Security
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt để hash password — KHÔNG bao giờ lưu plain text
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager dùng trong AuthService khi xử lý login
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}