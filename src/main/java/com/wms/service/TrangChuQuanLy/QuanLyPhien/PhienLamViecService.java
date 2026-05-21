package com.wms.service.TrangChuQuanLy.QuanLyPhien;

import com.wms.dao.TrangChuQuanLy.QuanLyPhien.DatChoDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyHoaDon.HoaDonDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyHoiVien.KhachHangDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyKhongGian.KhongGianDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyNhanVien.NhanVienDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyPhien.PhienLamViecDAO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.DichVuTrongPhienDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoiVien.HoiVienDTO;
import com.wms.model.TrangChuQuanLy.QuanLyKhongGian.KhongGianDTO;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.ThongTinXacNhanDatChoDTO;
import com.wms.util.EmailUtil;
import com.wms.util.MaQRUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class PhienLamViecService {

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
        return phienDAO.ketThucPhien(maPhien);
    }

    public boolean xacNhanThanhToanDatTruoc(String maDatCho, String maPhien) {
        String maQR = MaQRUtil.taoMaQRPhien(maPhien, maDatCho);
        boolean dcOk = datChoDAO.xacNhanThanhToan(maDatCho);
        boolean qrOk = dcOk && datChoDAO.capNhatMaQR(maDatCho, maQR);
        boolean hdOk = hoaDonDAO.capNhatTrangThaiThanhToanTheoPhien(maPhien, "Đã thanh toán thành công");
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

    public boolean taoPhienMoi(String maKH, String maKG, int soGioSuDung, double donGiaTheoGio) {
        PhienLamViecDTO phien = new PhienLamViecDTO();
        phien.setMaKH(maKH);
        phien.setMaKG(maKG);
        phien.setTrangThaiPhien("Đang hoạt động");
        phien.setDonGiaTheoGio(donGiaTheoGio);

        long now = System.currentTimeMillis();
        phien.setThoiGianBatDau(new java.sql.Timestamp(now));

        long durationMillis = (long) soGioSuDung * 3600 * 1000;
        phien.setThoiGianDuKienKetThuc(new java.sql.Timestamp(now + durationMillis));

        return phienDAO.taoPhienLamViecMoi(phien);
    }

    public boolean xoaPhien(String maPhien) {
        return phienDAO.xoaPhien(maPhien);
    }

    public boolean capNhatPhien(String maPhien, String trangThai, String tenKH) {
        return phienDAO.capNhatPhien(maPhien, trangThai, tenKH);
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
                qrPng
        );
        if (!sent) {
            System.err.println("[PhienLamViecService] Đã xác nhận thanh toán nhưng chưa gửi được email cho "
                    + thongTin.getEmail());
        }
    }

    private String dinhDangKhoangThoiGian(ThongTinXacNhanDatChoDTO thongTin) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd/MM/yyyy");
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
        return new DecimalFormat("#,### VNĐ").format(value);
    }
}
