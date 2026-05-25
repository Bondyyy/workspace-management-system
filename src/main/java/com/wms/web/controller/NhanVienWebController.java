package com.wms.web.controller;

import com.wms.web.form.YeuCauNhanChoBangQR;
import com.wms.web.model.KetQuaNhanChoBangQRView;
import com.wms.web.model.NguoiDungPhien;
import com.wms.web.service.CongThongTinService;
import com.wms.web.util.WebErrorMessages;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class NhanVienWebController {

    private final CongThongTinService congThongTinService;

    public NhanVienWebController(CongThongTinService congThongTinService) {
        this.congThongTinService = congThongTinService;
    }

    @GetMapping("/staff/bookings")
    public String hienThiDatCho(HttpSession session, Model model) {
        NguoiDungPhien user = layNhanVienHienTai(session);
        model.addAttribute("user", user);
        model.addAttribute("hienThiDatCho", congThongTinService.layTatCaDatCho());
        return "web/nhan-vien-dat-cho";
    }

    @GetMapping("/staff/bookings/scan")
    public String hienThiTrangQuetMaQR(HttpSession session, Model model) {
        NguoiDungPhien user = layNhanVienHienTai(session);
        model.addAttribute("user", user);
        return "web/nhan-vien-quet-qr";
    }

    @PostMapping("/staff/bookings/{id}/xacNhan")
    public String xacNhan(@PathVariable("id") String bookingId, RedirectAttributes redirectAttributes) {
        try {
            congThongTinService.xacNhanDatChoDaThanhToan(bookingId);
            redirectAttributes.addFlashAttribute("success", "Đã xác nhận yêu cầu đặt chỗ.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", WebErrorMessages.thanThien(
                    "Không thể xác nhận đặt chỗ lúc này. Vui lòng thử lại.", ex));
        }
        return "redirect:/staff/bookings";
    }

    @PostMapping("/staff/bookings/{id}/danhDauDaSuDung")
    public String danhDauDaSuDung(@PathVariable("id") String bookingId, RedirectAttributes redirectAttributes) {
        try {
            congThongTinService.danhDauDatChoDaSuDung(bookingId);
            redirectAttributes.addFlashAttribute("success", "Phiên đặt chỗ đã được đánh dấu đã sử dụng.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", WebErrorMessages.thanThien(
                    "Không thể cập nhật trạng thái đặt chỗ lúc này. Vui lòng thử lại.", ex));
        }
        return "redirect:/staff/bookings";
    }

    @PostMapping("/staff/api/bookings/nhan-cho-bang-qr")
    @ResponseBody
    public ResponseEntity<KetQuaNhanChoBangQRView> nhanChoBangMaQR(@RequestBody(required = false) YeuCauNhanChoBangQR yeuCau,
                                                                   HttpSession session) {
        NguoiDungPhien user = layNhanVienHienTai(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(KetQuaNhanChoBangQRView.thatBai("Phiên đăng nhập nhân viên đã hết hạn."));
        }
        if (!user.laNhanVien()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(KetQuaNhanChoBangQRView.thatBai("Tài khoản không có quyền nhận chỗ."));
        }

        String noiDungQR = yeuCau == null ? null : yeuCau.getNoiDungQR();
        KetQuaNhanChoBangQRView ketQua = congThongTinService.nhanChoBangMaQR(noiDungQR, user);
        return ResponseEntity.ok(ketQua);
    }

    private NguoiDungPhien layNhanVienHienTai(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (NguoiDungPhien) session.getAttribute("user");
    }
}
