	package com.carrental.controller;
	
	import com.carrental.dto.response.AdminOwnerRequestDetailResponse;
	import com.carrental.dto.response.AdminOwnerRequestListResponse;
	import com.carrental.enums.VerificationStatus;
	import com.carrental.service.AdminOwnerRequestService;
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
	@RequestMapping("/admin/owner-requests")
	@PreAuthorize("hasRole('Admin')")
	@RequiredArgsConstructor
	public class AdminOwnerRequestController {
	
	    private static final int PAGE_SIZE = 10;
	
	    private final AdminOwnerRequestService adminOwnerRequestService;
	
	    @GetMapping
	    public String list(
	            @RequestParam(required = false) String status,
	            @RequestParam(required = false) String keyword,
	            @RequestParam(defaultValue = "0") int page,
	            Model model) {
	
	        VerificationStatus statusEnum = null;
	        if (status != null && !status.isBlank()) {
	            try { statusEnum = VerificationStatus.valueOf(status); }
	            catch (IllegalArgumentException ignored) {}
	        }
	
	        Pageable pageable = PageRequest.of(page, PAGE_SIZE,
	                Sort.by(Sort.Direction.DESC, "submittedAt"));
	
	        Page<AdminOwnerRequestListResponse> requests =
	                adminOwnerRequestService.getRequestList(statusEnum, keyword, pageable);
	
	        model.addAttribute("requests",    requests);
	        model.addAttribute("status",      status);
	        model.addAttribute("keyword",     keyword);
	        model.addAttribute("statuses",    VerificationStatus.values());
	        model.addAttribute("extraParams", buildExtraParams(status, keyword));
	
	        return "pages/admin/owner-request-list";
	    }
	
	    @GetMapping("/{id}")
	    public String detail(@PathVariable Long id, @RequestParam(defaultValue = "0") int returnPage, Model model) {
	        AdminOwnerRequestDetailResponse request =
	                adminOwnerRequestService.getRequestDetail(id);
	        model.addAttribute("request", request);
	        model.addAttribute("returnPage", returnPage);
	        return "pages/admin/owner-request-detail";
	    }
	
	    @PostMapping("/{id}/approve")
	    public String approve(@PathVariable Long id, RedirectAttributes ra) {
	        try {
	            adminOwnerRequestService.approveOwnerRequest(id);
	            ra.addFlashAttribute("successMessage",
	                    "Đã duyệt thành công. Người dùng đã được nâng lên Owner.");
	        } catch (IllegalStateException e) {
	            ra.addFlashAttribute("errorMessage", e.getMessage());
	        }
	        return "redirect:/admin/owner-requests/" + id;
	    }
	
	    @PostMapping("/{id}/reject")
	    public String reject(@PathVariable Long id,
	                         @RequestParam String rejectReason,
	                         RedirectAttributes ra) {
	        if (rejectReason == null || rejectReason.isBlank()) {
	            ra.addFlashAttribute("errorMessage", "Vui lòng nhập lý do từ chối");
	            return "redirect:/admin/owner-requests/" + id;
	        }
	        try {
	            adminOwnerRequestService.rejectOwnerRequest(id, rejectReason);
	            ra.addFlashAttribute("successMessage", "Đã từ chối yêu cầu");
	        } catch (IllegalStateException e) {
	            ra.addFlashAttribute("errorMessage", e.getMessage());
	        }
	        return "redirect:/admin/owner-requests/" + id;
	    }
	
	    private String buildExtraParams(String status, String keyword) {
	        StringBuilder sb = new StringBuilder();
	        if (status  != null && !status.isBlank())  sb.append("&status=").append(status);
	        if (keyword != null && !keyword.isBlank()) sb.append("&keyword=").append(keyword);
	        return sb.toString();
	    }
	}