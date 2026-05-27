package com.wms.service.TrangChuQuanLy.QuanLyPhien;

import com.wms.dao.TrangChuQuanLy.QuanLyPhien.DatChoDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyHoaDon.HoaDonDAO;
import com.wms.controller.TrangChuGioiThieu.DangNhapController;
import com.wms.dao.TrangChuQuanLy.QuanLyHoiVien.KhachHangDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyKhongGian.KhongGianDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyNhanVien.NhanVienDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyPhien.PhienLamViecDAO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.DichVuTrongPhienDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoiVien.HoiVienDTO;
import com.wms.model.TrangChuQuanLy.QuanLyKhongGian.KhongGianDTO;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.KetQuaNhanChoDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.ThongTinXacNhanDatChoDTO;
import com.wms.util.EmailUtil;
import com.wms.util.MaQRUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhienLamViecService {
    private static final Pattern MA_DAT_CHO_PATTERN = Pattern.compile("\\bDC\\d{3,12}\\b", Pattern.CASE_INSENSITIVE);

    private final PhienLamViecDAO phienDAO;
    private final KhachHangDAO khachHangDAO;
    private final KhongGianDAO khongGianDAO;
    private final NhanVienDAO nhanVienDAO;
    private final DatChoDAO datChoDAO;
    private final HoaDonDAO hoaDonDAO;

    public PhienLamViecService() {
        this.phienDAO = new PhienLamViecDAO();
        this.khachHangDAO = new KhachHangDAO();
        this.khongGianDAO = new KhongGianDAO();
        this.nhanVienDAO = new NhanVienDAO();
        this.datChoDAO = new DatChoDAO();
        this.hoaDonDAO = new HoaDonDAO();
    }

    public List<PhienLamViecFullDTO> layDanhSachPhien(String keyword, String maCN) {
        return phienDAO.layDanhSachPhien(keyword, maCN);
    }

    public List<DichVuTrongPhienDTO> layDichVuCuaPhien(String maPhien) {
        return phienDAO.layDichVuCuaPhien(maPhien);
    }

    public boolean ketThucPhien(String maPhien) {
        NguoiDungDTO user = DangNhapController.getCurrentUser();
        String maNV = null;
        if (user != null) {
            maNV = user.getMaNV();
            if ((maNV == null || maNV.isBlank()) && user.getMaND() != null) {
                maNV = nhanVienDAO.layMaNVTuMaND(user.getMaND());
            }
        }
        return phienDAO.ketThucPhien(maPhien, maNV);
    }

    public boolean xacNhanThanhToanDatTruoc(String maDatCho, String maPhien) {
        String maQR = MaQRUtil.taoMaQRPhien(maPhien, maDatCho);
        boolean dcOk = datChoDAO.xacNhanThanhToan(maDatCho);
        boolean qrOk = dcOk && datChoDAO.capNhatMaQR(maDatCho, maQR);
        boolean hdOk = hoaDonDAO.capNhatTrangThaiThanhToanTheoPhien(maPhien, "Đã trả trước");
        if (dcOk && qrOk && hdOk) {
            guiEmailXacNhanDatCho(maDatCho, maPhien);
        }
        return dcOk && qrOk && hdOk;
    }

    public List<String[]> layDanhSachChiNhanh() {
        return nhanVienDAO.layDanhSachChiNhanh();
    }

    public String layMaCNTheNguoiDung(NguoiDungDTO user) {
        if (user == null) return null;
        if (user.getMaCN() != null && !user.getMaCN().trim().isEmpty()) {
            return user.getMaCN().trim();
        }
        return nhanVienDAO.layMaCNTuMaND(user.getMaND());
    }

    public List<KhongGianDTO> layKhongGian(String maCN) {
        if (maCN != null && !maCN.isEmpty()) {
            return khongGianDAO.layTheoChiNhanh(maCN);
        }
        return khongGianDAO.layTatCaKhongGian();
    }

    public HoiVienDTO timKhachHangTheoSdt(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) {
            return null;
        }
        return khachHangDAO.timTheoSdt(sdt.trim());
    }

    public String timHoacTaoKhachHang(String hoTen, String sdt) {
        HoiVienDTO khachHang = timKhachHangTheoSdt(sdt);
        if (khachHang != null) {
            return khachHang.getMaKH();
        }

        HoiVienDTO newKH = new HoiVienDTO();
        newKH.setHoTen(hoTen != null ? hoTen.trim() : "");
        newKH.setSdt(sdt != null ? sdt.trim() : "");
        newKH.setTrangThai("Đang hoạt động");
        newKH.setLoaiKH("Khách vãng lai");
        try {
            khachHangDAO.insert(newKH);
            khachHang = timKhachHangTheoSdt(sdt);
            if (khachHang != null) {
                return khachHang.getMaKH();
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecService] Lỗi tạo khách hàng: " + e.getMessage());
        }
        return null;
    }

    public String[] layGioHoatDongTheoKhongGian(String maKG) {
        return phienDAO.layGioHoatDongTheoKhongGian(maKG);
    }

    public boolean taoPhienMoi(String maKH, String maKG, int soGioSuDung, double donGiaTheoGio) {
        if (soGioSuDung <= 0) {
            throw new IllegalArgumentException("Thoi gian su dung phai lon hon 0.");
        }
        String[] gioHoatDong = phienDAO.layGioHoatDongTheoKhongGian(maKG);
        if (gioHoatDong == null) {
            throw new IllegalArgumentException("Loi: Khong tim thay khong gian hoac chi nhanh lien ket.");
        }

        String gioMoCua = gioHoatDong[0];
        String gioDongCua = gioHoatDong[1];

        java.time.ZoneId zoneId = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
        java.time.ZonedDateTime nowHcm = java.time.ZonedDateTime.now(zoneId);

        java.time.LocalTime openLocalTime = com.wms.util.BusinessHoursUtil.parseBranchTime(
                gioMoCua, java.time.LocalTime.of(7, 0));
        java.time.LocalTime closeLocalTime = com.wms.util.BusinessHoursUtil.parseBranchTime(
                gioDongCua, java.time.LocalTime.of(22, 0));
        java.time.LocalDateTime startLocal = nowHcm.toLocalDateTime();

        if (!com.wms.util.BusinessHoursUtil.isStartWithinBusinessHours(startLocal, openLocalTime, closeLocalTime)) {
            boolean chuaMoCua = !com.wms.util.BusinessHoursUtil.isOpen24h(openLocalTime, closeLocalTime)
                    && !com.wms.util.BusinessHoursUtil.isOvernight(openLocalTime, closeLocalTime)
                    && startLocal.toLocalTime().isBefore(openLocalTime);
            if (chuaMoCua) {
                throw new IllegalArgumentException("Loi: Chi nhanh chua den gio mo cua. Gio mo cua: " + gioMoCua + ".");
            } else {
                throw new IllegalArgumentException("Loi: Chi nhanh da qua gio hoat dong. Khong the mo phien moi.");
            }
        }

        java.time.ZonedDateTime expectedEnd = nowHcm.plusHours(soGioSuDung);

        PhienLamViecDTO phien = new PhienLamViecDTO();
        phien.setMaKH(maKH);
        phien.setMaKG(maKG);
        phien.setTrangThaiPhien("Đang hoạt động");
        phien.setDonGiaTheoGio(donGiaTheoGio);

        phien.setThoiGianBatDau(java.sql.Timestamp.from(nowHcm.toInstant()));
        phien.setThoiGianDuKienKetThuc(java.sql.Timestamp.from(expectedEnd.toInstant()));

        boolean res = phienDAO.taoPhienLamViecMoi(phien);
        if (!res) {
            throw new IllegalArgumentException("Khong the mo phien lam viec: procedure khong tra ve trang thai thanh cong.");
        }
        return true;
    }

    public KetQuaNhanChoDTO nhanChoBangQr(String noiDungQr) {
        if (noiDungQr == null || noiDungQr.isBlank()) {
            return new KetQuaNhanChoDTO(false, "Vui lòng nhập hoặc dán nội dung QR nhận chỗ.");
        }
        String maDatCho = tachMaDatCho(noiDungQr);
        if (maDatCho == null) {
            return new KetQuaNhanChoDTO(false, "Không tìm thấy mã đặt chỗ trong nội dung QR.");
        }
        return phienDAO.moPhienTuQrDatCho(maDatCho, noiDungQr.trim());
    }

    public boolean xoaPhien(String maPhien) {
        return phienDAO.xoaPhien(maPhien);
    }

    public void tuDongKetThucPhienQuaHanDatCho() {
        phienDAO.tuDongKetThucPhienQuaHanDatCho();
    }



    private void guiEmailXacNhanDatCho(String maDatCho, String maPhien) {
        ThongTinXacNhanDatChoDTO thongTin = phienDAO.layThongTinXacNhanDatCho(maDatCho, maPhien);
        if (thongTin == null) {
            return;
        }
        if (thongTin.getEmail() == null || thongTin.getEmail().isBlank()) {
            System.err.println("[PhienLamViecService] Khách hàng không có email để gửi xác nhận đặt chỗ.");
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
                thongTin.getMaQR(),
                qrPng
        );
        if (!sent) {
            System.err.println("[PhienLamViecService] Đã xác nhận thanh toán nhưng chưa gửi được email cho "
                    + thongTin.getEmail());
        }
    }

    private String dinhDangKhoangThoiGian(ThongTinXacNhanDatChoDTO thongTin) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if (thongTin.getThoiGianBatDau() == null) {
            return "Chưa có";
        }
        String start = formatter.format(thongTin.getThoiGianBatDau());
        if (thongTin.getThoiGianDuKienKetThuc() == null) {
            return start;
        }
        return start + " - " + formatter.format(thongTin.getThoiGianDuKienKetThuc());
    }

    private String dinhDangTien(BigDecimal value) {
        if (value == null) {
            return "0 VNĐ";
        }
        return com.wms.util.InputFormatUtil.formatThousands(value) + " VNĐ";
    }

    private String tachMaDatCho(String noiDungQr) {
        String[] parts = noiDungQr.split("\\|");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.regionMatches(true, 0, "DATCHO=", 0, "DATCHO=".length())) {
                String value = trimmed.substring("DATCHO=".length()).trim();
                return value.isBlank() ? null : value.toUpperCase();
            }
        }
        Matcher matcher = MA_DAT_CHO_PATTERN.matcher(noiDungQr.toUpperCase());
        return matcher.find() ? matcher.group().toUpperCase() : null;
    }
}
