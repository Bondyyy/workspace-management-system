package com.wms.web.service;

import com.wms.web.form.DatChoForm;
import com.wms.web.form.ThongTinTaiKhoanForm;
import com.wms.web.form.YeuCauWebhookThanhToan;
import com.wms.web.model.ThongTinTaiKhoanView;
import com.wms.web.model.LichSuDatChoView;
import com.wms.web.model.ThanhToanDatChoView;
import com.wms.web.model.DatChoView;
import com.wms.web.model.ChiNhanhView;
import com.wms.web.model.NguoiDungPhien;
import com.wms.web.model.KhongGianView;
import com.wms.web.model.PhieuGiamGiaView;
import com.wms.web.model.KetQuaNhanChoBangQRView;
import com.wms.web.model.ThongTinNhanChoBangQR;
import com.wms.web.repository.CongThongTinWebRepository;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.ThongTinXacNhanDatChoDTO;
import com.wms.util.EmailUtil;
import com.wms.util.MaQRUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CongThongTinService {

    private static final Pattern MA_DAT_CHO_PATTERN = Pattern.compile("\\bDC\\d{3,12}\\b", Pattern.CASE_INSENSITIVE);
    private static final ZoneId MUI_GIO_VIET_NAM = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int SO_GIO_DAT_CHO_MAC_DINH = 2;

    private final CongThongTinWebRepository khoDuLieu;

    public CongThongTinService(CongThongTinWebRepository khoDuLieu) {
        this.khoDuLieu = khoDuLieu;
    }

    public List<ChiNhanhView> layChiNhanh() {
        return khoDuLieu.timChiNhanhHoatDong();
    }

    public ThongTinTaiKhoanView layThongTinTaiKhoan(NguoiDungPhien user) {
        if (user == null) {
            return null;
        }
        return khoDuLieu.timThongTinTaiKhoan(user.getMaND());
    }

    public boolean coThongTinLienHeDayDu(NguoiDungPhien user) {
        ThongTinTaiKhoanView profile = layThongTinTaiKhoan(user);
        return profile != null && profile.coThongTinLienHeDayDu();
    }

    public String layTenHangThanhVien(NguoiDungPhien user) {
        ThongTinTaiKhoanView profile = layThongTinTaiKhoan(user);
        if (profile == null || profile.getHangThanhVien() == null || profile.getHangThanhVien().isBlank()) {
            return "Không có";
        }
        return profile.getHangThanhVien();
    }

    @Transactional
    public ThongTinTaiKhoanView capNhatThongTinTaiKhoan(NguoiDungPhien user, ThongTinTaiKhoanForm form) {
        if (user == null) {
            throw new IllegalArgumentException("Phiên đăng nhập đã hết hạn.");
        }
        if (form.getHoTen() == null || form.getHoTen().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập họ và tên.");
        }
        khoDuLieu.capNhatThongTinTaiKhoan(
                user.getMaND(),
                form.getHoTen().trim(),
                form.getEmail(),
                form.getSoDienThoai(),
                form.getNgaySinh(),
                form.getGioiTinh()
        );
        return khoDuLieu.timThongTinTaiKhoan(user.getMaND());
    }

    public List<KhongGianView> layKhongGian(String branchId) {
        return khoDuLieu.timKhongGian((branchId == null || branchId.isBlank()) ? null : branchId);
    }

    public List<KhongGianView> layKhongGian(String branchId, LocalDateTime selectedStart, LocalDateTime selectedEnd) {
        return khoDuLieu.timKhongGian((branchId == null || branchId.isBlank()) ? null : branchId, selectedStart, selectedEnd);
    }

    public Optional<ChiNhanhView> layMotChiNhanh(String branchId) {
        return layChiNhanh().stream()
                .filter(branch -> branch.getMaCN().equals(branchId))
                .findFirst();
    }

    public KhongGianView layMotKhongGian(String maKG) {
        return khoDuLieu.timKhongGianTheoMa(maKG);
    }

    public KhungGioDatCho khungGioDatChoMacDinh(ChiNhanhView branch) {
        LocalTime openTime = docGioChiNhanh(branch == null ? null : branch.getThoiGianMoCua(), LocalTime.of(7, 0));
        LocalTime closeTime = docGioChiNhanh(branch == null ? null : branch.getThoiGianDongCua(), LocalTime.of(22, 0));
        LocalDateTime hienTai = layThoiGianHienTaiVietNam();
        LocalDate ngayDat = hienTai.toLocalDate();
        LocalTime gioBatDau;

        if (hienTai.toLocalTime().isBefore(openTime)) {
            gioBatDau = openTime;
        } else if (!hienTai.toLocalTime().isBefore(closeTime)) {
            ngayDat = ngayDat.plusDays(1);
            gioBatDau = openTime;
        } else {
            gioBatDau = lamTronLenGioKeTiep(hienTai).toLocalTime();
        }

        LocalTime gioKetThuc = gioBatDau.plusHours(SO_GIO_DAT_CHO_MAC_DINH);
        if (gioBatDau.isBefore(openTime)
                || !gioBatDau.isBefore(closeTime)
                || !gioKetThuc.isAfter(gioBatDau)
                || gioKetThuc.isAfter(closeTime)) {
            ngayDat = ngayDat.plusDays(1);
            gioBatDau = openTime;
            gioKetThuc = openTime.plusHours(SO_GIO_DAT_CHO_MAC_DINH);
        }

        LocalDateTime thoiGianGoiY = LocalDateTime.of(ngayDat, gioBatDau);
        if (!thoiGianGoiY.isAfter(hienTai)) {
            ngayDat = hienTai.toLocalDate().plusDays(1);
            gioBatDau = openTime;
            gioKetThuc = openTime.plusHours(SO_GIO_DAT_CHO_MAC_DINH);
        }

        return new KhungGioDatCho(ngayDat, gioBatDau, gioKetThuc);
    }

    public List<String> layLuaChonGio(ChiNhanhView branch) {
        LocalTime openTime = docGioChiNhanh(branch == null ? null : branch.getThoiGianMoCua(), LocalTime.of(7, 0));
        LocalTime closeTime = docGioChiNhanh(branch == null ? null : branch.getThoiGianDongCua(), LocalTime.of(22, 0));
        java.util.ArrayList<String> options = new java.util.ArrayList<>();
        for (LocalTime time = openTime; !time.isAfter(closeTime); time = time.plusHours(1)) {
            options.add(time.toString());
        }
        return options;
    }

    public List<DatChoView> layDatChoTheoHoiVien(String maKH) {
        khoDuLieu.taoPhienConThieuChoDatCho();
        return khoDuLieu.timDatChoTheoHoiVien(maKH);
    }

    public List<LichSuDatChoView> layLichSuDatChoHoiVien(String maKH) {
        khoDuLieu.taoPhienConThieuChoDatCho();
        return khoDuLieu.timLichSuDatChoCuaHoiVien(maKH);
    }

    public List<DatChoView> layTatCaDatCho() {
        khoDuLieu.taoPhienConThieuChoDatCho();
        return khoDuLieu.timTatCaDatCho();
    }

    public List<PhieuGiamGiaView> layPhieuGiamGiaHieuLuc() {
        return khoDuLieu.timPhieuGiamGiaHieuLuc();
    }

    public PhieuGiamGiaView layPhieuGiamGiaHieuLucTheoMa(String voucherCode) {
        return khoDuLieu.timPhieuGiamGiaHieuLucTheoMa(voucherCode);
    }

    @Transactional
    public String taoDatCho(NguoiDungPhien user, DatChoForm form) {
        if (user.getMaKH() == null || user.getMaKH().isBlank()) {
            throw new IllegalArgumentException("Tai khoan nay khong co ho so hoi vien de dat cho.");
        }

        KhongGianView space = khoDuLieu.timKhongGianTheoMa(form.getMaKG());
        if (space == null) {
            throw new IllegalArgumentException("Khong tim thay khong gian da chon.");
        }

        String normalizedStatus = chuanHoa(space.getTrangThaiKG());
        if (normalizedStatus.contains("bao tri")) {
            throw new IllegalArgumentException("Khong gian nay dang bao tri.");
        }
        kiemTraThoiGianDatCho(form.getThoiGianDen(), form.getSoGioSuDung());
        kiemTraKhungGioChiNhanh(space, form.getThoiGianDen(), form.getSoGioSuDung());
        if (khoDuLieu.coTrungLich(
                form.getMaKG(),
                form.getThoiGianDen(),
                form.getThoiGianDen().plusHours(form.getSoGioSuDung()))) {
            throw new IllegalArgumentException("Khung gio nay da co nguoi dat. Vui long chon gio khac.");
        }

        BigDecimal beforeDiscount = tinhTien(space, form.getSoGioSuDung());
        BigDecimal total = tinhThanhTienSauGiam(beforeDiscount, form.getMaGiamGia());
        String note = form.getGhiChu();
        if (form.getMaGiamGia() != null && !form.getMaGiamGia().isBlank()) {
            note = (note == null || note.isBlank())
                    ? "Ma giam gia: " + form.getMaGiamGia().trim()
                    : note + " | Ma giam gia: " + form.getMaGiamGia().trim();
        }

        return khoDuLieu.taoDatCho(
                user,
                form.getMaKG(),
                form.getThoiGianDen(),
                form.getSoGioSuDung(),
                total,
                note
        );
    }

    public BigDecimal tinhTien(KhongGianView space, Integer durationHours) {
        if (space == null || durationHours == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal hourlyRate = space.getDonGiaTheoGio() == null ? BigDecimal.ZERO : space.getDonGiaTheoGio();
        return hourlyRate.multiply(BigDecimal.valueOf(durationHours));
    }

    public BigDecimal tinhGiamGia(BigDecimal subtotal, String voucherCode) {
        if (subtotal == null || voucherCode == null || voucherCode.isBlank()) {
            return BigDecimal.ZERO;
        }
        PhieuGiamGiaView voucher = layPhieuGiamGiaHieuLucTheoMa(voucherCode);
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

    public BigDecimal tinhThanhTienSauGiam(BigDecimal subtotal, String voucherCode) {
        BigDecimal safeSubtotal = subtotal == null ? BigDecimal.ZERO : subtotal;
        return safeSubtotal.subtract(tinhGiamGia(safeSubtotal, voucherCode));
    }

    public void kiemTraXacNhanDatCho(String maKG, LocalDateTime arrivalTime, Integer durationHours) {
        kiemTraThoiGianDatCho(arrivalTime, durationHours);
        KhongGianView space = khoDuLieu.timKhongGianTheoMa(maKG);
        kiemTraKhungGioChiNhanh(space, arrivalTime, durationHours);
        if (khoDuLieu.coTrungLich(maKG, arrivalTime, arrivalTime.plusHours(durationHours))) {
            throw new IllegalArgumentException("Khong gian nay da duoc dat trong khung gio ban chon.");
        }
    }

    public boolean laThoiGianDatChoTrongTuongLai(LocalDateTime thoiGianDatCho) {
        return thoiGianDatCho != null && thoiGianDatCho.isAfter(layThoiGianHienTaiVietNam());
    }

    public ThanhToanDatChoView layThanhToanDatCho(NguoiDungPhien user, String maDatCho) {
        if (user == null || user.getMaKH() == null || user.getMaKH().isBlank()) {
            return null;
        }
        return khoDuLieu.timThanhToanDatCho(maDatCho, user.getMaKH());
    }

    @Transactional
    public KetQuaWebhookThanhToan xacNhanThanhToanDemo(NguoiDungPhien user, String maDatCho) {
        ThanhToanDatChoView payment = layThanhToanDatCho(user, maDatCho);
        if (payment == null) {
            return new KetQuaWebhookThanhToan(false, "Không tìm thấy đặt chỗ của hội viên hiện tại.", maDatCho);
        }
        YeuCauWebhookThanhToan request = new YeuCauWebhookThanhToan();
        request.setMaGiaoDich("MOCK-" + maDatCho + "-" + System.currentTimeMillis());
        request.setSoTien(payment.getThanhTien());
        request.setNoiDung(payment.getNoiDungChuyenKhoan());
        request.setTrangThai("SUCCESS");
        request.setThoiGianThanhToan(LocalDateTime.now());
        return xuLyWebhookThanhToan(request);
    }

    @Transactional
    public void xacNhanDatChoDaThanhToan(String maDatCho) {
        ThongTinXacNhanDatChoDTO thongTin = khoDuLieu.timThongTinXacNhanTheoDatCho(maDatCho);
        if (thongTin == null) {
            throw new IllegalArgumentException("Không tìm thấy đặt chỗ cần xác nhận.");
        }
        String maQR = MaQRUtil.taoMaQRDatCho(maDatCho);
        boolean confirmed = khoDuLieu.xacNhanDatChoDaTraTien(
                maDatCho,
                maQR,
                " | Hệ thống đã xác nhận thanh toán."
        );
        if (!confirmed) {
            throw new IllegalArgumentException("Đặt chỗ không còn ở trạng thái chờ thanh toán.");
        }
        thongTin.setMaQR(maQR);
        guiEmailXacNhanDatCho(thongTin);
    }

    @Transactional
    public void danhDauDatChoDaSuDung(String maDatCho) {
        boolean created = khoDuLieu.taoPhienChoDatChoDaCheckIn(maDatCho);
        if (!created) {
            throw new IllegalArgumentException("Đặt chỗ chưa thanh toán, đã sử dụng hoặc không tồn tại.");
        }
    }

    @Transactional
    public KetQuaNhanChoBangQRView nhanChoBangMaQR(String noiDungQR, NguoiDungPhien nhanVien) {
        if (nhanVien == null || !nhanVien.laNhanVien()) {
            return KetQuaNhanChoBangQRView.thatBai("Phiên đăng nhập nhân viên đã hết hạn.");
        }
        if (noiDungQR == null || noiDungQR.isBlank()) {
            return KetQuaNhanChoBangQRView.thatBai("Nội dung QR không hợp lệ.");
        }

        String maQR = noiDungQR.trim();
        Optional<ThongTinNhanChoBangQR> ketQuaTim = khoDuLieu.timDatChoTheoMaQRDeNhanCho(maQR);
        if (ketQuaTim.isEmpty()) {
            return KetQuaNhanChoBangQRView.thatBai("QR không tồn tại, đã hết hiệu lực hoặc đã được sử dụng.");
        }

        ThongTinNhanChoBangQR thongTin = ketQuaTim.get();
        String trangThai = chuanHoa(thongTin.getTrangThaiDatTruoc());
        if (trangThai.contains("su dung")) {
            return KetQuaNhanChoBangQRView.thatBai("Mã QR này đã được sử dụng.");
        }
        if (!trangThai.contains("thanh toan thanh cong")) {
            return KetQuaNhanChoBangQRView.thatBai("Đặt chỗ chưa thanh toán thành công.");
        }
        if (khoDuLieu.daCoPhienTheoDatCho(thongTin.getMaDatCho())) {
            return KetQuaNhanChoBangQRView.thatBai("Mã QR này đã được sử dụng.");
        }

        LocalDateTime bayGio = khoDuLieu.layThoiGianHeThong();
        LocalDateTime gioDuKienToi = thongTin.getThoiGianDuKienToi();
        if (gioDuKienToi == null) {
            return KetQuaNhanChoBangQRView.thatBai("Đặt chỗ thiếu thời gian dự kiến tới.");
        }
        if (bayGio.isBefore(gioDuKienToi)) {
            return KetQuaNhanChoBangQRView.thatBai("Quá sớm, chưa đến giờ nhận chỗ hợp lệ.");
        }
        if (bayGio.isAfter(gioDuKienToi.plusHours(thongTin.laySoGioSuDungAnToan()))) {
            return KetQuaNhanChoBangQRView.thatBai("Mã QR đã quá hạn nhận chỗ.");
        }

        try {
            String maPhien = khoDuLieu.moPhienTuDatCho(thongTin);
            return KetQuaNhanChoBangQRView.thanhCong("Mở phiên thành công.", thongTin, maPhien);
        } catch (RuntimeException ex) {
            return KetQuaNhanChoBangQRView.thatBai(chuyenLoiNhanChoThanThien(ex));
        }
    }

    private String taoMaPhienMoi() {
        return "";
    }

    @Transactional
    public KetQuaWebhookThanhToan xuLyWebhookThanhToan(YeuCauWebhookThanhToan request) {
        System.out.println("[Webhook] Webhook thanh toan duoc goi vao Service.");
        if (request == null) {
            System.err.println("[Webhook] Request body bi NULL.");
            return new KetQuaWebhookThanhToan(false, "Webhook không có dữ liệu.");
        }
        
        System.out.println("[Webhook] Payload chi tiet: transactionId='" + request.getMaGiaoDich() 
                + "', amount=" + request.getSoTien() + ", content='" + request.getNoiDung() + "'");

        String maDatCho = tachMaDatCho(request.getNoiDung());
        if (maDatCho == null) {
            System.err.println("[Webhook] KHONG parse duoc MaDatCho tu noi dung chuyen khoan: '" + request.getNoiDung() + "'");
            return new KetQuaWebhookThanhToan(false, "Không tìm thấy mã đặt chỗ trong nội dung chuyển khoản.");
        }
        System.out.println("[Webhook] Parse duoc MaDatCho: " + maDatCho);
        System.out.println("[Webhook] So tien tu webhook (soTien): " + request.getSoTien() + " VND");

        // Idempotency: Kiem tra trung maGiaoDich trong GhiChu
        String currentGhiChu = khoDuLieu.timGhiChuDatCho(maDatCho);
        if (currentGhiChu != null && request.getMaGiaoDich() != null && !request.getMaGiaoDich().isBlank()) {
            if (currentGhiChu.contains(request.getMaGiaoDich())) {
                System.out.println("[Webhook] Giao dich trung maGiaoDich: " + request.getMaGiaoDich() 
                        + ". Bo qua khong xu ly lai. Trang thai hien tai: Đã thanh toán thành công");
                return new KetQuaWebhookThanhToan(true, "Đặt chỗ " + maDatCho + " đã được xử lý trước đó với mã giao dịch này.", maDatCho);
            }
        }

        ThanhToanDatChoView payment;
        try {
            payment = khoDuLieu.timThanhToanDatChoForUpdate(maDatCho);
        } catch (org.springframework.dao.DataAccessException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "";
            if (msg.contains("ORA-00054") 
                    || ex instanceof org.springframework.dao.CannotAcquireLockException 
                    || ex instanceof org.springframework.dao.PessimisticLockingFailureException) {
                System.err.println("[Webhook] Dat cho " + maDatCho + " dang duoc giao dich khac xu ly (ORA-00054).");
                return new KetQuaWebhookThanhToan(false, "Đặt chỗ đang được giao dịch khác xử lý.", maDatCho);
            }
            throw ex;
        }

        if (payment == null) {
            System.err.println("[Webhook] Khong tim thay DatCho trong CSDL: " + maDatCho);
            return new KetQuaWebhookThanhToan(false, "Không tìm thấy đặt chỗ " + maDatCho + ".", maDatCho);
        }

        System.out.println("[Webhook] ThanhTien trong DATCHO: " + payment.getThanhTien() + " VND");
        System.out.println("[Webhook] Trang thai truoc khi xu ly: " + payment.getTrangThaiDatTruoc());

        // Idempotency: Neu dat cho da o trang thai thanh toan thanh cong
        if (chuanHoa(payment.getTrangThaiDatTruoc()).contains("thanh cong")) {
            System.out.println("[Webhook] Dat cho " + maDatCho + " da o trang thai thanh toan thanh cong. Trang thai sau khi xu ly: " + payment.getTrangThaiDatTruoc());
            return new KetQuaWebhookThanhToan(true, "Đặt chỗ " + maDatCho + " đã được xử lý trước đó.", maDatCho);
        }

        if (!laTrangThaiWebhookThanhCong(request.getTrangThai())) {
            System.err.println("[Webhook] Trang thai trong request khong hop le: " + request.getTrangThai() + ". Trang thai sau khi xu ly: " + payment.getTrangThaiDatTruoc());
            return new KetQuaWebhookThanhToan(false, "Giao dịch ngân hàng chưa ở trạng thái thanh toán thành công.", maDatCho);
        }

        BigDecimal expectedAmount = lamTronTienVnd(payment.getThanhTien());
        if (request.getSoTien() == null) {
            System.err.println("[Webhook] Thieu so tien trong request. Trang thai sau khi xu ly: " + payment.getTrangThaiDatTruoc());
            return new KetQuaWebhookThanhToan(false, "Webhook thiếu số tiền chuyển khoản.", maDatCho);
        }
        BigDecimal paidAmount = lamTronTienVnd(request.getSoTien());
        if (paidAmount.compareTo(expectedAmount) < 0) {
            String reason = "Số tiền chuyển khoản chưa đủ. Cần "
                    + dinhDangTien(expectedAmount) + ", nhận " + dinhDangTien(paidAmount) + ".";
            System.err.println("[Webhook] " + reason + " Trang thai sau khi xu ly: " + payment.getTrangThaiDatTruoc());
            return new KetQuaWebhookThanhToan(false, reason, maDatCho);
        }

        ThongTinXacNhanDatChoDTO thongTin = khoDuLieu.timThongTinXacNhanTheoDatCho(maDatCho);
        if (thongTin == null) {
            System.err.println("[Webhook] Khong lay duoc chi tiet dat cho: " + maDatCho + ". Trang thai sau khi xu ly: " + payment.getTrangThaiDatTruoc());
            return new KetQuaWebhookThanhToan(false, "Không lấy được chi tiết đặt chỗ " + maDatCho + ".", maDatCho);
        }
        String maQR = MaQRUtil.taoMaQRDatCho(maDatCho);
        boolean confirmed = khoDuLieu.xacNhanDatChoDaTraTien(
                maDatCho,
                maQR,
                " | Webhook đã xác nhận giao dịch " + chuoiAnToan(request.getMaGiaoDich())
                        + " với số tiền " + dinhDangTien(paidAmount) + "."
        );
        if (!confirmed) {
            System.err.println("[Webhook] UPDATE that bai (co the do trang thai da bi doi boi giao dich song song). Trang thai sau khi xu ly: " + payment.getTrangThaiDatTruoc());
            return new KetQuaWebhookThanhToan(true, "Đặt chỗ " + maDatCho + " đã được xử lý trước đó.", maDatCho);
        }
        
        System.out.println("[Webhook] Trang thai sau khi xu ly: Đã thanh toán thành công");

        thongTin.setMaQR(maQR);
        boolean emailSent = guiEmailXacNhanDatCho(thongTin);
        String message = emailSent
                ? "Đã xác nhận thanh toán cho " + maDatCho + " và đã gửi email QR nhận chỗ."
                : "Đã xác nhận thanh toán cho " + maDatCho
                    + " nhưng chưa gửi được email. Khách vẫn xem được QR trong lịch sử.";
        return new KetQuaWebhookThanhToan(true, message, maDatCho);
    }

    @Transactional
    public void hetHanDatChoChoThanhToan() {
        List<ThongTinXacNhanDatChoDTO> expiredBookings = khoDuLieu.timDatChoChoThanhToanDaHetHan();
        if (expiredBookings != null && !expiredBookings.isEmpty()) {
            System.out.println("[Scheduler] Tim thay " + expiredBookings.size() + " dat cho cho thanh toan da het han.");
            for (ThongTinXacNhanDatChoDTO thongTin : expiredBookings) {
                String maDatCho = thongTin.getMaDatCho();
                String reason = "Chưa nhận được thanh toán sau 10 phút giữ chỗ.";
                System.out.println("[Scheduler] Tien hanh huy dat cho het han: MaDatCho=" + maDatCho 
                        + ", KhachHang=" + thongTin.getHoTen() + ", Email=" + thongTin.getEmail()
                        + ", ThanhTien=" + thongTin.getThanhTien());
                
                boolean updated = khoDuLieu.ghiNhanThanhToanDatChoThatBai(maDatCho, reason);
                if (updated) {
                    System.out.println("[Scheduler] Huy thanh cong " + maDatCho + ". gui email thong bao.");
                    guiEmailThanhToanThatBai(thongTin, reason);
                } else {
                    System.out.println("[Scheduler] Bo qua vi trang thai da thay doi doi voi DatCho: " + maDatCho);
                }
            }
        }
    }

    private String tachMaDatCho(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        Matcher matcher = MA_DAT_CHO_PATTERN.matcher(description.toUpperCase());
        return matcher.find() ? matcher.group().toUpperCase() : null;
    }

    private String chuoiAnToan(String value) {
        return value == null || value.isBlank() ? "không có mã giao dịch" : value.trim();
    }

    private String chuyenLoiNhanChoThanThien(RuntimeException ex) {
        String message = ex.getMessage() == null ? "" : ex.getMessage();
        String normalized = chuanHoa(message);
        if (normalized.contains("qua som") || normalized.contains("chua den gio")) {
            return "Quá sớm, chưa đến giờ nhận chỗ hợp lệ.";
        }
        if (normalized.contains("qua han")) {
            return "Mã QR đã quá hạn nhận chỗ.";
        }
        if (normalized.contains("da duoc su dung") || normalized.contains("da su dung")) {
            return "Mã QR này đã được sử dụng.";
        }
        if (normalized.contains("chua thanh toan") || normalized.contains("giao dich that bai")) {
            return "Đặt chỗ chưa thanh toán thành công.";
        }
        if (normalized.contains("khong gian")) {
            return "Không gian chưa sẵn sàng để mở phiên.";
        }
        return "Không thể mở phiên từ mã QR này.";
    }

    private boolean laTrangThaiWebhookThanhCong(String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        String normalized = chuanHoa(status);
        return normalized.equals("1")
                || normalized.equals("ok")
                || normalized.contains("success")
                || normalized.contains("paid")
                || normalized.contains("complete")
                || normalized.contains("thanh cong")
                || normalized.contains("da nhan")
                || normalized.contains("nhan tien");
    }

    private BigDecimal lamTronTienVnd(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.max(BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP);
    }

    private String chuanHoa(String value) {
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

    private void kiemTraThoiGianDatCho(LocalDateTime arrivalTime, Integer durationHours) {
        if (arrivalTime == null || durationHours == null || durationHours < 1) {
            throw new IllegalArgumentException("Vui lòng chọn thời gian đến và thời gian rời hợp lệ.");
        }
        if (!laThoiGianDatChoTrongTuongLai(arrivalTime)) {
            throw new IllegalArgumentException("Thời gian đặt chỗ không hợp lệ. Vui lòng chọn thời gian lớn hơn thời điểm hiện tại.");
        }
    }

    private void kiemTraKhungGioChiNhanh(KhongGianView space, LocalDateTime arrivalTime, Integer durationHours) {
        if (space == null || arrivalTime == null || durationHours == null) {
            throw new IllegalArgumentException("Vui long chon khong gian va khung gio hop le.");
        }
        LocalTime openTime = docGioChiNhanh(space.getThoiGianMoCua(), LocalTime.of(7, 0));
        LocalTime closeTime = docGioChiNhanh(space.getThoiGianDongCua(), LocalTime.of(22, 0));
        LocalTime start = arrivalTime.toLocalTime();
        LocalTime end = arrivalTime.plusHours(durationHours).toLocalTime();
        if (start.isBefore(openTime) || end.isAfter(closeTime) || !end.isAfter(start)) {
            throw new IllegalArgumentException("Khung gio phai nam trong thoi gian mo cua cua chi nhanh.");
        }
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
            return LocalTime.parse(normalized);
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private LocalDateTime layThoiGianHienTaiVietNam() {
        return LocalDateTime.now(MUI_GIO_VIET_NAM);
    }

    private LocalDateTime lamTronLenGioKeTiep(LocalDateTime thoiGian) {
        LocalDateTime ketQua = thoiGian.withMinute(0).withSecond(0).withNano(0);
        if (thoiGian.getMinute() > 0 || thoiGian.getSecond() > 0 || thoiGian.getNano() > 0) {
            ketQua = ketQua.plusHours(1);
        }
        return ketQua;
    }

    public Optional<byte[]> layAnhQrDatChoPng(String maKH, String maDatCho) {
        String maQR = khoDuLieu.timQrDatChoCuaHoiVien(maKH, maDatCho);
        if (maQR == null || maQR.isBlank()) {
            return Optional.empty();
        }
        byte[] png = MaQRUtil.taoAnhPng(maQR);
        return png.length == 0 ? Optional.empty() : Optional.of(png);
    }

    public Optional<byte[]> layAnhQrPhienPng(String maKH, String maPhien) {
        return Optional.empty();
    }

    private boolean guiEmailXacNhanDatCho(ThongTinXacNhanDatChoDTO thongTin) {
        if (thongTin == null || thongTin.getEmail() == null || thongTin.getEmail().isBlank()) {
            return false;
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
                thongTin.getMaQR(),
                qrPng
        );
        if (!sent) {
            System.err.println("[CongThongTinService] Đã xác nhận đặt chỗ nhưng chưa gửi được email cho " + thongTin.getEmail());
            System.err.println("[CongThongTinService] QR nhận chỗ dự phòng: " + thongTin.getMaQR());
        }
        return sent;
    }

    private void guiEmailThanhToanThatBai(ThongTinXacNhanDatChoDTO thongTin, String lyDo) {
        if (thongTin == null || thongTin.getEmail() == null || thongTin.getEmail().isBlank()) {
            return;
        }
        boolean sent = EmailUtil.guiEmailThanhToanDatChoThatBai(
                thongTin.getEmail(),
                thongTin.getHoTen(),
                thongTin.getMaDatCho(),
                thongTin.getTenKhongGian(),
                thongTin.getTenChiNhanh(),
                dinhDangKhoangThoiGian(thongTin),
                dinhDangTien(thongTin.getThanhTien()),
                lyDo
        );
        if (!sent) {
            System.err.println("[CongThongTinService] Chưa gửi được email thanh toán thất bại cho " + thongTin.getEmail());
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

    public record KhungGioDatCho(LocalDate date, LocalTime startTime, LocalTime endTime) {
    }

    public record KetQuaWebhookThanhToan(boolean success, String message, String maDatCho) {
        public KetQuaWebhookThanhToan(boolean success, String message) {
            this(success, message, null);
        }
    }
}
