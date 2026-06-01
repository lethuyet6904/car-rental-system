package com.carrental.controller;

import com.carrental.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/submit")
    public String submitReview(@RequestParam Long orderId,
                               @RequestParam int rating,
                               @RequestParam(required = false) String comment,
                               Authentication auth,
                               RedirectAttributes ra) {
        try {
            reviewService.submitReview(orderId, rating, comment, auth);
            ra.addFlashAttribute("success", "Cảm ơn bạn đã đánh giá chuyến đi!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/booking/order/" + orderId;
    }
}
