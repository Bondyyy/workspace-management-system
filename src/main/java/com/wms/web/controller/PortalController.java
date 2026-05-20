package com.wms.web.controller;

import com.wms.web.form.AccountProfileForm;
import com.wms.web.form.BookingForm;
import com.wms.web.model.AccountProfileView;
import com.wms.web.model.BookingView;
import com.wms.web.model.BranchView;
import com.wms.web.model.SessionUser;
import com.wms.web.model.SpaceView;
import com.wms.web.service.PortalService;
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
public class PortalController {

    private final PortalService portalService;

    public PortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/portal")
    public String dashboard(HttpSession session, Model model) {
        SessionUser user = currentMember(session);
        if (user == null) {
            return "redirect:/login";
        }

        List<BookingView> bookings = memberBookings(user.getMaKH());
        List<SpaceView> spaces = portalService.getSpaces(null);
        model.addAttribute("user", user);
        model.addAttribute("rankName", portalService.getMemberRankName(user));
        model.addAttribute("activePage", "home");
        model.addAttribute("bookings", bookings);
        model.addAttribute("recentBookings", bookings.stream().limit(6).toList());
        model.addAttribute("bookingCount", bookings.size());
        model.addAttribute("totalHours", bookings.stream()
                .map(BookingView::getKhoangThoiGianSuDung)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .sum());
        model.addAttribute("totalSpent", bookings.stream()
                .map(BookingView::getThanhTien)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("featuredSpace", spaces.stream().findFirst().orElse(null));
        return "web/portal";
    }

    @GetMapping("/portal/branches")
    public String branches(HttpSession session, Model model) {
        SessionUser user = currentMember(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("rankName", portalService.getMemberRankName(user));
        model.addAttribute("activePage", "booking");
        model.addAttribute("branches", portalService.getBranches());
        model.addAttribute("missingContactInfo", !portalService.hasCompleteContactInfo(user));
        return "web/branches";
    }

    @GetMapping("/portal/branches/{branchId}/spaces")
    public String branchSpaces(@PathVariable("branchId") String branchId,
                               @RequestParam(name = "date", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
                               @RequestParam(name = "start", required = false)
                               @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
                               @RequestParam(name = "end", required = false)
                               @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        SessionUser user = currentMember(session);
        if (user == null) {
            return "redirect:/login";
        }
        if (!portalService.hasCompleteContactInfo(user)) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ email và số điện thoại để đặt trước không gian.");
            return "redirect:/portal/branches";
        }
        BranchView branch = portalService.getBranch(branchId).orElse(null);
        if (branch == null) {
            return "redirect:/portal/branches";
        }

        var defaultWindow = portalService.defaultBookingWindow(branch);
        List<String> timeOptions = portalService.timeOptions(branch);
        LocalTime latestEnd = timeOptions.isEmpty()
                ? defaultWindow.endTime()
                : LocalTime.parse(timeOptions.get(timeOptions.size() - 1));
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
            selectedEnd = selectedStart.plusHours(1);
        }
        LocalDateTime selectedStartDateTime = LocalDateTime.of(selectedDate, selectedStart);
        LocalDateTime selectedEndDateTime = LocalDateTime.of(selectedDate, selectedEnd);

        List<SpaceView> spaces = portalService.getSpaces(branchId, selectedStartDateTime, selectedEndDateTime);
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
        model.addAttribute("rankName", portalService.getMemberRankName(user));
        model.addAttribute("activePage", "booking");
        model.addAttribute("branch", branch);
        model.addAttribute("spaces", spaces);
        model.addAttribute("timeOptions", timeOptions);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("selectedStart", selectedStart);
        model.addAttribute("selectedEnd", selectedEnd);
        model.addAttribute("selectedDuration", Math.max(1, java.time.Duration.between(selectedStart, selectedEnd).toHours()));
        model.addAttribute("mapColumns", mapColumns);
        model.addAttribute("mapRows", mapRows);
        return "web/space-map";
    }

    @GetMapping("/portal/checkout")
    public String checkout(@RequestParam("maKG") String maKG,
                           @RequestParam("arrivalTime") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime arrivalTime,
                           @RequestParam("durationHours") Integer durationHours,
                           @RequestParam(name = "voucherCode", required = false) String voucherCode,
                           @RequestParam(name = "note", required = false) String note,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        SessionUser user = currentMember(session);
        if (user == null) {
            return "redirect:/login";
        }
        if (!portalService.hasCompleteContactInfo(user)) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ email và số điện thoại để đặt trước không gian.");
            return "redirect:/portal/branches";
        }

        SpaceView space = portalService.getSpace(maKG);
        if (space == null || durationHours == null || durationHours < 1) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn không gian và khung giờ hợp lệ.");
            return "redirect:/portal/branches";
        }
        if (space.getTrangThaiKG() != null
                && java.text.Normalizer.normalize(space.getTrangThaiKG(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .contains("bao tri")) {
            redirectAttributes.addFlashAttribute("error", "Khong gian nay dang bao tri.");
            return "redirect:/portal/branches";
        }
        try {
            portalService.validateCheckout(maKG, arrivalTime, durationHours);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/portal/branches";
        }

        BigDecimal subtotal = portalService.calculateAmount(space, durationHours);
        BigDecimal discount = portalService.calculateDiscount(subtotal, voucherCode);
        model.addAttribute("user", user);
        model.addAttribute("rankName", portalService.getMemberRankName(user));
        model.addAttribute("activePage", "booking");
        model.addAttribute("space", space);
        model.addAttribute("arrivalTime", arrivalTime);
        model.addAttribute("arrivalTimeValue", arrivalTime.toString());
        model.addAttribute("endTime", arrivalTime.plusHours(durationHours));
        model.addAttribute("durationHours", durationHours);
        model.addAttribute("note", note == null ? "" : note);
        model.addAttribute("voucherCode", voucherCode == null ? "" : voucherCode.trim());
        model.addAttribute("vouchers", portalService.getActiveVouchers());
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        model.addAttribute("totalAmount", subtotal.subtract(discount));
        return "web/checkout";
    }

    @PostMapping("/portal/bookings")
    public String createBooking(@Valid @ModelAttribute("bookingForm") BookingForm bookingForm,
                                BindingResult bindingResult,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        SessionUser user = currentMember(session);
        if (user == null) {
            return "redirect:/login";
        }
        if (user.getMaKH() == null || user.getMaKH().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản này chưa có hồ sơ hội viên, nên chưa thể đặt chỗ.");
            return "redirect:/portal/branches";
        }
        if (!portalService.hasCompleteContactInfo(user)) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ email và số điện thoại để đặt trước không gian.");
            return "redirect:/portal/branches";
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin đặt chỗ.");
            return "redirect:/portal/branches";
        }

        try {
            portalService.createBooking(user, bookingForm);
            redirectAttributes.addFlashAttribute("success", "Đặt chỗ thành công. Nhân viên sẽ kiểm tra và xác nhận yêu cầu của bạn.");
            return "redirect:/portal";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/portal/branches";
        }
    }

