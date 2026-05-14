package com.wms.web.controller;

import com.wms.web.model.SessionUser;
import com.wms.web.service.PortalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StaffController {

    private final PortalService portalService;

    public StaffController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/staff/bookings")
    public String bookings(HttpSession session, Model model) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        model.addAttribute("user", user);
        model.addAttribute("bookings", portalService.getAllBookings());
        return "web/staff-bookings";
    }

    @PostMapping("/staff/bookings/{id}/confirm")
    public String confirm(@PathVariable("id") String bookingId, RedirectAttributes redirectAttributes) {
        portalService.confirmBooking(bookingId);
        redirectAttributes.addFlashAttribute("success", "Đã xác nhận yêu cầu đặt chỗ.");
        return "redirect:/staff/bookings";
    }

    @PostMapping("/staff/bookings/{id}/used")
    public String used(@PathVariable("id") String bookingId, RedirectAttributes redirectAttributes) {
        portalService.markUsed(bookingId);
        redirectAttributes.addFlashAttribute("success", "Phiên đặt chỗ đã được đánh dấu đã sử dụng.");
        return "redirect:/staff/bookings";
    }
}
