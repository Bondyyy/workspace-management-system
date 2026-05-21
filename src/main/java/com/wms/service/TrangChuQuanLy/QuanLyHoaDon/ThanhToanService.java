package com.wms.service.TrangChuQuanLy.QuanLyHoaDon;

import com.wms.controller.TrangChuGioiThieu.DangNhapController;
import com.wms.dao.TrangChuQuanLy.QuanLyHoaDon.HoaDonDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyNhanVien.NhanVienDAO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;

public class ThanhToanService {

    private final HoaDonDAO hoaDonDAO;
    private final NhanVienDAO nhanVienDAO;

    public ThanhToanService() {
        this.hoaDonDAO = new HoaDonDAO();
        this.nhanVienDAO = new NhanVienDAO();
    }

    public ThongTinHoaDonDTO layChiTietHoaDon(String maHoaDon) {
        return hoaDonDAO.layThongTinChiTietHoaDon(maHoaDon);
    }

    public boolean thucHienThanhToan(String maHoaDon, String phuongThuc, String maPGG, double thanhTien) {
        String maNV = layMaNhanVienDangNhap();
        ThongTinHoaDonDTO thongTin = hoaDonDAO.layThongTinChiTietHoaDon(maHoaDon);

        if (thongTin != null && thongTin.getMaPhien() != null) {
            return hoaDonDAO.thanhToanVoiPhieuGiamGia(thongTin.getMaPhien(), maNV, maPGG, phuongThuc);
        }

        return hoaDonDAO.xacNhanThanhToan(maHoaDon, phuongThuc, maNV, maPGG, thanhTien);
    }

    private String layMaNhanVienDangNhap() {
        NguoiDungDTO user = DangNhapController.getCurrentUser();
        return (user != null && user.getMaND() != null) ? nhanVienDAO.layMaNVTuMaND(user.getMaND()) : null;
    }
}