    @GetMapping("/portal/benefits")
    public String benefits(@RequestParam(name = "keyword", required = false) String keyword,
                           HttpSession session,
                           Model model) {
        SessionUser user = currentMember(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("rankName", portalService.getMemberRankName(user));
        model.addAttribute("activePage", "benefits");
        var vouchers = portalService.getActiveVouchers();
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
        return "web/benefits";
    }

    @GetMapping("/portal/history")
    public String history(HttpSession session, Model model) {
        SessionUser user = currentMember(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("rankName", portalService.getMemberRankName(user));
        model.addAttribute("activePage", "history");
        model.addAttribute("histories", portalService.getMemberBookingHistory(user.getMaKH()));
        return "web/history";
    }

    @GetMapping(value = "/portal/history/{maPhien}/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> sessionQr(@PathVariable("maPhien") String maPhien, HttpSession session) {
        SessionUser user = currentMember(session);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return portalService.getSessionQrPng(user.getMaKH(), maPhien)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate())
                        .body(bytes))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/portal/account")
    public String account(HttpSession session, Model model) {
        SessionUser user = currentMember(session);
        if (user == null) {
            return "redirect:/login";
        }
        List<BookingView> bookings = memberBookings(user.getMaKH());
        AccountProfileView profile = portalService.getAccountProfile(user);
        model.addAttribute("user", user);
        model.addAttribute("rankName", portalService.getMemberRankName(user));
        model.addAttribute("profile", profile);
        model.addAttribute("activePage", "account");
        model.addAttribute("joinDate", LocalDate.now());
        model.addAttribute("bookingCount", bookings.size());
        model.addAttribute("lastBooking", bookings.stream()
                .filter(booking -> booking.getThoiGianDuKienToi() != null)
                .max(Comparator.comparing(BookingView::getThoiGianDuKienToi))
                .orElse(null));
        return "web/account";
    }

    @PostMapping("/portal/account")
    public String updateAccount(@ModelAttribute AccountProfileForm form,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        SessionUser user = currentMember(session);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            AccountProfileView profile = portalService.updateAccountProfile(user, form);
            if (profile != null) {
                session.setAttribute("user", new SessionUser(
                        user.getMaND(),
                        user.getMaKH(),
                        profile.getHoTen(),
                        user.getTenTaiKhoan(),
                        user.isStaff()
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

    private SessionUser currentMember(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        if (user == null) {
            return null;
        }
        if (user.isStaff()) {
            return null;
        }
        return user;
    }

    private List<BookingView> memberBookings(String maKH) {
        if (maKH == null || maKH.isBlank()) {
            return List.of();
        }
        return portalService.getMemberBookings(maKH);
    }
}
