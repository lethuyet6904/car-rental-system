package com.carrental.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Cấu hình để Spring MVC serve được file ảnh đã upload qua URL /uploads/...
 *
 * Ví dụ: ảnh lưu tại  D:/project/uploads/cccd/front/abc.jpg
 *         truy cập qua http://localhost:8080/uploads/cccd/front/abc.jpg
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Chuyển đường dẫn thành absolute URI để Windows không bị lỗi
        String absoluteUploadPath = Paths.get(uploadPath)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        // Map URL /uploads/** → thư mục uploads thực tế trên disk
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absoluteUploadPath);
    }
}