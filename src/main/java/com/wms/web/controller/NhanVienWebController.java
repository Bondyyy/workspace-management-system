package com.wms.web.controller;

import com.wms.web.model.NguoiDungPhien;
import com.wms.web.service.CongThongTinService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class NhanVienWebController {

    private final CongThongTinService congThongTinService;

    public NhanVienWebController(CongThongTinService congThongTinService) {
        this.congThongTinService = congThongTinService;
    }

    @GetMapping("/staff/bookings")
    public String hienThiDatCho(HttpSession session, Model model) {
        NguoiDungPhien user = (NguoiDungPhien) session.getAttribute("user");
        model.addAttribute("user", user);
        model.addAttribute("hienThiDatCho", congThongTinService.layTatCaDatCho());
        return "web/nhan-vien-dat-cho";
    }

    @PostMapping("/staff/bookings/{id}/xacNhan")
    public String xacNhan(@PathVariable("id") String bookingId, RedirectAttributes redirectAttributes) {
        congThongTinService.xacNhanDatChoDaThanhToan(bookingId);
        redirectAttributes.addFlashAttribute("success", "Đã xác nhận yêu cầu đặt chỗ.");
        return "redirect:/staff/bookings";
    }

    @PostMapping("/staff/bookings/{id}/danhDauDaSuDung")
    public String danhDauDaSuDung(@PathVariable("id") String bookingId, RedirectAttributes redirectAttributes) {
        congThongTinService.danhDauDatChoDaSuDung(bookingId);
        redirectAttributes.addFlashAttribute("success", "Phiên đặt chỗ đã được đánh dấu đã sử dụng.");
        return "redirect:/staff/bookings";
    }
}
