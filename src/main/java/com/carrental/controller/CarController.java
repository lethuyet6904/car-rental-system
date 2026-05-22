package com.carrental.controller;

import com.carrental.dto.response.CarDetailResponse;
import com.carrental.dto.response.CarListResponse;
import com.carrental.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping({"", "/search"})
    public String searchCars(
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "fuel", required = false) String fuel,
            @RequestParam(name = "transmission", required = false) String transmission,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            Model model) {

        // Tạo đối tượng Pageable
        Pageable pageable = PageRequest.of(page, size);

        // Gọi hàm gộp từ Service
        Page<CarListResponse> carPage = carService.searchActiveCars(city, null, fuel, transmission, pageable);

        // Trả về danh sách xe cho vòng lặp th:each trong HTML
        model.addAttribute("cars", carPage.getContent());

        // Trả về toàn bộ object Page để sau này bạn làm nút "Trang sau", "Trang trước"
        model.addAttribute("pageInfo", carPage);
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedFuel", fuel);
        model.addAttribute("selectedTransmission", transmission);

        // Trả về trang danh sách xe
        return "pages/cars/car-list";
    }

    @GetMapping("/{id}")
    public String carDetail(@PathVariable("id") Long id, Model model) {
        CarDetailResponse car = carService.getCarById(id);
        model.addAttribute("car", car);
        return "pages/cars/car-detail";
    }
}