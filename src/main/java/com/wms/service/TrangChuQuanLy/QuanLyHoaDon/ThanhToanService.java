package com.wms.service.TrangChuQuanLy.QuanLyHoaDon;

import com.wms.controller.TrangChuGioiThieu.DangNhapController;
import com.wms.dao.TrangChuQuanLy.QuanLyPhieuGiamGia.PhieuGiamGiaDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyHoaDon.HoaDonDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyNhanVien.NhanVienDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyPhien.PhienLamViecDAO;
import com.wms.model.TrangChuGioiThieu.NguoiDungDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;

public class ThanhToanService {

    private final HoaDonDAO hoaDonDAO;
    private final NhanVienDAO nhanVienDAO;
    private final PhienLamViecDAO phienDAO;
    private final PhieuGiamGiaDAO phieuGiamGiaDAO;

    public ThanhToanService() {
        this.hoaDonDAO = new HoaDonDAO();
        this.nhanVienDAO = new NhanVienDAO();
        this.phienDAO = new PhienLamViecDAO();
        this.phieuGiamGiaDAO = new PhieuGiamGiaDAO();
    }

    public ThongTinHoaDonDTO layChiTietHoaDon(String maHoaDon) {
        return hoaDonDAO.layThongTinChiTietHoaDon(maHoaDon);
    }

    public boolean thucHienThanhToan(String maHoaDon, String phuongThuc, String maPGG, double thanhTien) {
        ThongTinHoaDonDTO thongTin = hoaDonDAO.layThongTinChiTietHoaDon(maHoaDon);
        if (thongTin != null && thongTin.getMaPhien() != null) {
            if (!"Đã kết thúc".equals(thongTin.getTrangThaiPhien())) {
                boolean endSuccess = phienDAO.ketThucPhien(thongTin.getMaPhien());
                if (!endSuccess) {
                    return false;
                }
            }
        }

        String maNV = layMaNhanVienDangNhap();
        boolean updated = hoaDonDAO.xacNhanThanhToan(maHoaDon, phuongThuc, maNV, maPGG, thanhTien);

        if (updated && maPGG != null && !maPGG.isEmpty()) {
            phieuGiamGiaDAO.tangSoLuongDaDung(maPGG);
        }

        return updated;
    }

    private String layMaNhanVienDangNhap() {
        NguoiDungDTO user = DangNhapController.getCurrentUser();
        return (user != null && user.getMaND() != null) ? nhanVienDAO.layMaNVTuMaND(user.getMaND()) : null;
    }
}
