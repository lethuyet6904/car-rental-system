package com.carrental.controller;

import com.carrental.dto.response.AdminCarDetailResponse;
import com.carrental.dto.response.AdminCarListResponse;
import com.carrental.enums.CarStatus;
import com.carrental.service.AdminCarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/cars")
@PreAuthorize("hasRole('Admin')")
@RequiredArgsConstructor
public class AdminCarController {

    private static final int PAGE_SIZE = 10;

    private final AdminCarService adminCarService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        CarStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try { statusEnum = CarStatus.valueOf(status); }
            catch (IllegalArgumentException ignored) {}
        }

        Pageable pageable = PageRequest.of(page, PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AdminCarListResponse> cars =
                adminCarService.getCarList(statusEnum, keyword, pageable);

        model.addAttribute("cars",        cars);
        model.addAttribute("status",      status);
        model.addAttribute("keyword",     keyword);
        model.addAttribute("carStatuses", CarStatus.values());
        model.addAttribute("extraParams", buildExtraParams(status, keyword));

        return "pages/admin/car-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @RequestParam(defaultValue = "0") int returnPage, Model model) {
        AdminCarDetailResponse car = adminCarService.getCarDetail(id);
        model.addAttribute("car", car);
        model.addAttribute("returnPage", returnPage);
        return "pages/admin/car-detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id,  @RequestParam(defaultValue = "0") int returnPage, RedirectAttributes ra) {
        try {
            adminCarService.approve(id);
            ra.addFlashAttribute("successMessage",
                    "Đã duyệt xe thành công. Xe đã chuyển sang trạng thái Có sẵn.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/cars/" + id+ "?returnPage=" + returnPage;
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam String rejectReason,
                         @RequestParam(defaultValue = "0") int returnPage,
                         RedirectAttributes ra) {
        if (rejectReason == null || rejectReason.isBlank()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng nhập lý do từ chối");
            return "redirect:/admin/cars/" + id;
        }
        try {
            adminCarService.reject(id, rejectReason);
            ra.addFlashAttribute("successMessage", "Đã từ chối xe");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/cars/" + id + "?returnPage=" + returnPage;
    }

    private String buildExtraParams(String status, String keyword) {
        StringBuilder sb = new StringBuilder();
        if (status  != null && !status.isBlank())  sb.append("&status=").append(status);
        if (keyword != null && !keyword.isBlank()) sb.append("&keyword=").append(keyword);
        return sb.toString();
    }
}