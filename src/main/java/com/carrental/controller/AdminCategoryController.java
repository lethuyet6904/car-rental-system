package com.carrental.controller;

import com.carrental.service.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String addBrand(@RequestParam String name, RedirectAttributes ra) {
        try {
            adminCategoryService.addBrand(name);
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
                            RedirectAttributes ra) {
        try {
            adminCategoryService.editBrand(id, name, status);
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
}