package com.carrental.controller;

import com.carrental.dto.response.AdminComplaintDetailResponse;
import com.carrental.dto.response.AdminComplaintListResponse;
import com.carrental.enums.ComplaintStatus;
import com.carrental.enums.ComplaintType;
import com.carrental.service.AdminComplaintService;
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
@RequestMapping("/admin/complaints")
@PreAuthorize("hasRole('Admin')")
@RequiredArgsConstructor
public class AdminComplaintController {

    private static final int PAGE_SIZE = 10;

    private final AdminComplaintService adminComplaintService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        ComplaintStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try { statusEnum = ComplaintStatus.valueOf(status); }
            catch (IllegalArgumentException ignored) {}
        }

        ComplaintType typeEnum = null;
        if (type != null && !type.isBlank()) {
            try { typeEnum = ComplaintType.valueOf(type); }
            catch (IllegalArgumentException ignored) {}
        }

        Pageable pageable = PageRequest.of(page, PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AdminComplaintListResponse> complaints =
                adminComplaintService.getComplaintList(statusEnum, typeEnum, keyword, pageable);

        model.addAttribute("complaints",   complaints);
        model.addAttribute("status",       status);
        model.addAttribute("type",         type);
        model.addAttribute("keyword",      keyword);
        model.addAttribute("statuses",     ComplaintStatus.values());
        model.addAttribute("types",        ComplaintType.values());
        model.addAttribute("extraParams",  buildExtraParams(status, type, keyword));

        return "pages/admin/complaint-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        AdminComplaintDetailResponse complaint =
                adminComplaintService.getComplaintDetail(id);
        model.addAttribute("complaint", complaint);
        model.addAttribute("statuses",  ComplaintStatus.values());
        return "pages/admin/complaint-detail";
    }

    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable Long id,
                          @RequestParam String status,
                          @RequestParam(required = false) String resolution,
                          RedirectAttributes ra) {
    	if ((status.equals("Resolved") || status.equals("Rejected"))
    	        && (resolution == null || resolution.isBlank())) {
    	    ra.addFlashAttribute("errorMessage", "Vui lòng nhập phán quyết");
    	    return "redirect:/admin/complaints/" + id;
    	}
        try {
            ComplaintStatus newStatus = ComplaintStatus.valueOf(status);
            adminComplaintService.resolve(id, newStatus, resolution);
            ra.addFlashAttribute("successMessage", "Đã cập nhật xử lý khiếu nại thành công");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/complaints/" + id;
    }

    private String buildExtraParams(String status, String type, String keyword) {
        StringBuilder sb = new StringBuilder();
        if (status  != null && !status.isBlank())  sb.append("&status=").append(status);
        if (type    != null && !type.isBlank())    sb.append("&type=").append(type);
        if (keyword != null && !keyword.isBlank()) sb.append("&keyword=").append(keyword);
        return sb.toString();
    }
}