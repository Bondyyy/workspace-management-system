package com.wms.service;

import com.wms.controller.DangNhapController;

import com.wms.dao.PhieuGiamGiaDAO;

import com.wms.dao.HoaDonDAO;

import com.wms.dao.NhanVienDAO;
import com.wms.model.NguoiDungDTO;
import com.wms.model.ThongTinHoaDonDTO;

public class ThanhToanService {
    
    private final HoaDonDAO hoaDonDAO;
    private final NhanVienDAO nhanVienDAO;

    public ThanhToanService() {
        this.hoaDonDAO = new HoaDonDAO();
        this.nhanVienDAO = new NhanVienDAO();
    }

    // Lấy dữ liệu đẩy lên View
    public ThongTinHoaDonDTO layChiTietHoaDon(String maHoaDon) {
        return hoaDonDAO.layThongTinChiTietHoaDon(maHoaDon);
    }

    // Thực hiện logic thanh toán
    public boolean thucHienThanhToan(String maHoaDon, String phuongThuc, String maPGG) {
        String maNV = layMaNhanVienDangNhap();
        
        // Gọi DAO để update
        boolean updated = hoaDonDAO.xacNhanThanhToan(maHoaDon, phuongThuc, maNV, maPGG);
        
        if (updated && maPGG != null && !maPGG.isEmpty()) {
            // Tăng số lượng đã dùng của voucher
            new PhieuGiamGiaDAO().tangSoLuongDaDung(maPGG);
        }
        
        return updated;
    }

    // Lấy mã nhân viên từ session đăng nhập
    private String layMaNhanVienDangNhap() {
        NguoiDungDTO user = DangNhapController.getCurrentUser();
        if (user != null && user.getMaND() != null) {
            return nhanVienDAO.layMaNVTuMaND(user.getMaND());
        }
        return null; // Nếu test chưa đăng nhập
    }
}





