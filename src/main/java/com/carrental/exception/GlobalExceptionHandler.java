package com.carrental.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Xử lý lỗi 404 — trang không tồn tại
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handle404(Model model) {
        model.addAttribute("error", "Trang bạn tìm không tồn tại");
        return "error/404";
    }

    // Xử lý lỗi không có quyền truy cập
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handle403(Model model) {
        model.addAttribute("error", "Bạn không có quyền truy cập trang này");
        return "error/403";
    }

    // Xử lý RuntimeException — lỗi nghiệp vụ chung
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntime(RuntimeException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error/500";
    }
}