package com.carrental.controller;

import com.carrental.dto.response.AdminOrderListResponse;
import com.carrental.enums.OrderStatus;
import com.carrental.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('Admin')")
@RequiredArgsConstructor
public class AdminOrderController {

    private static final int PAGE_SIZE = 10;

    private final AdminOrderService adminOrderService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        OrderStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try { statusEnum = OrderStatus.valueOf(status); }
            catch (IllegalArgumentException ignored) {}
        }

        Pageable pageable = PageRequest.of(page, PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AdminOrderListResponse> orders =
                adminOrderService.getOrderList(statusEnum, timeRange, keyword, pageable);

        model.addAttribute("orders",      orders);
        model.addAttribute("status",      status);
        model.addAttribute("timeRange",   timeRange);
        model.addAttribute("keyword",     keyword);
        model.addAttribute("statuses",    OrderStatus.values());
        model.addAttribute("extraParams", buildExtraParams(status, timeRange, keyword));

        return "pages/admin/order-list";
    }
    
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("order", adminOrderService.getOrderDetail(id));
        return "pages/admin/order-detail";
    }

    private String buildExtraParams(String status, String timeRange, String keyword) {
        StringBuilder sb = new StringBuilder();
        if (status    != null && !status.isBlank())    sb.append("&status=").append(status);
        if (timeRange != null && !timeRange.isBlank()) sb.append("&timeRange=").append(timeRange);
        if (keyword   != null && !keyword.isBlank())   sb.append("&keyword=").append(keyword);
        return sb.toString();
    }
}