package com.carrental.controller;

import com.carrental.dto.response.CarListResponse;  // ← Sửa import này
import com.carrental.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping("/search")
    public String searchCars(@RequestParam(name = "city", required = false) String city, Model model) {
        List<CarListResponse> searchResults;  // ← Sửa List<Car> thành List<CarListResponse>

        if (city != null && !city.isBlank()) {
            searchResults = carService.searchActiveCarsByCity(city);
        } else {
            searchResults = carService.getActiveCars();
        }

        model.addAttribute("cars", searchResults);
        model.addAttribute("selectedCity", city);

        return "home";
    }
}