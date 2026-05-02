package com.wms.service;

import com.wms.dao.HoaDonDAO;
import com.wms.dao.NhanVienDAO;
import com.wms.model.ThanhToan_KhuyenMai.ThongTinHoaDonDTO;
import com.wms.controller.DangNhapController;
import com.wms.model.NhanSu_KhachHang.NguoiDungDTO;

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
    public boolean thucHienThanhToan(String maHoaDon, String phuongThuc) {
        String maNV = layMaNhanVienDangNhap();
        
        // Gọi DAO để update (Trigger trong DB sẽ lo việc đổi Trạng Thái thành 'Đã thanh toán thành công')
        return hoaDonDAO.xacNhanThanhToan(maHoaDon, phuongThuc, maNV);
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