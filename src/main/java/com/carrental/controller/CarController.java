package com.carrental.controller;

import com.carrental.dto.response.CarDetailResponse;
import com.carrental.dto.response.CarListResponse;
import com.carrental.entity.Review;
import com.carrental.entity.User;
import com.carrental.enums.FuelType;
import com.carrental.enums.TransmissionType;
import com.carrental.repository.ReviewRepository;
import com.carrental.repository.CarScheduleRepository;
import com.carrental.service.CarService;
import com.carrental.service.IdentityVerificationService;
import com.carrental.service.UserService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;
    private final UserService userService;
    private final IdentityVerificationService identityVerificationService;
    private final ReviewRepository reviewRepository;
    private final CarScheduleRepository carScheduleRepository;

    @GetMapping({ "", "/search" })
    public String searchCars(
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(name = "brandId", required = false) Long brandId,
            @RequestParam(name = "carTypeId", required = false) Long carTypeId,
            @RequestParam(name = "fuel", required = false) String fuel,
            @RequestParam(name = "transmission", required = false) String transmission,
            @RequestParam(name = "seats", required = false) Integer seats,
            @RequestParam(name = "sortBy", defaultValue = "newest") String sortBy,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size,
            Model model) {

        String dateError = null;
        if (dateFrom != null && dateFrom.isBefore(LocalDate.now())) {
            dateError = "Ngày nhận không được ở quá khứ";
            dateFrom = null;
            dateTo = null;
        } else if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            dateError = "Ngày nhận phải trước ngày trả";
            dateFrom = null;
            dateTo = null;
        } else if ((dateFrom == null) != (dateTo == null)) {
            dateFrom = null;
            dateTo = null;
        }

        Pageable pageable = PageRequest.of(page, size);
        FuelType selectedFuelType = parseFuel(fuel);
        TransmissionType selectedTransmissionType = parseTransmission(transmission);
        Page<CarListResponse> carPage = carService.searchActiveCarsWithFilter(
                city, dateFrom, dateTo, brandId, carTypeId,
                selectedFuelType != null ? selectedFuelType.name() : null,
                selectedTransmissionType != null ? selectedTransmissionType.name() : null,
                seats, null, sortBy, pageable);

        model.addAttribute("brands", carService.getActiveBrands());
        model.addAttribute("carTypes", carService.getActiveCarTypes());
        model.addAttribute("cars", carPage.getContent());
        model.addAttribute("pageInfo", carPage);
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedDateFrom", dateFrom);
        model.addAttribute("selectedDateTo", dateTo);
        model.addAttribute("selectedBrandId", brandId);
        model.addAttribute("selectedCarTypeId", carTypeId);
        model.addAttribute("selectedFuel", fuel);
        model.addAttribute("selectedTransmission", transmission);
        model.addAttribute("selectedSeats", seats);
        model.addAttribute("selectedSortBy", sortBy);
        model.addAttribute("selectedSize", size);
        model.addAttribute("dateError", dateError);

        return "pages/cars/car-list";
    }

    private FuelType parseFuel(String fuel) {
        if (fuel == null || fuel.isBlank())
            return null;
        try {
            return FuelType.valueOf(fuel);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private TransmissionType parseTransmission(String transmission) {
        if (transmission == null || transmission.isBlank())
            return null;
        try {
            return TransmissionType.valueOf(transmission);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @GetMapping("/{id}")
    public String carDetail(@PathVariable("id") Long id, Model model) {

        CarDetailResponse car = carService.getCarById(id);
        if (car == null)
            return "redirect:/cars";
        model.addAttribute("car", car);

        // Reviews
        List<Review> reviews = reviewRepository.findByCarId(id);
        model.addAttribute("reviews", reviews);

        // Trạng thái đăng nhập & xác minh — dùng trong JS để kiểm tra trước khi submit
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
        boolean isVerified = false;

        if (isLoggedIn) {
            try {
                User currentUser = userService.findByPhone(auth.getName());
                if (currentUser != null) {
                    isVerified = identityVerificationService.isFullyVerified(currentUser.getUserId());
                }
            } catch (Exception ignored) {
            }
        }

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("isVerified", isVerified);

        return "pages/cars/car-detail";
    }

    @GetMapping("/{id}/check-availability")
    @ResponseBody
    public java.util.Map<String, Object> checkAvailability(
            @PathVariable("id") Long id,
            @RequestParam("pickupDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
            @RequestParam("returnDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate) {
        
        boolean hasConflict = carScheduleRepository.existsConflict(id, pickupDate, returnDate);
        return java.util.Map.of("available", !hasConflict);
    }
}
