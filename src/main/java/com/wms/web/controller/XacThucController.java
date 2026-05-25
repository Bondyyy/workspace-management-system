package com.wms.web.controller;

import com.wms.web.form.DangNhapWebForm;
import com.wms.web.form.DatLaiMatKhauWebForm;
import com.wms.web.form.MaOtpForm;
import com.wms.web.form.DangKyWebForm;
import com.wms.web.form.QuenMatKhauWebForm;
import com.wms.web.model.DangKyChoXacThuc;
import com.wms.web.model.NguoiDungPhien;
import com.wms.web.model.YeuCauDatLaiMatKhau;
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
        } catch (RuntimeException ex) {
            model.addAttribute("error", WebErrorMessages.dangNhap(ex));
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

    @GetMapping("/dangKy")
    public String hienThiDangKyAlias() {
        return "redirect:/register";
    }

    @GetMapping("/quen-mat-khau")
    public String hienThiQuenMatKhau(Model model) {
        if (!model.containsAttribute("QuenMatKhauWebForm")) {
            model.addAttribute("QuenMatKhauWebForm", new QuenMatKhauWebForm());
        }
        return "web/quen-mat-khau";
    }

    @PostMapping("/quen-mat-khau/gui-otp")
    public String guiOtpQuenMatKhau(@Valid @ModelAttribute("QuenMatKhauWebForm") QuenMatKhauWebForm form,
                                    BindingResult bindingResult,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "web/quen-mat-khau";
        }

        try {
            YeuCauDatLaiMatKhau yeuCau = xacThucWebService.yeuCauOtpDatLaiMatKhau(form);
            session.setAttribute("YeuCauDatLaiMatKhau", yeuCau);
            redirectAttributes.addFlashAttribute("success", "Mã OTP đặt lại mật khẩu đã được gửi tới email của bạn.");
            return "redirect:/quen-mat-khau/dat-lai";
        } catch (RuntimeException ex) {
            model.addAttribute("error", WebErrorMessages.thanThien(
                    "Không thể gửi OTP đặt lại mật khẩu lúc này. Vui lòng thử lại sau.", ex));
            return "web/quen-mat-khau";
        }
    }

    @GetMapping("/quen-mat-khau/dat-lai")
    public String hienThiDatLaiMatKhau(HttpSession session, Model model) {
        YeuCauDatLaiMatKhau yeuCau =
                (YeuCauDatLaiMatKhau) session.getAttribute("YeuCauDatLaiMatKhau");
        if (yeuCau == null) {
            return "redirect:/quen-mat-khau";
        }
        if (!model.containsAttribute("DatLaiMatKhauWebForm")) {
            model.addAttribute("DatLaiMatKhauWebForm", new DatLaiMatKhauWebForm());
        }
        model.addAttribute("email", yeuCau.getEmail());
        return "web/dat-lai-mat-khau";
    }

    @PostMapping("/quen-mat-khau/dat-lai")
    public String datLaiMatKhau(@Valid @ModelAttribute("DatLaiMatKhauWebForm") DatLaiMatKhauWebForm form,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        YeuCauDatLaiMatKhau yeuCau =
                (YeuCauDatLaiMatKhau) session.getAttribute("YeuCauDatLaiMatKhau");
        if (yeuCau == null) {
            return "redirect:/quen-mat-khau";
        }

        model.addAttribute("email", yeuCau.getEmail());
        if (bindingResult.hasErrors()) {
            return "web/dat-lai-mat-khau";
        }

        try {
            xacThucWebService.datLaiMatKhau(yeuCau, form);
            session.removeAttribute("YeuCauDatLaiMatKhau");
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.");
            return "redirect:/dangNhap";
        } catch (RuntimeException ex) {
            model.addAttribute("error", WebErrorMessages.thanThien(
                    "Không thể đổi mật khẩu lúc này. Vui lòng kiểm tra thông tin và thử lại.", ex));
            return "web/dat-lai-mat-khau";
        }
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
