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
import com.wms.web.util.WebErrorMessages;
import com.wms.util.BusinessHoursUtil;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    @GetMapping("/chi-nhanh")
    public String hienThiChiNhanhCongKhai(HttpSession session, Model model) {
        NguoiDungPhien user = (NguoiDungPhien) session.getAttribute("user");
        model.addAttribute("user", user);
        model.addAttribute("rankName", user == null || user.laNhanVien() ? "" : congThongTinService.layTenHangThanhVien(user));
        model.addAttribute("activePage", "booking");
        model.addAttribute("hienThiChiNhanh", congThongTinService.layChiNhanh());
        model.addAttribute("missingContactInfo", false);
        model.addAttribute("guestMode", user == null);
        return "web/chi-nhanh";
    }

    @GetMapping("/chi-nhanh/{branchId}")
    public String chuyenDenSoDoCongKhai(@PathVariable("branchId") String branchId) {
        return "redirect:/so-do-khong-gian?maCN=" + branchId;
    }

    @GetMapping("/so-do-khong-gian")
    public String hienThiSoDoCongKhai(@RequestParam(name = "maCN", required = false) String maCN,
                                      @RequestParam(name = "branchId", required = false) String branchId,
                                      @RequestParam(name = "date", required = false) String bookingDateText,
                                      @RequestParam(name = "start", required = false) String startTimeText,
                                      @RequestParam(name = "end", required = false) String endTimeText,
                                      HttpSession session,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        List<ChiNhanhView> danhSachChiNhanh = layChiNhanhAnToan(model);
        String selectedMaCN = chonMaCN(maCN, branchId, danhSachChiNhanh);
        ChiNhanhView branch = timChiNhanhTheoMa(danhSachChiNhanh, selectedMaCN);
        if (danhSachChiNhanh.isEmpty()) {
            NguoiDungPhien user = (NguoiDungPhien) session.getAttribute("user");
            return napModelSoDoKhongGianRong(user, danhSachChiNhanh, selectedMaCN,
                    "Hiện chưa có chi nhánh hoạt động để hiển thị.", model);
        }
        if (branch == null) {
            NguoiDungPhien user = (NguoiDungPhien) session.getAttribute("user");
            return napModelSoDoKhongGianRong(user, danhSachChiNhanh, selectedMaCN,
                    "Chi nhánh không tồn tại hoặc đã ngừng hoạt động.", model);
        }
        LocalDate bookingDate = null;
        LocalTime startTime = null;
        LocalTime endTime = null;
        String loiNgayGio = null;
        try {
            bookingDate = parseWebDate(bookingDateText, "Ngày đặt chỗ");
            startTime = parseWebTime(startTimeText, "Từ giờ");
            endTime = parseWebTime(endTimeText, "Đến giờ");
        } catch (IllegalArgumentException ex) {
            loiNgayGio = ex.getMessage();
        }
        NguoiDungPhien user = (NguoiDungPhien) session.getAttribute("user");
        ThongTinTaiKhoanView profile = user == null || user.laNhanVien()
                ? null
                : congThongTinService.layThongTinTaiKhoan(user);
        return napModelSoDoKhongGian(user, profile, branch, bookingDate, startTime, endTime,
                loiNgayGio, null, danhSachChiNhanh, model);
    }

    @GetMapping("/portal/branches/{branchId}/spaces")
    public String hienThiKhongGianTheoChiNhanh(@PathVariable("branchId") String branchId,
                               @RequestParam(name = "date", required = false) String bookingDateText,
                               @RequestParam(name = "start", required = false) String startTimeText,
                               @RequestParam(name = "end", required = false) String endTimeText,
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

        LocalDate bookingDate = null;
        LocalTime startTime = null;
        LocalTime endTime = null;
        String loiNgayGio = null;
        try {
            bookingDate = parseWebDate(bookingDateText, "Ngày đặt chỗ");
            startTime = parseWebTime(startTimeText, "Từ giờ");
            endTime = parseWebTime(endTimeText, "Đến giờ");
        } catch (IllegalArgumentException ex) {
            loiNgayGio = ex.getMessage();
        }

        return napModelSoDoKhongGian(user, profile, branch, bookingDate, startTime, endTime,
                loiNgayGio, doiMaCanhBaoDatCho(canhBao), layChiNhanhAnToan(model), model);
    }

    private List<ChiNhanhView> layChiNhanhAnToan(Model model) {
        try {
            return congThongTinService.layChiNhanh();
        } catch (IllegalStateException ex) {
            System.err.println("[Web] Khong the tai danh sach chi nhanh: " + ex.getMessage());
            if (model != null) {
                model.addAttribute("error", WebErrorMessages.thanThien(
                        "Không thể tải danh sách chi nhánh lúc này. Vui lòng thử lại sau.", ex));
            }
            return List.of();
        }
    }

    private String chonMaCN(String maCN, String branchId, List<ChiNhanhView> danhSachChiNhanh) {
        String selectedMaCN = maCN == null || maCN.isBlank() ? branchId : maCN;
        if (selectedMaCN != null && !selectedMaCN.isBlank()) {
            return selectedMaCN.trim();
        }
        return danhSachChiNhanh == null || danhSachChiNhanh.isEmpty()
                ? null
                : danhSachChiNhanh.get(0).getMaCN();
    }

    private ChiNhanhView timChiNhanhTheoMa(List<ChiNhanhView> danhSachChiNhanh, String maCN) {
        if (danhSachChiNhanh == null || maCN == null || maCN.isBlank()) {
            return null;
        }
        return danhSachChiNhanh.stream()
                .filter(branch -> branch.getMaCN() != null && branch.getMaCN().equalsIgnoreCase(maCN.trim()))
                .findFirst()
                .orElse(null);
    }

    private String napModelSoDoKhongGianRong(NguoiDungPhien user,
                                             List<ChiNhanhView> danhSachChiNhanh,
                                             String selectedMaCN,
                                             String thongBaoLoi,
                                             Model model) {
        List<ChiNhanhView> safeBranches = danhSachChiNhanh == null ? List.of() : danhSachChiNhanh;
        model.addAttribute("user", user);
        model.addAttribute("daDangNhap", user != null && !user.laNhanVien());
        model.addAttribute("profile", null);
        model.addAttribute("rankName", user == null || user.laNhanVien() ? "" : congThongTinService.layTenHangThanhVien(user));
        model.addAttribute("activePage", "booking");
        model.addAttribute("guestMode", user == null);
        model.addAttribute("danhSachChiNhanh", safeBranches);
        model.addAttribute("hienThiChiNhanh", safeBranches);
        model.addAttribute("branch", null);
        model.addAttribute("chiNhanhDangChon", null);
        model.addAttribute("selectedBranch", null);
        model.addAttribute("selectedMaCN", selectedMaCN == null ? "" : selectedMaCN);
        model.addAttribute("spaces", List.of());
        model.addAttribute("danhSachKhongGian", List.of());
        model.addAttribute("khongGianList", List.of());
        model.addAttribute("dungGridFallback", true);
        model.addAttribute("selectedDate", LocalDate.now());
        model.addAttribute("selectedStart", LocalTime.of(7, 0));
        model.addAttribute("selectedEnd", LocalTime.of(9, 0));
        model.addAttribute("selectedDuration", 2);
        model.addAttribute("overnightBranch", false);
        model.addAttribute("twentyFourHoursBranch", false);
        model.addAttribute("mapColumns", 12);
        model.addAttribute("mapRows", 8);
        model.addAttribute("error", thongBaoLoi);
        model.addAttribute("thongBaoLoi", thongBaoLoi);
        model.addAttribute("canhBao", null);
        return "web/so-do-khong-gian";
    }

    private LocalTime docGioChiNhanh(String value, LocalTime fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim();
        if (normalized.length() > 5) {
            normalized = normalized.substring(0, 5);
        }
        try {
            return BusinessHoursUtil.parseBranchTime(normalized, fallback);
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private String napModelSoDoKhongGian(NguoiDungPhien user,
                                         ThongTinTaiKhoanView profile,
                                         ChiNhanhView branch,
                                         LocalDate bookingDate,
                                         LocalTime startTime,
                                         LocalTime endTime,
                                         String thongBaoLoi,
                                         String canhBao,
                                         List<ChiNhanhView> danhSachChiNhanh,
                                         Model model) {
        var defaultWindow = congThongTinService.khungGioDatChoMacDinh(branch);
        
        LocalTime openTime = docGioChiNhanh(branch.getThoiGianMoCua(), LocalTime.of(7, 0));
        LocalTime closeTime = docGioChiNhanh(branch.getThoiGianDongCua(), LocalTime.of(22, 0));

        LocalDate selectedDate = bookingDate == null ? defaultWindow.date() : bookingDate;
        LocalTime selectedStart = startTime == null ? defaultWindow.startTime() : startTime;
        LocalTime selectedEnd = endTime == null ? defaultWindow.endTime() : endTime;

        LocalDateTime selectedStartDateTime = LocalDateTime.of(selectedDate, selectedStart);
        LocalDateTime selectedEndDateTime = BusinessHoursUtil.resolveEnd(selectedDate, selectedStart, selectedEnd);

        String openStr = BusinessHoursUtil.format(openTime);
        String closeStr = BusinessHoursUtil.format(closeTime);
        if (!BusinessHoursUtil.fitsInBranchHours(selectedStartDateTime, selectedEndDateTime, openTime, closeTime)) {
            thongBaoLoi = "Khung giờ đặt chỗ phải nằm trong giờ hoạt động của chi nhánh: " + openStr + " - " + closeStr + ".";
            selectedStart = defaultWindow.startTime();
            selectedEnd = defaultWindow.endTime();
            selectedDate = defaultWindow.date();
            selectedStartDateTime = LocalDateTime.of(selectedDate, selectedStart);
            selectedEndDateTime = BusinessHoursUtil.resolveEnd(selectedDate, selectedStart, selectedEnd);
        }
        if (thongBaoLoi == null && !congThongTinService.laThoiGianDatChoTrongTuongLai(selectedStartDateTime)) {
            thongBaoLoi = "Thời gian đặt chỗ không hợp lệ. Vui lòng chọn thời gian lớn hơn thời điểm hiện tại.";
        }

        List<String> layLuaChonGioBatDau = BusinessHoursUtil.hourlyOptions(openTime, closeTime, false);
        List<String> layLuaChonGioKetThuc = BusinessHoursUtil.hourlyOptions(openTime, closeTime, true);

        List<KhongGianView> spaces = List.of();
        try {
            spaces = congThongTinService.layKhongGian(branch.getMaCN(), selectedStartDateTime, selectedEndDateTime);
        } catch (IllegalStateException ex) {
            System.err.println("[Web] Khong the tai so do maCN=" + branch.getMaCN() + ": " + ex.getMessage());
            thongBaoLoi = WebErrorMessages.thanThien("Không thể tải sơ đồ không gian lúc này. Vui lòng thử lại sau.", ex);
        }
        boolean dungGridFallback = !spaces.isEmpty()
                && spaces.stream().allMatch(space -> space.getToaDoX() == 0 && space.getToaDoY() == 0);
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
        model.addAttribute("daDangNhap", user != null && !user.laNhanVien());
        model.addAttribute("profile", profile);
        model.addAttribute("rankName", user == null || user.laNhanVien() ? "" : congThongTinService.layTenHangThanhVien(user));
        model.addAttribute("activePage", "booking");
        model.addAttribute("guestMode", user == null);
        model.addAttribute("danhSachChiNhanh", danhSachChiNhanh == null ? List.of() : danhSachChiNhanh);
        model.addAttribute("hienThiChiNhanh", danhSachChiNhanh == null ? List.of() : danhSachChiNhanh);
        model.addAttribute("branch", branch);
        model.addAttribute("chiNhanhDangChon", branch);
        model.addAttribute("selectedBranch", branch);
        model.addAttribute("selectedMaCN", branch.getMaCN());
        model.addAttribute("spaces", spaces);
        model.addAttribute("danhSachKhongGian", spaces);
        model.addAttribute("khongGianList", spaces);
        model.addAttribute("dungGridFallback", dungGridFallback);
        model.addAttribute("layLuaChonGioBatDau", layLuaChonGioBatDau);
        model.addAttribute("layLuaChonGioKetThuc", layLuaChonGioKetThuc);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("selectedStart", selectedStart);
        model.addAttribute("selectedEnd", selectedEnd);
        model.addAttribute("selectedDuration", Math.max(1, java.time.Duration.between(selectedStartDateTime, selectedEndDateTime).toHours()));
        model.addAttribute("overnightBranch", BusinessHoursUtil.isOvernight(openTime, closeTime));
        model.addAttribute("twentyFourHoursBranch", BusinessHoursUtil.isTwentyFourHours(openTime, closeTime));
        model.addAttribute("mapColumns", mapColumns);
        model.addAttribute("mapRows", mapRows);
        model.addAttribute("error", thongBaoLoi);
        model.addAttribute("thongBaoLoi", thongBaoLoi);
        model.addAttribute("canhBao", canhBao);
        return "web/so-do-khong-gian";
    }

    @GetMapping("/portal/checkout")
    public String hienThiXacNhanDatCho(@RequestParam("maKG") String maKG,
                           @RequestParam("arrivalTime") String arrivalTimeText,
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
        LocalDateTime arrivalTime;
        try {
            arrivalTime = parseWebDateTimeRequired(arrivalTimeText, "Thời gian đặt chỗ");
        } catch (IllegalArgumentException ex) {
            return traVeSoDoKhongGianVoiLoi(user, checkoutProfile, space, null, durationHours,
                    ex.getMessage(), model, redirectAttributes);
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
                    WebErrorMessages.thanThien("Không thể kiểm tra không gian lúc này. Vui lòng thử lại.", ex),
                    model, redirectAttributes);
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
            String thongBaoBinding = thongBaoLoiBindingDatCho(bindingResult);
            KhongGianView space = congThongTinService.layMotKhongGian(DatChoForm.getMaKG());
            ThongTinTaiKhoanView profile = congThongTinService.layThongTinTaiKhoan(user);
            if (space != null) {
                return traVeSoDoKhongGianVoiLoi(user, profile, space, DatChoForm.getThoiGianDen(),
                        DatChoForm.getSoGioSuDung(), thongBaoBinding, model, redirectAttributes);
            }
            redirectAttributes.addFlashAttribute("error", thongBaoBinding);
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
                        DatChoForm.getSoGioSuDung(),
                        WebErrorMessages.thanThien("Không thể tạo đặt chỗ lúc này. Vui lòng kiểm tra lại thông tin.", ex),
                        model, redirectAttributes);
            }
            redirectAttributes.addFlashAttribute("error", WebErrorMessages.thanThien(
                    "Không thể tạo đặt chỗ lúc này. Vui lòng kiểm tra lại thông tin.", ex));
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

    @GetMapping("/portal/account/avatar")
    public ResponseEntity<byte[]> layAnhDaiDien(HttpSession session) {
        NguoiDungPhien user = layHoiVienHienTai(session);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return congThongTinService.layAnhDaiDien(user)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(xacDinhLoaiAnh(bytes))
                        .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePrivate())
                        .body(bytes))
                .orElseGet(() -> ResponseEntity.notFound().build());
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
                                BindingResult bindingResult,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        NguoiDungPhien user = layHoiVienHienTai(session);
        if (user == null) {
            return "redirect:/dangNhap";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ngày sinh không đúng định dạng. Vui lòng chọn bằng lịch.");
            return "redirect:/portal/account";
        }

        try {
            ThongTinTaiKhoanView profile = congThongTinService.capNhatThongTinTaiKhoan(user, form);
            if (profile != null) {
                session.setAttribute("user", new NguoiDungPhien(
                        user.getMaND(),
                        user.getMaKH(),
                        user.getMaNV(),
                        user.getMaCN(),
                        user.getTenCN(),
                        profile.getHoTen(),
                        user.getTenTaiKhoan(),
                        user.laNhanVien()
                ));
            }
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin tài khoản thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", WebErrorMessages.thanThien(
                    "Không thể cập nhật thông tin. Vui lòng kiểm tra lại dữ liệu.", ex));
        } catch (RuntimeException ex) {
            System.err.println("[Web] Cap nhat tai khoan loi: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật thông tin. Email hoặc số điện thoại có thể đã được sử dụng.");
        }
        return "redirect:/portal/account";
    }

    @PostMapping("/portal/account/change-password")
    public String doiMatKhau(@RequestParam("currentPassword") String currentPassword,
                             @RequestParam("newPassword") String newPassword,
                             @RequestParam("confirmNewPassword") String confirmNewPassword,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        NguoiDungPhien user = layHoiVienHienTai(session);
        if (user == null) {
            return "redirect:/dangNhap";
        }
        try {
            congThongTinService.doiMatKhau(user, currentPassword, newPassword, confirmNewPassword);
            redirectAttributes.addFlashAttribute("successPassword", "Đổi mật khẩu thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorPassword", WebErrorMessages.thanThien(
                    "Không thể đổi mật khẩu. Vui lòng kiểm tra lại thông tin.", ex));
        } catch (RuntimeException ex) {
            System.err.println("[Web] Doi mat khau loi: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("errorPassword", "Không thể đổi mật khẩu lúc này. Vui lòng thử lại sau.");
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
                thongBaoLoi, null, layChiNhanhAnToan(model), model);
    }

    private String doiMaCanhBaoDatCho(String canhBao) {
        if ("chon-lai-khong-gian".equals(canhBao)) {
            return "Bạn đã thay đổi thời gian. Vui lòng chọn lại không gian phù hợp với khung giờ mới.";
        }
        return null;
    }

    private LocalDate parseWebDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " không đúng định dạng. Vui lòng chọn bằng lịch.");
        }
    }

    private LocalTime parseWebTime(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return com.wms.util.DateInputUtil.parseTime(value.trim(), fieldName);
    }

    private LocalDateTime parseWebDateTimeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn thời gian đặt chỗ.");
        }
        try {
            return LocalDateTime.parse(value.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " không đúng định dạng. Vui lòng chọn lại ngày giờ.");
        }
    }

    private String thongBaoLoiBindingDatCho(BindingResult bindingResult) {
        if (bindingResult.getFieldError("arrivalTime") != null) {
            return "Thời gian đặt chỗ không đúng định dạng. Vui lòng chọn lại ngày giờ.";
        }
        if (bindingResult.getFieldError("durationHours") != null) {
            return "Vui lòng chọn khung giờ hợp lệ.";
        }
        return "Vui lòng kiểm tra lại thông tin đặt chỗ.";
    }

    private MediaType xacDinhLoaiAnh(byte[] bytes) {
        if (bytes != null && bytes.length >= 12) {
            if ((bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47) {
                return MediaType.IMAGE_PNG;
            }
            if ((bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8) {
                return MediaType.IMAGE_JPEG;
            }
            if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                    && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
                return MediaType.parseMediaType("image/webp");
            }
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
