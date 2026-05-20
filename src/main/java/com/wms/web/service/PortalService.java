package com.wms.web.service;

import com.wms.web.form.BookingForm;
import com.wms.web.form.AccountProfileForm;
import com.wms.web.model.AccountProfileView;
import com.wms.web.model.BookingHistoryView;
import com.wms.web.model.BookingView;
import com.wms.web.model.BranchView;
import com.wms.web.model.SessionUser;
import com.wms.web.model.SpaceView;
import com.wms.web.model.VoucherView;
import com.wms.web.repository.WebPortalRepository;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.ThongTinXacNhanDatChoDTO;
import com.wms.util.EmailUtil;
import com.wms.util.MaQRUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class PortalService {

    private final WebPortalRepository repository;

    public PortalService(WebPortalRepository repository) {
        this.repository = repository;
    }

    public List<BranchView> getBranches() {
        return repository.findActiveBranches();
    }

    public AccountProfileView getAccountProfile(SessionUser user) {
        if (user == null) {
            return null;
        }
        return repository.findAccountProfile(user.getMaND());
    }

    public boolean hasCompleteContactInfo(SessionUser user) {
        AccountProfileView profile = getAccountProfile(user);
        return profile != null && profile.hasCompleteContactInfo();
    }

    public String getMemberRankName(SessionUser user) {
        AccountProfileView profile = getAccountProfile(user);
        if (profile == null || profile.getHangThanhVien() == null || profile.getHangThanhVien().isBlank()) {
            return "Không có";
        }
        return profile.getHangThanhVien();
    }

    @Transactional
    public AccountProfileView updateAccountProfile(SessionUser user, AccountProfileForm form) {
        if (user == null) {
            throw new IllegalArgumentException("Phiên đăng nhập đã hết hạn.");
        }
        if (form.getHoTen() == null || form.getHoTen().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập họ và tên.");
        }
        repository.updateAccountProfile(
                user.getMaND(),
                form.getHoTen().trim(),
                form.getEmail(),
                form.getSoDienThoai(),
                form.getNgaySinh(),
                form.getGioiTinh()
        );
        return repository.findAccountProfile(user.getMaND());
    }

    public List<SpaceView> getSpaces(String branchId) {
        return repository.findSpaces((branchId == null || branchId.isBlank()) ? null : branchId);
    }

    public List<SpaceView> getSpaces(String branchId, LocalDateTime selectedStart, LocalDateTime selectedEnd) {
        return repository.findSpaces((branchId == null || branchId.isBlank()) ? null : branchId, selectedStart, selectedEnd);
    }

    public Optional<BranchView> getBranch(String branchId) {
        return getBranches().stream()
                .filter(branch -> branch.getMaCN().equals(branchId))
                .findFirst();
    }

    public SpaceView getSpace(String maKG) {
        return repository.findSpaceById(maKG);
    }

    public BookingWindow defaultBookingWindow(BranchView branch) {
        LocalTime openTime = parseBranchTime(branch == null ? null : branch.getThoiGianMoCua(), LocalTime.of(7, 0));
        LocalTime closeTime = parseBranchTime(branch == null ? null : branch.getThoiGianDongCua(), LocalTime.of(22, 0));
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime roundedStart = now.withMinute(0).withSecond(0).withNano(0);
        if (now.getMinute() > 0 || now.getSecond() > 0 || now.getNano() > 0) {
            roundedStart = roundedStart.plusHours(1);
        }

        if (roundedStart.toLocalTime().isBefore(openTime)) {
            roundedStart = LocalDateTime.of(today, openTime);
        }

        if (!roundedStart.toLocalTime().plusHours(2).isAfter(closeTime)) {
            return new BookingWindow(roundedStart.toLocalDate(), roundedStart.toLocalTime(), roundedStart.toLocalTime().plusHours(2));
        }

        LocalDateTime tomorrowStart = LocalDateTime.of(today.plusDays(1), openTime);
        return new BookingWindow(tomorrowStart.toLocalDate(), tomorrowStart.toLocalTime(), tomorrowStart.toLocalTime().plusHours(2));
    }

    public List<String> timeOptions(BranchView branch) {
        LocalTime openTime = parseBranchTime(branch == null ? null : branch.getThoiGianMoCua(), LocalTime.of(7, 0));
        LocalTime closeTime = parseBranchTime(branch == null ? null : branch.getThoiGianDongCua(), LocalTime.of(22, 0));
        java.util.ArrayList<String> options = new java.util.ArrayList<>();
        for (LocalTime time = openTime; !time.isAfter(closeTime); time = time.plusHours(1)) {
            options.add(time.toString());
        }
        return options;
    }

    public List<BookingView> getMemberBookings(String maKH) {
        repository.createMissingSessionsForBookings();
        return repository.findBookingsForMember(maKH);
    }

    public List<BookingHistoryView> getMemberBookingHistory(String maKH) {
        repository.createMissingSessionsForBookings();
        return repository.findBookingHistoryForMember(maKH);
    }

    public List<BookingView> getAllBookings() {
        repository.createMissingSessionsForBookings();
        return repository.findAllBookings();
    }

    public List<VoucherView> getActiveVouchers() {
        return repository.findActiveVouchers();
    }

    public VoucherView getActiveVoucher(String voucherCode) {
        return repository.findActiveVoucherByCode(voucherCode);
    }

    @Transactional
    public void createBooking(SessionUser user, BookingForm form) {
        if (user.getMaKH() == null || user.getMaKH().isBlank()) {
            throw new IllegalArgumentException("Tai khoan nay khong co ho so hoi vien de dat cho.");
        }

        SpaceView space = repository.findSpaceById(form.getMaKG());
        if (space == null) {
            throw new IllegalArgumentException("Khong tim thay khong gian da chon.");
        }

        String normalizedStatus = normalize(space.getTrangThaiKG());
        if (normalizedStatus.contains("bao tri")) {
            throw new IllegalArgumentException("Khong gian nay dang bao tri.");
        }
        validateBookingTime(form.getArrivalTime(), form.getDurationHours());
        validateBranchWindow(space, form.getArrivalTime(), form.getDurationHours());
        if (repository.hasScheduleConflict(
                form.getMaKG(),
                form.getArrivalTime(),
                form.getArrivalTime().plusHours(form.getDurationHours()))) {
            throw new IllegalArgumentException("Khung gio nay da co nguoi dat. Vui long chon gio khac.");
        }

        BigDecimal beforeDiscount = calculateAmount(space, form.getDurationHours());
        BigDecimal total = calculateFinalAmount(beforeDiscount, form.getVoucherCode());
        String note = form.getNote();
        if (form.getVoucherCode() != null && !form.getVoucherCode().isBlank()) {
            note = (note == null || note.isBlank())
                    ? "Ma giam gia: " + form.getVoucherCode().trim()
                    : note + " | Ma giam gia: " + form.getVoucherCode().trim();
        }

        repository.createBooking(
                repository.nextBookingId(),
                repository.nextSessionId(),
                repository.nextInvoiceId(),
                user,
                form.getMaKG(),
                form.getArrivalTime(),
                form.getDurationHours(),
                total,
                note
        );
    }

    public BigDecimal calculateAmount(SpaceView space, Integer durationHours) {
        if (space == null || durationHours == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal hourlyRate = space.getDonGiaTheoGio() == null ? BigDecimal.ZERO : space.getDonGiaTheoGio();
        return hourlyRate.multiply(BigDecimal.valueOf(durationHours));
    }

    public BigDecimal calculateDiscount(BigDecimal subtotal, String voucherCode) {
        if (subtotal == null || voucherCode == null || voucherCode.isBlank()) {
            return BigDecimal.ZERO;
        }
        VoucherView voucher = getActiveVoucher(voucherCode);
        if (voucher == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal minimum = voucher.getGiaTriApDungToiThieu() == null
                ? BigDecimal.ZERO
                : voucher.getGiaTriApDungToiThieu();
        if (subtotal.compareTo(minimum) < 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = voucher.getGiaTriGiamGia() == null ? BigDecimal.ZERO : voucher.getGiaTriGiamGia();
        return discount.min(subtotal);
    }

    public BigDecimal calculateFinalAmount(BigDecimal subtotal, String voucherCode) {
        BigDecimal safeSubtotal = subtotal == null ? BigDecimal.ZERO : subtotal;
        return safeSubtotal.subtract(calculateDiscount(safeSubtotal, voucherCode));
    }

    public void validateCheckout(String maKG, LocalDateTime arrivalTime, Integer durationHours) {
        validateBookingTime(arrivalTime, durationHours);
        SpaceView space = repository.findSpaceById(maKG);
        validateBranchWindow(space, arrivalTime, durationHours);
        if (repository.hasScheduleConflict(maKG, arrivalTime, arrivalTime.plusHours(durationHours))) {
            throw new IllegalArgumentException("Khong gian nay da duoc dat trong khung gio ban chon.");
        }
    }

    @Transactional
    public void confirmBooking(String maDatCho) {
        ThongTinXacNhanDatChoDTO thongTin = repository.findConfirmationDetailsByBooking(maDatCho);
        String maQR = thongTin == null ? null : MaQRUtil.taoMaQRPhien(thongTin.getMaPhien(), maDatCho);
        repository.updateBookingStatus(
                maDatCho,
                "Da thanh toan thanh cong",
                " | Nhan vien da xac nhan yeu cau."
        );
        repository.updateInvoiceStatusByBooking(maDatCho, "Da thanh toan thanh cong");
        if (maQR != null) {
            repository.updateBookingQr(maDatCho, maQR);
            thongTin.setMaQR(maQR);
            guiEmailXacNhanDatCho(thongTin);
        }
    }

    @Transactional
    public void markUsed(String maDatCho) {
        repository.updateBookingStatus(
                maDatCho,
                "Da su dung",
                " | Da nhan cho tai quay."
        );
        repository.updateInvoiceStatusByBooking(maDatCho, "Da thanh toan thanh cong");
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void validateBookingTime(LocalDateTime arrivalTime, Integer durationHours) {
        if (arrivalTime == null || durationHours == null || durationHours < 1) {
            throw new IllegalArgumentException("Vui long chon thoi gian den va thoi gian roi hop le.");
        }
        if (arrivalTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thoi gian den phai o tuong lai.");
        }
    }

    private void validateBranchWindow(SpaceView space, LocalDateTime arrivalTime, Integer durationHours) {
        if (space == null || arrivalTime == null || durationHours == null) {
            throw new IllegalArgumentException("Vui long chon khong gian va khung gio hop le.");
        }
        LocalTime openTime = parseBranchTime(space.getThoiGianMoCua(), LocalTime.of(7, 0));
        LocalTime closeTime = parseBranchTime(space.getThoiGianDongCua(), LocalTime.of(22, 0));
        LocalTime start = arrivalTime.toLocalTime();
        LocalTime end = arrivalTime.plusHours(durationHours).toLocalTime();
        if (start.isBefore(openTime) || end.isAfter(closeTime) || !end.isAfter(start)) {
            throw new IllegalArgumentException("Khung gio phai nam trong thoi gian mo cua cua chi nhanh.");
        }
    }

    private LocalTime parseBranchTime(String value, LocalTime fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim();
        if (normalized.length() > 5) {
            normalized = normalized.substring(0, 5);
        }
        try {
            return LocalTime.parse(normalized);
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    public Optional<byte[]> getSessionQrPng(String maKH, String maPhien) {
        String maQR = repository.findSessionQrForMember(maKH, maPhien);
        if (maQR == null || maQR.isBlank()) {
            return Optional.empty();
        }
        byte[] png = MaQRUtil.taoAnhPng(maQR);
        return png.length == 0 ? Optional.empty() : Optional.of(png);
    }

    private void guiEmailXacNhanDatCho(ThongTinXacNhanDatChoDTO thongTin) {
        if (thongTin == null || thongTin.getEmail() == null || thongTin.getEmail().isBlank()) {
            return;
        }
        byte[] qrPng = MaQRUtil.taoAnhPng(thongTin.getMaQR());
        boolean sent = EmailUtil.guiEmailXacNhanDatChoDaThanhToan(
                thongTin.getEmail(),
                thongTin.getHoTen(),
                thongTin.getMaPhien(),
                thongTin.getMaDatCho(),
                thongTin.getTenKhongGian(),
                thongTin.getTenChiNhanh(),
                dinhDangKhoangThoiGian(thongTin),
                dinhDangTien(thongTin.getThanhTien()),
                qrPng
        );
        if (!sent) {
            System.err.println("[PortalService] Đã xác nhận đặt chỗ nhưng chưa gửi được email cho " + thongTin.getEmail());
        }
    }

    private String dinhDangKhoangThoiGian(ThongTinXacNhanDatChoDTO thongTin) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        if (thongTin.getThoiGianBatDau() == null) {
            return "Chưa có";
        }
        LocalDateTime start = thongTin.getThoiGianBatDau().toLocalDateTime();
        if (thongTin.getThoiGianDuKienKetThuc() == null) {
            return formatter.format(start);
        }
        LocalDateTime end = thongTin.getThoiGianDuKienKetThuc().toLocalDateTime();
        return formatter.format(start) + " - " + formatter.format(end);
    }

    private String dinhDangTien(BigDecimal value) {
        if (value == null) {
            return "0 VNĐ";
        }
        return new DecimalFormat("#,### VNĐ").format(value);
    }

    public record BookingWindow(LocalDate date, LocalTime startTime, LocalTime endTime) {
    }
}
