package com.wms.web.controller;

import com.wms.web.form.ThongTinTaiKhoanForm;
import com.wms.web.form.DatChoForm;
import com.wms.web.model.ThongTinTaiKhoanView;
import com.wms.web.model.ThanhToanDatChoView;
import com.wms.web.model.DatChoView;
import com.wms.web.model.ChiNhanhView;
import com.wms.web.model.NguoiDungPhien;
import com.wms.web.model.KhongGianView;
import com.wms.web.model.PhieuGiamGiaView;
import com.wms.web.service.CongThongTinService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
public class CongThongTinController {

    private final CongThongTinService congThongTinService;

    public CongThongTinController(CongThongTinService congThongTinService) {
        this.congThongTinService = congThongTinService;
    }

    @GetMapping("/portal")
    public String hienThiBangDieuKhien(HttpSession session, Model model) {
        NguoiDungPhien user = layHoiVienHienTai(session);
        if (user == null) {
            return "redirect:/dangNhap";
        }

        List<DatChoView> hienThiDatCho = layDanhSachDatChoHoiVien(user.getMaKH());
        List<KhongGianView> spaces = congThongTinService.layKhongGian(null);
        model.addAttribute("user", user);
        model.addAttribute("rankName", congThongTinService.layTenHangThanhVien(user));
        model.addAttribute("activePage", "trangChu");
        model.addAttribute("hienThiDatCho", hienThiDatCho);
        model.addAttribute("recentBookings", hienThiDatCho.stream().limit(6).toList());
        model.addAttribute("bookingCount", hienThiDatCho.size());
        model.addAttribute("totalHours", hienThiDatCho.stream()
                .map(DatChoView::getKhoangThoiGianSuDung)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .sum());
        model.addAttribute("totalSpent", hienThiDatCho.stream()
                .map(DatChoView::getThanhTien)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("featuredSpace", spaces.stream().findFirst().orElse(null));
        return "web/cong-thong-tin";
    }

    @GetMapping("/portal/branches")
    public String hienThiChiNhanh(HttpSession session, Model model) {
        NguoiDungPhien user = layHoiVienHienTai(session);
        if (user == null) {
            return "redirect:/dangNhap";
        }
        model.addAttribute("user", user);
        model.addAttribute("rankName", congThongTinService.layTenHangThanhVien(user));
        model.addAttribute("activePage", "booking");
        model.addAttribute("hienThiChiNhanh", congThongTinService.layChiNhanh());
        model.addAttribute("missingContactInfo", !congThongTinService.coThongTinLienHeDayDu(user));
        return "web/chi-nhanh";
    }

    @GetMapping("/portal/branches/{branchId}/spaces")
    public String hienThiKhongGianTheoChiNhanh(@PathVariable("branchId") String branchId,
                               @RequestParam(name = "date", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
                               @RequestParam(name = "start", required = false)
                               @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
                               @RequestParam(name = "end", required = false)
                               @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
                               @RequestParam(name = "canhBao", required = false) String canhBao,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        NguoiDungPhien user = layHoiVienHienTai(session);
        if (user == null) {
            return "redirect:/dangNhap";
        }
        ThongTinTaiKhoanView profile = congThongTinService.layThongTinTaiKhoan(user);
        if (profile == null || !profile.coThongTinLienHeDayDu()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ email và số điện thoại để đặt trước không gian.");
            return "redirect:/portal/branches";
        }
        ChiNhanhView branch = congThongTinService.layMotChiNhanh(branchId).orElse(null);
        if (branch == null) {
            return "redirect:/portal/branches";
        }

        return napModelSoDoKhongGian(user, profile, branch, bookingDate, startTime, endTime,
                null, doiMaCanhBaoDatCho(canhBao), model);
    }

    private String napModelSoDoKhongGian(NguoiDungPhien user,
                                         ThongTinTaiKhoanView profile,
                                         ChiNhanhView branch,
                                         LocalDate bookingDate,
                                         LocalTime startTime,
                                         LocalTime endTime,
                                         String thongBaoLoi,
                                         String canhBao,
                                         Model model) {
        var defaultWindow = congThongTinService.khungGioDatChoMacDinh(branch);
        List<String> layLuaChonGio = congThongTinService.layLuaChonGio(branch);
        LocalTime latestEnd = layLuaChonGio.isEmpty()
                ? defaultWindow.endTime()
                : LocalTime.parse(layLuaChonGio.get(layLuaChonGio.size() - 1));
        LocalDate selectedDate = bookingDate == null ? defaultWindow.date() : bookingDate;
        LocalTime selectedStart = startTime == null ? defaultWindow.startTime() : startTime;
        LocalTime selectedEnd = endTime == null ? defaultWindow.endTime() : endTime;
        if (selectedStart.plusHours(1).isAfter(latestEnd)) {
            selectedStart = latestEnd.minusHours(1);
            selectedEnd = latestEnd;
        }
        if (selectedEnd.isAfter(latestEnd)) {
            selectedEnd = latestEnd;
        }
        if (!selectedEnd.isAfter(selectedStart)) {
            thongBaoLoi = "Giờ kết thúc phải sau giờ bắt đầu.";
            selectedEnd = selectedStart.plusHours(1);
        }
        LocalDateTime selectedStartDateTime = LocalDateTime.of(selectedDate, selectedStart);
        LocalDateTime selectedEndDateTime = LocalDateTime.of(selectedDate, selectedEnd);
        if (thongBaoLoi == null && !congThongTinService.laThoiGianDatChoTrongTuongLai(selectedStartDateTime)) {
            thongBaoLoi = "Thời gian đặt chỗ không hợp lệ. Vui lòng chọn thời gian lớn hơn thời điểm hiện tại.";
        }

        List<KhongGianView> spaces = congThongTinService.layKhongGian(branch.getMaCN(), selectedStartDateTime, selectedEndDateTime);
        int maxSpaceColumn = spaces.stream()
                .mapToInt(space -> space.getToaDoX() + space.getChieuDai())
                .max()
                .orElse(3);
        int maxSpaceRow = spaces.stream()
                .mapToInt(space -> space.getToaDoY() + space.getChieuRong())
                .max()
                .orElse(2);
        int mapColumns = Math.min(12, Math.max(6, Math.max(3, maxSpaceColumn) + 3));
        int mapRows = Math.min(8, Math.max(4, maxSpaceRow + 1));

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("rankName", congThongTinService.layTenHangThanhVien(user));
        model.addAttribute("activePage", "booking");
        model.addAttribute("branch", branch);
        model.addAttribute("spaces", spaces);
        model.addAttribute("layLuaChonGio", layLuaChonGio);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("selectedStart", selectedStart);
        model.addAttribute("selectedEnd", selectedEnd);
        model.addAttribute("selectedDuration", Math.max(1, java.time.Duration.between(selectedStart, selectedEnd).toHours()));
        model.addAttribute("mapColumns", mapColumns);
        model.addAttribute("mapRows", mapRows);
        model.addAttribute("error", thongBaoLoi);
        model.addAttribute("canhBao", canhBao);
        return "web/so-do-khong-gian";
    }

    @GetMapping("/portal/checkout")
    public String hienThiXacNhanDatCho(@RequestParam("maKG") String maKG,
                           @RequestParam("arrivalTime") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime arrivalTime,
                           @RequestParam("durationHours") Integer durationHours,
                           @RequestParam(name = "voucherCode", required = false) String voucherCode,
                           @RequestParam(name = "note", required = false) String note,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        NguoiDungPhien user = layHoiVienHienTai(session);
        if (user == null) {
            return "redirect:/dangNhap";
        }
        ThongTinTaiKhoanView checkoutProfile = congThongTinService.layThongTinTaiKhoan(user);
        if (checkoutProfile == null || !checkoutProfile.coThongTinLienHeDayDu()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ email và số điện thoại để đặt trước không gian.");
            return "redirect:/portal/branches";
        }

        KhongGianView space = congThongTinService.layMotKhongGian(maKG);
        if (space == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn không gian và khung giờ hợp lệ.");
            return "redirect:/portal/branches";
        }
        if (durationHours == null || durationHours < 1) {
            return traVeSoDoKhongGianVoiLoi(user, checkoutProfile, space, arrivalTime, durationHours,
                    "Vui lòng chọn khung giờ hợp lệ.", model, redirectAttributes);
        }
        if (space.getTrangThaiKG() != null
                && java.text.Normalizer.normalize(space.getTrangThaiKG(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .contains("bao tri")) {
            return traVeSoDoKhongGianVoiLoi(user, checkoutProfile, space, arrivalTime, durationHours,
                    "Không gian này đang bảo trì.", model, redirectAttributes);
        }
        try {
            congThongTinService.kiemTraXacNhanDatCho(maKG, arrivalTime, durationHours);
        } catch (IllegalArgumentException ex) {
            return traVeSoDoKhongGianVoiLoi(user, checkoutProfile, space, arrivalTime, durationHours,
                    ex.getMessage(), model, redirectAttributes);
        }

        BigDecimal subtotal = congThongTinService.tinhTien(space, durationHours);
        String safeVoucherCode = voucherCode == null ? "" : voucherCode.trim();
        BigDecimal discount = BigDecimal.ZERO;
        String voucherMessage = "";
        if (!safeVoucherCode.isBlank()) {
            PhieuGiamGiaView voucher = congThongTinService.layPhieuGiamGiaHieuLucTheoMa(safeVoucherCode);
            if (voucher == null) {
                voucherMessage = "Mã ưu đãi không hợp lệ, hết hạn hoặc đã hết lượt sử dụng.";
            } else {
                BigDecimal minimum = voucher.getGiaTriApDungToiThieu() == null
                        ? BigDecimal.ZERO
                        : voucher.getGiaTriApDungToiThieu();
                if (subtotal.compareTo(minimum) < 0) {
                    voucherMessage = "Đơn này chưa đạt mức tối thiểu " + minimum.toPlainString() + " VNĐ để dùng mã ưu đãi.";
                } else {
                    discount = congThongTinService.tinhGiamGia(subtotal, safeVoucherCode);
                    voucherMessage = discount.compareTo(BigDecimal.ZERO) > 0
                            ? "Đã áp dụng mã ưu đãi " + safeVoucherCode + "."
                            : "Mã ưu đãi hợp lệ nhưng chưa làm giảm giá trị đơn này.";
                }
            }
        }
        model.addAttribute("user", user);
        model.addAttribute("profile", checkoutProfile);
        model.addAttribute("rankName", congThongTinService.layTenHangThanhVien(user));
        model.addAttribute("activePage", "booking");
        model.addAttribute("space", space);
        model.addAttribute("arrivalTime", arrivalTime);
        model.addAttribute("arrivalTimeValue", arrivalTime.toString());
        model.addAttribute("endTime", arrivalTime.plusHours(durationHours));
        model.addAttribute("durationHours", durationHours);
        model.addAttribute("note", note == null ? "" : note);
        model.addAttribute("voucherCode", safeVoucherCode);
        model.addAttribute("voucherMessage", voucherMessage);
        model.addAttribute("voucherApplied", discount.compareTo(BigDecimal.ZERO) > 0);
        model.addAttribute("vouchers", congThongTinService.layPhieuGiamGiaHieuLuc());
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        model.addAttribute("totalAmount", subtotal.subtract(discount));
        return "web/xac-nhan-dat-cho";
    }

    @PostMapping("/portal/bookings")
    public String taoDatCho(@Valid @ModelAttribute("DatChoForm") DatChoForm DatChoForm,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        NguoiDungPhien user = layHoiVienHienTai(session);
        if (user == null) {
            return "redirect:/dangNhap";
        }
        if (user.getMaKH() == null || user.getMaKH().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản này chưa có hồ sơ hội viên, nên chưa thể đặt chỗ.");
            return "redirect:/portal/branches";
        }
        if (!congThongTinService.coThongTinLienHeDayDu(user)) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ email và số điện thoại để đặt trước không gian.");
            return "redirect:/portal/branches";
        }
        if (bindingResult.hasErrors()) {
            KhongGianView space = congThongTinService.layMotKhongGian(DatChoForm.getMaKG());
            ThongTinTaiKhoanView profile = congThongTinService.layThongTinTaiKhoan(user);
            if (space != null) {
                return traVeSoDoKhongGianVoiLoi(user, profile, space, DatChoForm.getThoiGianDen(),
                        DatChoForm.getSoGioSuDung(), "Vui lòng kiểm tra lại thông tin đặt chỗ.", model, redirectAttributes);
            }
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin đặt chỗ.");
            return "redirect:/portal/branches";
        }

        try {
            String maDatCho = congThongTinService.taoDatCho(user, DatChoForm);
            redirectAttributes.addFlashAttribute("success", "Hệ thống đã giữ chỗ trong 10 phút. Vui lòng chuyển khoản đúng nội dung để tự xác nhận.");
            return "redirect:/portal/bookings/" + maDatCho + "/payment";
        } catch (IllegalArgumentException ex) {
            KhongGianView space = congThongTinService.layMotKhongGian(DatChoForm.getMaKG());
            ThongTinTaiKhoanView profile = congThongTinService.layThongTinTaiKhoan(user);
            if (space != null) {
                return traVeSoDoKhongGianVoiLoi(user, profile, space, DatChoForm.getThoiGianDen(),
                        DatChoForm.getSoGioSuDung(), ex.getMessage(), model, redirectAttributes);
            }
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/portal/branches";
        }
    }

    @GetMapping("/portal/bookings/{maDatCho}/payment")
    public String hienThiThanhToanDatCho(@PathVariable("maDatCho") String maDatCho,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        NguoiDungPhien user = layHoiVienHienTai(session);
        if (user == null) {
            return "redirect:/dangNhap";
        }
        ThanhToanDatChoView payment = congThongTinService.layThanhToanDatCho(user, maDatCho);
        if (payment == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy yêu cầu đặt chỗ này.");
            return "redirect:/portal/history";
        }
        model.addAttribute("user", user);
        model.addAttribute("rankName", congThongTinService.layTenHangThanhVien(user));
        model.addAttribute("activePage", "booking");
        model.addAttribute("payment", payment);
        return "web/thanh-toan";
    }

    @PostMapping("/portal/bookings/{maDatCho}/payment/mock-confirm")
    public String giaLapDaNhanChuyenKhoan(@PathVariable("maDatCho") String maDatCho,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        NguoiDungPhien user = layHoiVienHienTai(session);
        if (user == null) {
            return "redirect:/dangNhap";
        }
        CongThongTinService.KetQuaWebhookThanhToan result =
                congThongTinService.xacNhanThanhToanDemo(user, maDatCho);
        redirectAttributes.addFlashAttribute(result.success() ? "success" : "error", result.message());
        return result.success() ? "redirect:/portal/history" : "redirect:/portal/bookings/" + maDatCho + "/payment";
    }

    @GetMapping("/portal/benefits")
    public String hienThiUuDai(@RequestParam(name = "keyword", required = false) String keyword,
                           HttpSession session,
                           Model model) {
        String accessRedirect = redirectNeuKhongPhaiHoiVien(session);
        if (accessRedirect != null) {
            return accessRedirect;
        }
        NguoiDungPhien user = layHoiVienHienTai(session);
        model.addAttribute("user", user);
        model.addAttribute("rankName", congThongTinService.layTenHangThanhVien(user));
        model.addAttribute("activePage", "uuDai");
        var vouchers = congThongTinService.layPhieuGiamGiaHieuLuc();
        if (keyword != null && !keyword.isBlank()) {
            String normalizedKeyword = keyword.trim().toLowerCase();
            vouchers = vouchers.stream()
                    .filter(voucher -> voucher.getMaChuSoPGG() != null
                            && voucher.getMaChuSoPGG().toLowerCase().contains(normalizedKeyword))
                    .toList();
        }
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        model.addAttribute("expiringVoucherCount", vouchers.stream()
                .filter(voucher -> voucher.getNgayKetThucApDung() != null)
                .filter(voucher -> voucher.getNgayKetThucApDung().isBefore(LocalDateTime.now().plusDays(3)))
                .count());
        return "web/uu-dai";
    }

    @GetMapping("/portal/history")
    public String hienThiLichSu(HttpSession session, Model model) {
        String accessRedirect = redirectNeuKhongPhaiHoiVien(session);
        if (accessRedirect != null) {
            return accessRedirect;
        }
        NguoiDungPhien user = layHoiVienHienTai(session);
        model.addAttribute("user", user);
        model.addAttribute("rankName", congThongTinService.layTenHangThanhVien(user));
        model.addAttribute("activePage", "lichSu");
        model.addAttribute("histories", congThongTinService.layLichSuDatChoHoiVien(user.getMaKH()));
        return "web/lich-su";
    }

    @GetMapping(value = "/portal/history/bookings/{maDatCho}/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> layQrDatCho(@PathVariable("maDatCho") String maDatCho, HttpSession session) {
        NguoiDungPhien user = layHoiVienHienTai(session);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return congThongTinService.layAnhQrDatChoPng(user.getMaKH(), maDatCho)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate())
                        .body(bytes))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/portal/history/{maPhien}/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> layQrPhien(@PathVariable("maPhien") String maPhien, HttpSession session) {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/portal/account")
    public String hienThiTaiKhoan(HttpSession session, Model model) {
        String accessRedirect = redirectNeuKhongPhaiHoiVien(session);
        if (accessRedirect != null) {
            return accessRedirect;
        }
        NguoiDungPhien user = layHoiVienHienTai(session);
        List<DatChoView> hienThiDatCho = layDanhSachDatChoHoiVien(user.getMaKH());
        ThongTinTaiKhoanView profile = congThongTinService.layThongTinTaiKhoan(user);
        model.addAttribute("user", user);
        model.addAttribute("rankName", congThongTinService.layTenHangThanhVien(user));
        model.addAttribute("profile", profile);
        model.addAttribute("activePage", "taiKhoan");
        model.addAttribute("joinDate", LocalDate.now());
        model.addAttribute("bookingCount", hienThiDatCho.size());
        model.addAttribute("lastBooking", hienThiDatCho.stream()
                .filter(booking -> booking.getThoiGianDuKienToi() != null)
                .max(Comparator.comparing(DatChoView::getThoiGianDuKienToi))
                .orElse(null));
        return "web/tai-khoan";
    }

    @PostMapping("/portal/account")
    public String capNhatTaiKhoan(@ModelAttribute ThongTinTaiKhoanForm form,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        NguoiDungPhien user = layHoiVienHienTai(session);
        if (user == null) {
            return "redirect:/dangNhap";
        }

        try {
            ThongTinTaiKhoanView profile = congThongTinService.capNhatThongTinTaiKhoan(user, form);
            if (profile != null) {
                session.setAttribute("user", new NguoiDungPhien(
                        user.getMaND(),
                        user.getMaKH(),
                        profile.getHoTen(),
                        user.getTenTaiKhoan(),
                        user.laNhanVien()
                ));
            }
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin tài khoản thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật thông tin. Email hoặc số điện thoại có thể đã được sử dụng.");
        }
        return "redirect:/portal/account";
    }

    private NguoiDungPhien layHoiVienHienTai(HttpSession session) {
        NguoiDungPhien user = (NguoiDungPhien) session.getAttribute("user");
        if (user == null) {
            return null;
        }
        if (user.laNhanVien()) {
            return null;
        }
        return user;
    }

    private String redirectNeuKhongPhaiHoiVien(HttpSession session) {
        NguoiDungPhien user = (NguoiDungPhien) session.getAttribute("user");
        if (user == null) {
            return "redirect:/dangNhap";
        }
        if (user.laNhanVien()) {
            return "redirect:/staff/bookings";
        }
        if (user.getMaKH() == null || user.getMaKH().isBlank()) {
            return "redirect:/dangNhap";
        }
        return null;
    }

    private List<DatChoView> layDanhSachDatChoHoiVien(String maKH) {
        if (maKH == null || maKH.isBlank()) {
            return List.of();
        }
        return congThongTinService.layDatChoTheoHoiVien(maKH);
    }

    private String traVeSoDoKhongGianVoiLoi(NguoiDungPhien user,
                                            ThongTinTaiKhoanView profile,
                                            KhongGianView space,
                                            LocalDateTime arrivalTime,
                                            Integer durationHours,
                                            String thongBaoLoi,
                                            Model model,
                                            RedirectAttributes redirectAttributes) {
        if (space == null || space.getMaCN() == null || space.getMaCN().isBlank()) {
            redirectAttributes.addFlashAttribute("error", thongBaoLoi);
            return "redirect:/portal/branches";
        }
        ChiNhanhView branch = congThongTinService.layMotChiNhanh(space.getMaCN()).orElse(null);
        if (branch == null) {
            redirectAttributes.addFlashAttribute("error", thongBaoLoi);
            return "redirect:/portal/branches";
        }
        LocalDate ngayDat = arrivalTime == null ? null : arrivalTime.toLocalDate();
        LocalTime gioBatDau = arrivalTime == null ? null : arrivalTime.toLocalTime();
        LocalTime gioKetThuc = arrivalTime == null || durationHours == null || durationHours < 1
                ? null
                : arrivalTime.plusHours(durationHours).toLocalTime();
        return napModelSoDoKhongGian(user, profile, branch, ngayDat, gioBatDau, gioKetThuc,
                thongBaoLoi, null, model);
    }

    private String doiMaCanhBaoDatCho(String canhBao) {
        if ("chon-lai-khong-gian".equals(canhBao)) {
            return "Bạn đã thay đổi thời gian. Vui lòng chọn lại không gian phù hợp với khung giờ mới.";
        }
        return null;
    }
}
