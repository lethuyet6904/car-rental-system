package com.carrental.controller;

import com.carrental.dto.response.ComplaintResponse;
import com.carrental.entity.RentalOrder;
import com.carrental.entity.User;
import com.carrental.repository.RentalOrderRepository;
import com.carrental.service.ComplaintService;
import com.carrental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/complaint")
@RequiredArgsConstructor
public class ComplaintController {

    private static final int PAGE_SIZE = 10;

    private final ComplaintService complaintService;
    private final UserService userService;
    private final RentalOrderRepository rentalOrderRepository;

    @GetMapping("/my-complaints")
    public String myComplaints(@RequestParam(defaultValue = "0") int page,
                               Authentication auth,
                               Model model) {
        Page<ComplaintResponse> complaints = complaintService.getMyComplaints(
                auth, PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt")));

        model.addAttribute("complaints", complaints);
        model.addAttribute("currentPage", complaints.getNumber());
        model.addAttribute("totalPages", complaints.getTotalPages());
        return "pages/complaint/my-complaints";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Authentication auth,
                         Model model) {
        ComplaintResponse complaint = complaintService.getMyComplaintDetail(id, auth);
        User user = userService.findByPhone(auth.getName());

        model.addAttribute("complaint", complaint);
        model.addAttribute("user", user);
        model.addAttribute("backUrl", resolveOrderDetailUrl(complaint.getOrderId(), user));
        return "pages/complaint/complaint-detail";
    }

    @PostMapping("/submit")
    public String submit(@RequestParam Long orderId,
                         @RequestParam String type,
                         @RequestParam String content,
                         @RequestParam(required = false) List<MultipartFile> evidenceImages,
                         Authentication auth,
                         RedirectAttributes ra) {
        try {
            Long complaintId = complaintService.submitComplaint(orderId, type, content, evidenceImages, auth);
            ra.addFlashAttribute("success",
                    "Khiếu nại đã được gửi. Chúng tôi sẽ xử lý trong 24–48 giờ.");
            return "redirect:/complaint/" + complaintId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:" + resolveErrorRedirect(orderId, auth);
        }
    }

    private String resolveErrorRedirect(Long orderId, Authentication auth) {
        User user = userService.findByPhone(auth.getName());
        return resolveOrderDetailUrl(orderId, user);
    }

    private String resolveOrderDetailUrl(Long orderId, User user) {
        if (user == null) {
            return "/auth/login";
        }
        RentalOrder order = rentalOrderRepository.findDetailedById(orderId).orElse(null);
        if (order != null && order.getCar().getOwner().getUserId().equals(user.getUserId())) {
            return "/owner/orders/" + orderId;
        }
        return "/booking/order/" + orderId;
    }
}
