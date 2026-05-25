package com.wms.web.controller;

import com.wms.web.model.NguoiDungPhien;
import com.wms.web.service.CongThongTinService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TrangChuWebController {

    private final CongThongTinService congThongTinService;

    public TrangChuWebController(CongThongTinService congThongTinService) {
        this.congThongTinService = congThongTinService;
    }

    @GetMapping("/")
    public String trangChu(HttpSession session, Model model) {
        NguoiDungPhien user = (NguoiDungPhien) session.getAttribute("user");
        model.addAttribute("user", user);
        model.addAttribute("hienThiChiNhanh", congThongTinService.layChiNhanh().stream().limit(3).toList());
        return "web/trang-chu";
    }

    @GetMapping("/trang-chu")
    public String hienThiTrangChu(HttpSession session, Model model) {
        return trangChu(session, model);
    }
}
