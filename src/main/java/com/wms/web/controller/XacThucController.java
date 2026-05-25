package com.wms.web.controller;

import com.wms.web.form.DangNhapWebForm;
import com.wms.web.form.MaOtpForm;
import com.wms.web.form.DangKyWebForm;
import com.wms.web.model.DangKyChoXacThuc;
import com.wms.web.model.NguoiDungPhien;
import com.wms.web.service.XacThucWebService;
import com.wms.web.util.WebErrorMessages;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class XacThucController {

    private final XacThucWebService xacThucWebService;

    public XacThucController(XacThucWebService xacThucWebService) {
        this.xacThucWebService = xacThucWebService;
    }

    @GetMapping("/dangNhap")
    public String hienThiDangNhap(HttpSession session, Model model) {
        NguoiDungPhien user = (NguoiDungPhien) session.getAttribute("user");
        if (user != null) {
            return user.laNhanVien() ? "redirect:/staff/bookings" : "redirect:/portal";
        }
        if (!model.containsAttribute("DangNhapWebForm")) {
            model.addAttribute("DangNhapWebForm", new DangNhapWebForm());
        }
        return "web/dang-nhap";
    }

    @PostMapping("/dangNhap")
    public String dangNhap(@Valid @ModelAttribute("DangNhapWebForm") DangNhapWebForm DangNhapWebForm,
                        BindingResult bindingResult,
                        HttpSession session,
                        Model model) {
        if (bindingResult.hasErrors()) {
            return "web/dang-nhap";
        }

        try {
            NguoiDungPhien user = xacThucWebService.dangNhap(DangNhapWebForm);
            session.setAttribute("user", user);
            return user.laNhanVien() ? "redirect:/staff/bookings" : "redirect:/portal";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", WebErrorMessages.thanThien("Không thể đăng nhập. Vui lòng kiểm tra lại thông tin.", ex));
            return "web/dang-nhap";
        }
    }

    @GetMapping("/register")
    public String hienThiDangKy(Model model) {
        if (!model.containsAttribute("DangKyWebForm")) {
            model.addAttribute("DangKyWebForm", new DangKyWebForm());
        }
        return "web/dang-ky";
    }

    @PostMapping("/register")
    public String dangKy(@Valid @ModelAttribute("DangKyWebForm") DangKyWebForm DangKyWebForm,
                           BindingResult bindingResult,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "web/dang-ky";
        }

        try {
            DangKyChoXacThuc DangKyChoXacThuc = xacThucWebService.yeuCauOtpDangKy(DangKyWebForm);
            session.setAttribute("DangKyChoXacThuc", DangKyChoXacThuc);
            redirectAttributes.addFlashAttribute("success", "Mã OTP đã được gửi tới email của bạn.");
            return "redirect:/register/verify";
        } catch (RuntimeException ex) {
            model.addAttribute("error", WebErrorMessages.thanThien(
                    "Không thể gửi mã OTP lúc này. Vui lòng thử lại sau.", ex));
            return "web/dang-ky";
        }
    }

    @GetMapping("/register/verify")
    public String hienThiXacThucOtp(HttpSession session, Model model) {
        DangKyChoXacThuc DangKyChoXacThuc =
                (DangKyChoXacThuc) session.getAttribute("DangKyChoXacThuc");
        if (DangKyChoXacThuc == null) {
            return "redirect:/register";
        }
        if (!model.containsAttribute("MaOtpForm")) {
            model.addAttribute("MaOtpForm", new MaOtpForm());
        }
        model.addAttribute("email", DangKyChoXacThuc.getEmail());
        return "web/xac-thuc-otp";
    }

    @PostMapping("/register/verify")
    public String xacThucOtp(@Valid @ModelAttribute("MaOtpForm") MaOtpForm MaOtpForm,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        DangKyChoXacThuc DangKyChoXacThuc =
                (DangKyChoXacThuc) session.getAttribute("DangKyChoXacThuc");
        if (DangKyChoXacThuc == null) {
            return "redirect:/register";
        }

        model.addAttribute("email", DangKyChoXacThuc.getEmail());
        if (bindingResult.hasErrors()) {
            return "web/xac-thuc-otp";
        }

        try {
            xacThucWebService.hoanTatDangKy(DangKyChoXacThuc, MaOtpForm.getOtp());
            session.removeAttribute("DangKyChoXacThuc");
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công. Bạn có thể đăng nhập ngay.");
            return "redirect:/dangNhap";
        } catch (RuntimeException ex) {
            model.addAttribute("error", WebErrorMessages.thanThien(
                    "Không tạo được tài khoản. Vui lòng kiểm tra thông tin và thử lại.", ex));
            return "web/xac-thuc-otp";
        }
    }

    @PostMapping("/dangXuat")
    public String dangXuat(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
