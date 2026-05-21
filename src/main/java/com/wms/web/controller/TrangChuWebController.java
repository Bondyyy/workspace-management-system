package com.wms.web.controller;

import com.wms.web.model.NguoiDungPhien;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TrangChuWebController {

    @GetMapping("/")
    public String trangChu(HttpSession session) {
        NguoiDungPhien user = (NguoiDungPhien) session.getAttribute("user");
        if (user == null) {
            return "redirect:/dangNhap";
        }
        return user.laNhanVien() ? "redirect:/staff/bookings" : "redirect:/portal";
    }
}
