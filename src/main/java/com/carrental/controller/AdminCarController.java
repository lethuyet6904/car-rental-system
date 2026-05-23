package com.carrental.controller;

import com.carrental.enums.CarStatus;
import com.carrental.enums.FuelType;
import com.carrental.enums.TransmissionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/cars")
@PreAuthorize("hasRole('Admin')")
@RequiredArgsConstructor
public class AdminCarController {

    private static final int PAGE_SIZE = 10;

    @Getter
    @AllArgsConstructor
    private static class MockCarItem {
        private Long carId;
        private String modelName;
        private String licensePlate;
        private String brandName;
        private String typeName;
        private String ownerName;
        private String firstImageUrl;
        private LocalDateTime createdAt;
        private CarStatus status;
    }

    @Getter
    @AllArgsConstructor
    private static class MockCarDetail {
        // Thông tin xe
        private Long carId;
        private String modelName;
        private String licensePlate;
        private String brandName;
        private String typeName;
        private Integer seats;
        private Integer yearOfManufacture;
        private FuelType fuel;
        private TransmissionType transmission;
        private BigDecimal pricePerDay;
        private String regionName;
        private String features;
        private String description;
        private CarStatus status;
        private String rejectReason;
        private LocalDateTime createdAt;
        // Ảnh xe
        private List<String> images;
        // Chủ xe
        private Long ownerId;
        private String ownerName;
        private String ownerPhone;
        // Giấy tờ xe
        private String registrationFrontImage;
        private String registrationBackImage;
        private String insuranceImage;
        private String inspectionImage;
    }

    // ── GET: Danh sách xe ────────────────────────────────────────
    @GetMapping
    public String list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        List<MockCarItem> mockList = List.of(
            new MockCarItem(1L, "Toyota Camry 2022",  "51A-123.45", "Toyota", "Sedan",   "Trần Hùng",   null, LocalDateTime.of(2025,5,18,10,0),  CarStatus.Pending),
            new MockCarItem(2L, "Honda CR-V 2023",    "51B-678.90", "Honda",  "SUV",     "Nguyễn B",    null, LocalDateTime.of(2025,5,19,9,0),   CarStatus.Pending),
            new MockCarItem(3L, "Ford Ranger 2021",   "43C-222.11", "Ford",   "Bán tải", "Lê C",        null, LocalDateTime.of(2025,5,20,14,0),  CarStatus.Pending),
            new MockCarItem(4L, "Hyundai Tucson 2022","51D-333.22", "Hyundai","SUV",     "Phạm D",      null, LocalDateTime.of(2025,5,15,8,0),   CarStatus.Active),
            new MockCarItem(5L, "Kia Morning 2021",   "51E-444.33", "Kia",    "Hatchback","Hoàng E",    null, LocalDateTime.of(2025,5,10,11,0),  CarStatus.Rejected)
        );

        Page<MockCarItem> pageResult = new PageImpl<>(
            mockList, PageRequest.of(page, PAGE_SIZE), mockList.size()
        );

        model.addAttribute("cars",        pageResult);
        model.addAttribute("status",      status);
        model.addAttribute("keyword",     keyword);
        model.addAttribute("carStatuses", CarStatus.values());
        model.addAttribute("extraParams", buildExtraParams(status, keyword));

        return "pages/admin/car-list";
    }

    // ── GET: Chi tiết xe ─────────────────────────────────────────
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("car", buildMockDetail(id));
        return "pages/admin/car-detail";
    }

    // ── POST: Duyệt xe ───────────────────────────────────────────
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        // TODO: adminCarService.approve(id)
        ra.addFlashAttribute("successMessage", "Đã duyệt xe thành công. Xe đã chuyển sang trạng thái Có sẵn.");
        return "redirect:/admin/cars/" + id;
    }

    // ── POST: Từ chối xe ─────────────────────────────────────────
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam String rejectReason,
                         RedirectAttributes ra) {
        if (rejectReason == null || rejectReason.isBlank()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng nhập lý do từ chối");
            return "redirect:/admin/cars/" + id;
        }
        // TODO: adminCarService.reject(id, rejectReason)
        ra.addFlashAttribute("successMessage", "Đã từ chối xe");
        return "redirect:/admin/cars/" + id;
    }

    // ── Mock helpers ─────────────────────────────────────────────
    private MockCarDetail buildMockDetail(Long id) {
        return switch (id.intValue()) {
            case 2 -> new MockCarDetail(
                2L, "Honda CR-V 2023", "51B-678.90",
                "Honda", "SUV", 7, 2023,
                FuelType.Gasoline, TransmissionType.Automatic,
                BigDecimal.valueOf(1100000), "TP. Hồ Chí Minh",
                "Camera 360, Cảm biến đỗ xe, Apple CarPlay",
                "Xe gia đình 7 chỗ, nội thất rộng rãi.",
                CarStatus.Pending, null,
                LocalDateTime.of(2025,5,19,9,0),
                List.of(), 5L, "Nguyễn B", "0944333444",
                null, null, null, null
            );
            case 4 -> new MockCarDetail(
                4L, "Hyundai Tucson 2022", "51D-333.22",
                "Hyundai", "SUV", 5, 2022,
                FuelType.Gasoline, TransmissionType.Automatic,
                BigDecimal.valueOf(900000), "Hà Nội",
                "Màn hình cảm ứng, Ghế da",
                "Xe SUV 5 chỗ tiết kiệm nhiên liệu.",
                CarStatus.Active, null,
                LocalDateTime.of(2025,5,15,8,0),
                List.of(), 6L, "Phạm D", "0955444555",
                null, null, null, null
            );
            case 5 -> new MockCarDetail(
                5L, "Kia Morning 2021", "51E-444.33",
                "Kia", "Hatchback", 4, 2021,
                FuelType.Gasoline, TransmissionType.Manual,
                BigDecimal.valueOf(450000), "Đà Nẵng",
                "Điều hòa, USB",
                "Xe đô thị nhỏ gọn, dễ đỗ xe.",
                CarStatus.Rejected, "Giấy đăng ký xe không hợp lệ",
                LocalDateTime.of(2025,5,10,11,0),
                List.of(), 7L, "Hoàng E", "0966555666",
                null, null, null, null
            );
            default -> new MockCarDetail(
                1L, "Toyota Camry 2022", "51A-123.45",
                "Toyota", "Sedan", 5, 2022,
                FuelType.Gasoline, TransmissionType.Automatic,
                BigDecimal.valueOf(850000), "TP. Hồ Chí Minh",
                "Camera lùi, Cảm biến đỗ xe, Android Auto",
                "Xe gia đình tiện nghi, tiết kiệm nhiên liệu.",
                CarStatus.Pending, null,
                LocalDateTime.of(2025,5,18,10,0),
                List.of(), 2L, "Trần Hùng", "0987654321",
                null, null, null, null
            );
        };
    }

    private String buildExtraParams(String status, String keyword) {
        StringBuilder sb = new StringBuilder();
        if (status  != null && !status.isBlank())  sb.append("&status=").append(status);
        if (keyword != null && !keyword.isBlank()) sb.append("&keyword=").append(keyword);
        return sb.toString();
    }
}