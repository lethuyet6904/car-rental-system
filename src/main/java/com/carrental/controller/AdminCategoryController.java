package com.carrental.controller;

import com.carrental.service.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('Admin')")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    // ── CarType ──────────────────────────────────────────────────

    @GetMapping("/car-types")
    public String carTypeList(@RequestParam(required = false) String keyword,
                              Model model) {
        model.addAttribute("types",   adminCategoryService.getTypeList(keyword));
        model.addAttribute("keyword", keyword);
        return "pages/admin/cat-type";
    }

    @PostMapping("/car-types/add")
    public String addCarType(@RequestParam String name,
                             @RequestParam(required = false) String description,
                             RedirectAttributes ra) {
        try {
            adminCategoryService.addCarType(name, description);
            ra.addFlashAttribute("successMessage",
                    "Đã thêm loại xe \"" + name + "\" thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories/car-types";
    }

    @PostMapping("/car-types/{id}/edit")
    public String editCarType(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam(required = false) String description,
                              @RequestParam String status,
                              RedirectAttributes ra) {
        try {
            adminCategoryService.editCarType(id, name, description, status);
            ra.addFlashAttribute("successMessage", "Đã cập nhật loại xe thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories/car-types";
    }

    @PostMapping("/car-types/{id}/delete")
    public String deleteCarType(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminCategoryService.deleteCarType(id);
            ra.addFlashAttribute("successMessage", "Đã xóa loại xe thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories/car-types";
    }

    // ── Brand ────────────────────────────────────────────────────

    @GetMapping("/brands")
    public String brandList(@RequestParam(required = false) String keyword,
                            Model model) {
        model.addAttribute("brands",  adminCategoryService.getBrandList(keyword));
        model.addAttribute("keyword", keyword);
        return "pages/admin/cat-brand";
    }

    @PostMapping("/brands/add")
    public String addBrand(@RequestParam String name,
                           @RequestParam(value = "logo", required = false) MultipartFile logoFile,
                           RedirectAttributes ra) {
        try {
            String logoUrl = saveLogoFile(logoFile);
            
            // Lưu ý: Bạn cần mở file AdminCategoryService ra và thêm tham số logoUrl vào method này
            adminCategoryService.addBrand(name, logoUrl);
            
            ra.addFlashAttribute("successMessage",
                    "Đã thêm hãng xe \"" + name + "\" thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories/brands";
    }

    @PostMapping("/brands/{id}/edit")
    public String editBrand(@PathVariable Long id,
                            @RequestParam String name,
                            @RequestParam String status,
                            @RequestParam(value = "logo", required = false) MultipartFile logoFile,
                            RedirectAttributes ra) {
        try {
            String logoUrl = saveLogoFile(logoFile);
            
            // Lưu ý: Bạn cần mở file AdminCategoryService ra và thêm tham số logoUrl vào method này
            adminCategoryService.editBrand(id, name, status, logoUrl);
            
            ra.addFlashAttribute("successMessage", "Đã cập nhật hãng xe thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories/brands";
    }

    @PostMapping("/brands/{id}/delete")
    public String deleteBrand(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminCategoryService.deleteBrand(id);
            ra.addFlashAttribute("successMessage", "Đã xóa hãng xe thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories/brands";
    }

    // ── Region ───────────────────────────────────────────────────

    @GetMapping("/regions")
    public String regionList(@RequestParam(required = false) String keyword,
                             Model model) {
        model.addAttribute("regions", adminCategoryService.getRegionList(keyword));
        model.addAttribute("keyword", keyword);
        return "pages/admin/cat-region";
    }

    @PostMapping("/regions/add")
    public String addRegion(@RequestParam String name, RedirectAttributes ra) {
        try {
            adminCategoryService.addRegion(name);
            ra.addFlashAttribute("successMessage",
                    "Đã thêm khu vực \"" + name + "\" thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories/regions";
    }

    @PostMapping("/regions/{id}/edit")
    public String editRegion(@PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam String status,
                             RedirectAttributes ra) {
        try {
            adminCategoryService.editRegion(id, name, status);
            ra.addFlashAttribute("successMessage", "Đã cập nhật khu vực thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories/regions";
    }

    @PostMapping("/regions/{id}/delete")
    public String deleteRegion(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminCategoryService.deleteRegion(id);
            ra.addFlashAttribute("successMessage", "Đã xóa khu vực thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories/regions";
    }

    // ── Helper: Lưu file ảnh ─────────────────────────────────────
    
    private static final long MAX_LOGO_SIZE = 2 * 1024 * 1024; // 2MB
    private static final java.util.Set<String> ALLOWED_EXTENSIONS =
            java.util.Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg");

    private String saveLogoFile(MultipartFile logoFile) throws Exception {
        if (logoFile == null || logoFile.isEmpty()) {
            return null;
        }

        // Validate MIME type
        String contentType = logoFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Chỉ chấp nhận file ảnh (JPG, PNG, WEBP, SVG...)");
        }

        // Validate file size
        if (logoFile.getSize() > MAX_LOGO_SIZE) {
            throw new RuntimeException("Logo không được vượt quá 2MB");
        }

        // Validate extension
        String originalFilename = logoFile.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("Định dạng file không được hỗ trợ. Chấp nhận: JPG, PNG, GIF, WEBP, SVG");
        }

        // Generate safe filename (no original name to prevent path traversal)
        String newFilename = UUID.randomUUID().toString() + extension;

        String uploadDir = "uploads/brands/";
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(newFilename).normalize();

        // Đảm bảo file path vẫn nằm trong upload directory (chống path traversal)
        if (!filePath.startsWith(uploadPath)) {
            throw new RuntimeException("Đường dẫn file không hợp lệ");
        }

        Files.copy(logoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return newFilename;
    }
}