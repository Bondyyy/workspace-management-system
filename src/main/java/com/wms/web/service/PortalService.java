package com.wms.web.service;

import com.wms.web.form.BookingForm;
import com.wms.web.model.BookingView;
import com.wms.web.model.BranchView;
import com.wms.web.model.SessionUser;
import com.wms.web.model.SpaceView;
import com.wms.web.model.VoucherView;
import com.wms.web.repository.WebPortalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
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

    public List<SpaceView> getSpaces(String branchId) {
        return repository.findSpaces((branchId == null || branchId.isBlank()) ? null : branchId);
    }

    public Optional<BranchView> getBranch(String branchId) {
        return getBranches().stream()
                .filter(branch -> branch.getMaCN().equals(branchId))
                .findFirst();
    }

    public SpaceView getSpace(String maKG) {
        return repository.findSpaceById(maKG);
    }

    public List<BookingView> getMemberBookings(String maKH) {
        return repository.findBookingsForMember(maKH);
    }

    public List<BookingView> getAllBookings() {
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
            throw new IllegalArgumentException("Tài khoản này không phải hội viên đặt chỗ.");
        }

        SpaceView space = repository.findSpaceById(form.getMaKG());
        if (space == null) {
            throw new IllegalArgumentException("Không tìm thấy không gian đã chọn.");
        }

        String normalizedStatus = normalize(space.getTrangThaiKG());
        if (!(normalizedStatus.contains("trong") || normalizedStatus.contains("dat truoc"))) {
            throw new IllegalArgumentException("Không gian này hiện chưa sẵn sàng để đặt.");
        }

        BigDecimal beforeDiscount = calculateAmount(space, form.getDurationHours());
        BigDecimal total = calculateFinalAmount(beforeDiscount, form.getVoucherCode());
        String note = form.getNote();
        if (form.getVoucherCode() != null && !form.getVoucherCode().isBlank()) {
            note = (note == null || note.isBlank())
                    ? "Mã giảm giá: " + form.getVoucherCode().trim()
                    : note + " | Mã giảm giá: " + form.getVoucherCode().trim();
        }
        repository.createBooking(
                repository.nextBookingId(),
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

    @Transactional
    public void confirmBooking(String maDatCho) {
        repository.updateBookingStatus(
                maDatCho,
                "Đã thanh toán thành công",
                " | Nhân viên đã xác nhận yêu cầu."
        );
    }

    @Transactional
    public void markUsed(String maDatCho) {
        repository.updateBookingStatus(
                maDatCho,
                "Đã sử dụng",
                " | Đã nhận chỗ tại quầy."
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
