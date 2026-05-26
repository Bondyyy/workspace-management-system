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
import com.wms.util.BusinessHoursUtil;
import com.wms.util.MaQRUtil;
import com.wms.util.PasswordUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CongThongTinService {

    private static final Pattern MA_DAT_CHO_PATTERN = Pattern.compile("\\bDC\\d{3,12}\\b", Pattern.CASE_INSENSITIVE);
    private static final ZoneId MUI_GIO_VIET_NAM = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int SO_GIO_DAT_CHO_MAC_DINH = 2;
    private static final long AVATAR_MAX_BYTES = 2L * 1024L * 1024L;

    private final CongThongTinWebRepository khoDuLieu;

    public CongThongTinService(CongThongTinWebRepository khoDuLieu) {
        this.khoDuLieu = khoDuLieu;
    }

    public List<ChiNhanhView> layChiNhanh() {
        try {
            return khoDuLieu.timChiNhanhHoatDong();
        } catch (DataAccessException ex) {
            System.err.println("[CongThongTinService] Loi tai danh sach chi nhanh: " + ex.getMessage());
            throw new IllegalStateException("Không thể tải danh sách chi nhánh lúc này. Vui lòng thử lại sau.");
        }
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
        if (form.getNgaySinh() != null && form.getNgaySinh().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày sinh không được lớn hơn ngày hiện tại.");
        }
        khoDuLieu.capNhatThongTinTaiKhoan(
                user.getMaND(),
                form.getHoTen().trim(),
                form.getEmail(),
                form.getSoDienThoai(),
                form.getNgaySinh(),
                form.getGioiTinh()
        );
        capNhatAnhDaiDienNeuCo(user.getMaND(), form.getAnhDaiDien());
        return khoDuLieu.timThongTinTaiKhoan(user.getMaND());
    }

    public Optional<byte[]> layAnhDaiDien(NguoiDungPhien user) {
        if (user == null || user.getMaND() == null || user.getMaND().isBlank()) {
            return Optional.empty();
        }
        byte[] bytes = khoDuLieu.layAnhDaiDien(user.getMaND());
        return bytes == null || bytes.length == 0 ? Optional.empty() : Optional.of(bytes);
    }

    @Transactional
    public void doiMatKhau(NguoiDungPhien user, String matKhauHienTai,
                           String matKhauMoi, String xacNhanMatKhauMoi) {
        if (user == null) {
            throw new IllegalArgumentException("Phiên đăng nhập đã hết hạn.");
        }
        if (matKhauHienTai == null || matKhauHienTai.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại.");
        }
        if (matKhauMoi == null || matKhauMoi.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu mới.");
        }
        if (xacNhanMatKhauMoi == null || xacNhanMatKhauMoi.isBlank()) {
            throw new IllegalArgumentException("Vui lòng xác nhận mật khẩu mới.");
        }
        if (!matKhauMoi.equals(xacNhanMatKhauMoi)) {
            throw new IllegalArgumentException("Xác nhận mật khẩu mới không khớp.");
        }
        if (matKhauMoi.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới cần ít nhất 6 ký tự.");
        }
        if (matKhauMoi.equals(matKhauHienTai)) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng mật khẩu hiện tại.");
        }

        String matKhauMaHoaHienTai = khoDuLieu.layMatKhauMaHoaTheoMaND(user.getMaND());
        if (matKhauMaHoaHienTai == null || matKhauMaHoaHienTai.isBlank()
                || !PasswordUtil.verify(matKhauHienTai, matKhauMaHoaHienTai)) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
        }
        khoDuLieu.capNhatMatKhau(user.getMaND(), PasswordUtil.hash(matKhauMoi));
    }

    public List<KhongGianView> layKhongGian(String branchId) {
        try {
            return sapXepKhongGian(khoDuLieu.timKhongGian(chuanHoaMaCN(branchId)));
        } catch (DataAccessException ex) {
            System.err.println("[CongThongTinService] Loi tai khong gian maCN=" + branchId + ": " + ex.getMessage());
            throw new IllegalStateException("Không thể tải sơ đồ không gian lúc này. Vui lòng thử lại sau.");
        }
    }

    public List<KhongGianView> layKhongGian(String branchId, LocalDateTime selectedStart, LocalDateTime selectedEnd) {
        try {
            hetHanDatChoChoThanhToan();
            return sapXepKhongGian(khoDuLieu.timKhongGian(chuanHoaMaCN(branchId), selectedStart, selectedEnd));
        } catch (DataAccessException ex) {
            System.err.println("[CongThongTinService] Loi tai khong gian maCN=" + branchId + ": " + ex.getMessage());
            throw new IllegalStateException("Không thể tải sơ đồ không gian lúc này. Vui lòng thử lại sau.");
        }
    }

    public Optional<ChiNhanhView> layMotChiNhanh(String branchId) {
        String maCN = chuanHoaMaCN(branchId);
        if (maCN == null) {
            return Optional.empty();
        }
        return layChiNhanh().stream()
                .filter(branch -> branch.getMaCN().equalsIgnoreCase(maCN))
                .findFirst();
    }

    public KhongGianView layMotKhongGian(String maKG) {
        return khoDuLieu.timKhongGianTheoMa(maKG);
    }

    public KhungGioDatCho khungGioDatChoMacDinh(ChiNhanhView branch) {
        LocalTime openTime = docGioChiNhanh(branch == null ? null : branch.getThoiGianMoCua(), LocalTime.of(7, 0));
        LocalTime closeTime = docGioChiNhanh(branch == null ? null : branch.getThoiGianDongCua(), LocalTime.of(22, 0));
        LocalDateTime hienTai = layThoiGianHienTaiVietNam();
        LocalDateTime[] window = BusinessHoursUtil.nextWindowFrom(hienTai, openTime, closeTime);
        LocalDateTime start = window[0].isAfter(hienTai) ? window[0] : lamTronLenGioKeTiep(hienTai);
        if (!BusinessHoursUtil.isStartWithinBusinessHours(start, openTime, closeTime)) {
            window = BusinessHoursUtil.nextWindowFrom(window[1].plusSeconds(1), openTime, closeTime);
            start = window[0];
        }
        LocalDateTime end = start.plusHours(SO_GIO_DAT_CHO_MAC_DINH);
        return new KhungGioDatCho(start.toLocalDate(), start.toLocalTime(), end.toLocalTime());
    }

    public List<String> layLuaChonGio(ChiNhanhView branch) {
        LocalTime openTime = docGioChiNhanh(branch == null ? null : branch.getThoiGianMoCua(), LocalTime.of(7, 0));
        LocalTime closeTime = docGioChiNhanh(branch == null ? null : branch.getThoiGianDongCua(), LocalTime.of(22, 0));
        return BusinessHoursUtil.hourlyOptions(openTime, closeTime, true);
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
        hetHanDatChoChoThanhToan();
        if (khoDuLieu.coTrungLich(
                form.getMaKG(),
                form.getThoiGianDen(),
                form.getThoiGianDen().plusHours(form.getSoGioSuDung()))) {
            throw new IllegalArgumentException("Khung gio nay da co nguoi dat. Vui long chon gio khac.");
        }

        BigDecimal tongTienGocDatTruoc = lamTronTienVnd(tinhTien(space, form.getSoGioSuDung()));
        PhieuGiamGiaView voucher = null;
        BigDecimal tienGiamVoucher = BigDecimal.ZERO;
        String voucherCode = form.getMaGiamGia() == null ? "" : form.getMaGiamGia().trim();
        if (!voucherCode.isBlank()) {
            voucher = layPhieuGiamGiaHieuLucTheoMa(voucherCode);
            if (voucher == null) {
                throw new IllegalArgumentException("Mã ưu đãi không hợp lệ, hết hạn hoặc đã hết lượt sử dụng.");
            }
            BigDecimal minimum = voucher.getGiaTriApDungToiThieu() == null
                    ? BigDecimal.ZERO
                    : voucher.getGiaTriApDungToiThieu();
            if (tongTienGocDatTruoc.compareTo(minimum) < 0) {
                throw new IllegalArgumentException("Đơn này chưa đạt mức tối thiểu "
                        + dinhDangTien(minimum) + " để dùng mã ưu đãi.");
            }
            BigDecimal giaTriVoucher = voucher.getGiaTriGiamGia() == null
                    ? BigDecimal.ZERO
                    : voucher.getGiaTriGiamGia();
            tienGiamVoucher = giaTriVoucher.min(tongTienGocDatTruoc).max(BigDecimal.ZERO);
        }

        CongThongTinWebRepository.HangThanhVienSnapshot hangThanhVien =
                khoDuLieu.layHangThanhVienCuaKhach(user.getMaKH());
        BigDecimal phanTramGiamHangTV = hangThanhVien == null || hangThanhVien.phanTramTienGiam() == null
                ? BigDecimal.ZERO
                : hangThanhVien.phanTramTienGiam().max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));
        BigDecimal tienSauVoucher = tongTienGocDatTruoc.subtract(tienGiamVoucher).max(BigDecimal.ZERO);
        BigDecimal tienGiamHangTV = tienSauVoucher
                .multiply(phanTramGiamHangTV)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO);
        BigDecimal thanhTienSauGiam = tongTienGocDatTruoc
                .subtract(tienGiamVoucher)
                .subtract(tienGiamHangTV)
                .max(BigDecimal.ZERO)
                .setScale(0, RoundingMode.HALF_UP);

        return khoDuLieu.taoDatCho(
                user,
                form.getMaKG(),
                form.getThoiGianDen(),
                form.getSoGioSuDung(),
                tongTienGocDatTruoc,
                voucher == null ? null : voucher.getMaPGG(),
                voucher == null ? null : voucher.getMaChuSoPGG(),
                tienGiamVoucher,
                phanTramGiamHangTV,
                tienGiamHangTV,
                thanhTienSauGiam,
                form.getGhiChu()
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

    public BigDecimal layPhanTramGiamHangThanhVien(NguoiDungPhien user) {
        if (user == null || user.getMaKH() == null || user.getMaKH().isBlank()) {
            return BigDecimal.ZERO;
        }
        CongThongTinWebRepository.HangThanhVienSnapshot snapshot =
                khoDuLieu.layHangThanhVienCuaKhach(user.getMaKH());
        if (snapshot == null || snapshot.phanTramTienGiam() == null) {
            return BigDecimal.ZERO;
        }
        return snapshot.phanTramTienGiam().max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));
    }

    public BigDecimal tinhGiamHangThanhVien(NguoiDungPhien user, BigDecimal amountAfterVoucher) {
        BigDecimal base = amountAfterVoucher == null ? BigDecimal.ZERO : amountAfterVoucher.max(BigDecimal.ZERO);
        BigDecimal percent = layPhanTramGiamHangThanhVien(user);
        return base.multiply(percent).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
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

    private String parseMaDatChoTuQR(String qrContent) {
        if (qrContent == null || qrContent.isBlank()) {
            return null;
        }
        String parsed = tachMaDatCho(qrContent);
        if (parsed != null) {
            return parsed;
        }
        Pattern p = Pattern.compile("DATCHO=(DC\\d{3,12})", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(qrContent);
        if (m.find()) {
            return m.group(1).toUpperCase();
        }
        return null;
    }

    @Transactional
    public void tuDongKetThucPhienQuaHanDatCho() {
        new com.wms.dao.TrangChuQuanLy.QuanLyPhien.PhienLamViecDAO().tuDongKetThucPhienQuaHanDatCho();
    }

    @Transactional
    public void xuLyDatChoDaThanhToanNhungKhongDen() {
        System.out.println("[Scheduler] Bat dau kiem tra dat cho da thanh toan nhung khach khong den");
        List<DatChoView> quaHanList = khoDuLieu.timDatChoDaThanhToanNhungKhongDenQuaGio();
        int size = (quaHanList != null) ? quaHanList.size() : 0;
        System.out.println("[Scheduler] So dat cho qua han tim thay: " + size);
        if (quaHanList != null && !quaHanList.isEmpty()) {
            for (DatChoView dc : quaHanList) {
                String maDatCho = dc.getMaDatCho();
                String maKG = khoDuLieu.timMaKGCuaDatCho(maDatCho);
                if (maKG == null) {
                    continue;
                }
                int rows = khoDuLieu.danhDauDatChoKhongDenThanhDaSuDung(maDatCho);
                if (rows == 1) {
                    khoDuLieu.capNhatTrangThaiKhongGianSauKhiDatChoHetHieuLuc(maKG);
                    System.out.println("[Scheduler] Da nha MaDatCho=" + maDatCho + ", MaKG=" + maKG);
                }
            }
        } else {
            try {
                List<java.util.Map<String, Object>> list = khoDuLieu.timDatChoDaThanhToanDeDebug();
                if (list == null || list.isEmpty()) {
                    System.out.println("[Scheduler] Debug: Khong tim thay bat ky dat cho nao o trang thai 'Da thanh toan thanh cong' de phan tich.");
                } else {
                    for (java.util.Map<String, Object> map : list) {
                        System.out.println("[Scheduler] Debug dat cho dang cho: MaDatCho=" + map.get("MaDatCho")
                                + ", TrangThaiDatTruoc=" + map.get("TrangThaiDatTruoc")
                                + ", ThoiGianDuKienToi=" + map.get("ThoiGianDuKienToi")
                                + ", KhoangThoiGianSuDung=" + map.get("KhoangThoiGianSuDung")
                                + ", SYSTIMESTAMP=" + map.get("GioHeThong")
                                + ", SoPhien=" + map.get("SoPhien"));
                    }
                }
            } catch (Exception e) {
                System.out.println("[Scheduler] Debug: Khong the truy van thong tin debug dat cho: " + e.getMessage());
            }
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
        String maDatCho = parseMaDatChoTuQR(maQR);
        Optional<ThongTinNhanChoBangQR> ketQuaTim = Optional.empty();
        
        if (maDatCho != null) {
            ketQuaTim = khoDuLieu.timDatChoTheoMaDatChoDeNhanCho(maDatCho);
        }
        if (ketQuaTim.isEmpty()) {
            ketQuaTim = khoDuLieu.timDatChoTheoMaQRDeNhanCho(maQR);
        }

        if (ketQuaTim.isEmpty()) {
            return KetQuaNhanChoBangQRView.thatBai("QR không tồn tại, đã hết hiệu lực hoặc đã được sử dụng.");
        }

        ThongTinNhanChoBangQR thongTin = ketQuaTim.get();
        String maCNNhanVien = nhanVien.getMaCN() == null ? "" : nhanVien.getMaCN().trim();
        String maCNDatCho = thongTin.getMaCN() == null ? "" : thongTin.getMaCN().trim();
        if (maCNNhanVien.isBlank()) {
            return KetQuaNhanChoBangQRView.thatBai("Tài khoản nhân viên chưa được gán chi nhánh để nhận chỗ.");
        }
        if (maCNDatCho.isBlank()) {
            return KetQuaNhanChoBangQRView.thatBai("Đặt chỗ thiếu thông tin chi nhánh, không thể nhận chỗ.");
        }
        if (!maCNNhanVien.equalsIgnoreCase(maCNDatCho)) {
            return KetQuaNhanChoBangQRView.thatBai("Nhân viên chỉ có thể nhận chỗ cho đặt chỗ thuộc chi nhánh đang làm việc.");
        }

        String trangThai = chuanHoa(thongTin.getTrangThaiDatTruoc());
        boolean coPhien = khoDuLieu.daCoPhienTheoDatCho(thongTin.getMaDatCho());

        System.out.println("[QR CheckIn] MaDatCho=" + thongTin.getMaDatCho()
                + ", MaKG=" + thongTin.getMaKG()
                + ", TrangThaiDatTruoc=" + thongTin.getTrangThaiDatTruoc()
                + ", ThoiGianDuKienToi=" + thongTin.getThoiGianDuKienToi()
                + ", coPhien=" + coPhien);

        if (trangThai.contains("qua han nhan cho")) {
            return KetQuaNhanChoBangQRView.thatBai("Đặt chỗ này đã quá hạn nhận chỗ.");
        }

        if (trangThai.contains("su dung")) {
            if (coPhien) {
                return KetQuaNhanChoBangQRView.thatBai("Mã QR này đã được sử dụng.");
            }
        }
        
        if (coPhien) {
            return KetQuaNhanChoBangQRView.thatBai("Mã QR này đã được sử dụng.");
        }

        if (thongTin.getMaQR() == null || thongTin.getMaQR().isBlank()) {
            return KetQuaNhanChoBangQRView.thatBai("Mã QR không còn hiệu lực để nhận chỗ.");
        }

        if (!trangThai.contains("thanh toan thanh cong")) {
            return KetQuaNhanChoBangQRView.thatBai("Đặt chỗ chưa thanh toán thành công.");
        }

        LocalDateTime bayGio = khoDuLieu.layThoiGianHeThong();
        LocalDateTime gioDuKienToi = thongTin.getThoiGianDuKienToi();
        if (gioDuKienToi == null) {
            return KetQuaNhanChoBangQRView.thatBai("Đặt chỗ thiếu thời gian dự kiến tới.");
        }
        
        LocalDateTime thoiGianDuKienKetThuc = gioDuKienToi.plusHours(thongTin.laySoGioSuDungAnToan());
        if (bayGio.isAfter(thoiGianDuKienKetThuc)) {
            int rows = khoDuLieu.danhDauDatChoKhongDenThanhDaSuDung(thongTin.getMaDatCho());
            if (rows == 1) {
                khoDuLieu.capNhatTrangThaiKhongGianSauKhiDatChoHetHieuLuc(thongTin.getMaKG());
            }
            return KetQuaNhanChoBangQRView.thatBai("Đặt chỗ đã quá giờ nhận chỗ. Hệ thống đã nhả không gian.");
        }

        if (bayGio.isBefore(gioDuKienToi.minusMinutes(15))) {
            return KetQuaNhanChoBangQRView.thatBai("Quá sớm, chưa đến giờ nhận chỗ hợp lệ.");
        }
        if (bayGio.isAfter(gioDuKienToi.plusHours(thongTin.laySoGioSuDungAnToan()))) {
            int rows = khoDuLieu.danhDauDatChoKhongDenThanhDaSuDung(thongTin.getMaDatCho());
            if (rows == 1) {
                khoDuLieu.capNhatTrangThaiKhongGianSauKhiDatChoHetHieuLuc(thongTin.getMaKG());
            }
            return KetQuaNhanChoBangQRView.thatBai("Đặt chỗ đã quá giờ nhận chỗ. Hệ thống đã nhả không gian.");
        }

        try {
            String maPhien = khoDuLieu.moPhienTuDatCho(thongTin);
            System.out.println("[QR CheckIn] Mo phien thanh cong MaDatCho=" + thongTin.getMaDatCho()
                    + ", MaKG=" + thongTin.getMaKG() + ", MaPhien=" + maPhien);
            return KetQuaNhanChoBangQRView.thanhCong("Mở phiên thành công.", thongTin, maPhien);
        } catch (RuntimeException ex) {
            String maPhienRetry = khoDuLieu.timMaPhienTheoDatCho(thongTin.getMaDatCho());
            if (maPhienRetry != null && !maPhienRetry.isBlank()) {
                System.out.println("[QR CheckIn] Retry sau khi phien da mo MaDatCho="
                        + thongTin.getMaDatCho() + ", MaPhien=" + maPhienRetry);
                return KetQuaNhanChoBangQRView.thatBai("Mã QR này đã được sử dụng.");
            }
            System.err.println("[QR CheckIn] Mo phien that bai MaDatCho=" + thongTin.getMaDatCho()
                    + ", MaKG=" + thongTin.getMaKG() + ", loi=" + ex.getMessage());
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
        if (normalized.contains("trang thai hien tai")) {
            return ex.getMessage();
        }
        if (normalized.contains("khong gian")) {
            return "Không gian chưa sẵn sàng để mở phiên.";
        }
        return "Không thể mở phiên từ mã QR này.";
    }

    private boolean laTrangThaiWebhookThanhCong(String status) {
        if (status == null || status.isBlank()) {
            return false;
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
            throw new IllegalArgumentException("Vui lòng chọn thời gian bắt đầu và số giờ sử dụng hợp lệ.");
        }
        if (!laThoiGianDatChoTrongTuongLai(arrivalTime)) {
            throw new IllegalArgumentException("Thời gian đặt chỗ không hợp lệ. Vui lòng chọn thời gian lớn hơn thời điểm hiện tại.");
        }
    }

    private void kiemTraKhungGioChiNhanh(KhongGianView space, LocalDateTime arrivalTime, Integer durationHours) {
        if (space == null || arrivalTime == null || durationHours == null) {
            throw new IllegalArgumentException("Vui lòng chọn không gian và khung giờ hợp lệ.");
        }
        LocalTime openTime = docGioChiNhanh(space.getThoiGianMoCua(), LocalTime.of(7, 0));
        LocalTime closeTime = docGioChiNhanh(space.getThoiGianDongCua(), LocalTime.of(22, 0));

        String openStr = BusinessHoursUtil.format(openTime);
        String closeStr = BusinessHoursUtil.format(closeTime);

        if (!BusinessHoursUtil.isStartWithinBusinessHours(arrivalTime, openTime, closeTime)) {
            throw new IllegalArgumentException("Thời điểm bắt đầu đặt chỗ phải nằm trong giờ hoạt động của chi nhánh: " + openStr + " - " + closeStr + ".");
        }
    }

    private String chuanHoaMaCN(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private List<KhongGianView> sapXepKhongGian(List<KhongGianView> spaces) {
        if (spaces == null || spaces.isEmpty()) {
            return List.of();
        }
        return spaces.stream()
                .sorted(Comparator
                        .comparing((KhongGianView space) -> space.getViTri() == null ? "" : space.getViTri(),
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(space -> space.getTenKG() == null ? "" : space.getTenKG(),
                                String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(space -> space.getMaKG() == null ? "" : space.getMaKG(),
                                String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private void capNhatAnhDaiDienNeuCo(String maND, MultipartFile anhDaiDien) {
        if (anhDaiDien == null || anhDaiDien.isEmpty()) {
            return;
        }
        String contentType = anhDaiDien.getContentType() == null ? "" : anhDaiDien.getContentType().toLowerCase();
        if (!contentType.equals("image/jpeg")
                && !contentType.equals("image/png")
                && !contentType.equals("image/webp")) {
            throw new IllegalArgumentException("Ảnh đại diện phải là PNG, JPG hoặc WebP.");
        }
        if (anhDaiDien.getSize() > AVATAR_MAX_BYTES) {
            throw new IllegalArgumentException("Ảnh đại diện không được vượt quá 2MB.");
        }
        try {
            khoDuLieu.capNhatAnhDaiDien(maND, anhDaiDien.getBytes());
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException("Không đọc được ảnh đại diện. Vui lòng chọn lại tệp ảnh.");
        } catch (DataAccessException ex) {
            System.err.println("[CongThongTinService] Loi cap nhat anh dai dien: " + ex.getMessage());
            throw new IllegalStateException("Không thể cập nhật ảnh đại diện lúc này. Vui lòng thử lại sau.");
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
            return BusinessHoursUtil.parseBranchTime(normalized, fallback);
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
        byte[] png = MaQRUtil.taoAnhPng(maQR, 600);
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
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
        return com.wms.util.InputFormatUtil.formatThousands(value) + " VNĐ";
    }

    public record KhungGioDatCho(LocalDate date, LocalTime startTime, LocalTime endTime) {
    }

    public record KetQuaWebhookThanhToan(boolean success, String message, String maDatCho) {
        public KetQuaWebhookThanhToan(boolean success, String message) {
            this(success, message, null);
        }
    }
}
