package com.wms.controller;

import com.wms.model.ThanhToan_KhuyenMai.XacNhanPhieuGiamGiaDTO;
import com.wms.model.ThanhToan_KhuyenMai.ThongTinHoaDonDTO;
import com.wms.service.ThanhToanService;
import com.wms.service.XacNhanPhieuGiamGiaService;

public class ThanhToanController {
    
    private final ThanhToanService thanhToanService;
    private final XacNhanPhieuGiamGiaService xacNhanPhieuGiamGiaService;

    public ThanhToanController() {
        this.thanhToanService = new ThanhToanService();
        this.xacNhanPhieuGiamGiaService = new XacNhanPhieuGiamGiaService();
    }

    // View sẽ gọi hàm này lúc mới mở form lên để lấy dữ liệu
    public ThongTinHoaDonDTO loadDuLieuHoaDon(String maHoaDon) {
        return thanhToanService.layChiTietHoaDon(maHoaDon);
    }

    // View sẽ gọi hàm này khi người dùng bấm xác nhận ở form Tiền Mặt / Chuyển Khoản
    public boolean xacNhanThanhToan(String maHoaDon, String phuongThuc) {
        if (maHoaDon == null || maHoaDon.isEmpty()) {
            return false;
        }
        return thanhToanService.thucHienThanhToan(maHoaDon, phuongThuc);
    }
    
    /**
     * Kiểm tra phiếu giảm giá từ backend
     * 
     * @param maChuSoPGG Mã phiếu giảm giá
     * @param tongTienGoc Tổng tiền gốc
     * @return XacNhanPhieuGiamGiaDTO chứa kết quả kiểm tra
     */
    public XacNhanPhieuGiamGiaDTO kiemTraPhieuGiamGia(String maChuSoPGG, double tongTienGoc) {
        return xacNhanPhieuGiamGiaService.kiemTraPhieuGiamGia(maChuSoPGG, tongTienGoc);
    }
    
    /**
     * Tính lại thành tiền sau khi áp dụng voucher
     * 
     * @param tongTienGoc Tổng tiền gốc
     * @param discountAmount Số tiền giảm
     * @return Thành tiền cuối cùng
     */
    public double tinhThanhTien(double tongTienGoc, double discountAmount) {
        return xacNhanPhieuGiamGiaService.tinhThanhTien(tongTienGoc, discountAmount);
    }
}