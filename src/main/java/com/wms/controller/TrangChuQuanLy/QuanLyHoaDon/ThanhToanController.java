package com.wms.controller.TrangChuQuanLy.QuanLyHoaDon;

import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.XacNhanPhieuGiamGiaDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.KetQuaThanhToanDTO;
import com.wms.service.TrangChuQuanLy.QuanLyHoaDon.ThanhToanService;
import com.wms.service.TrangChuQuanLy.QuanLyHoaDon.XacNhanPhieuGiamGiaService;

public class ThanhToanController {
    
    private final ThanhToanService thanhToanService;
    private final XacNhanPhieuGiamGiaService xacNhanPhieuGiamGiaService;

    public ThanhToanController() {
        this.thanhToanService = new ThanhToanService();
        this.xacNhanPhieuGiamGiaService = new XacNhanPhieuGiamGiaService();
    }

    public ThongTinHoaDonDTO loadDuLieuHoaDon(String maHoaDon) {
        return thanhToanService.layChiTietHoaDon(maHoaDon);
    }

    public boolean xacNhanThanhToan(String maHoaDon, String phuongThuc, String maPGG, double thanhTien) {
        String ketQua = thanhToanService.thucHienThanhToan(maHoaDon, phuongThuc, maPGG, thanhTien);
        return ketQua == null;
    }

    public KetQuaThanhToanDTO thucHienThanhToanMoi(String maHoaDon, String phuongThuc, String maPGG, double thanhTien) {
        return thanhToanService.thucHienThanhToanMoi(maHoaDon, phuongThuc, maPGG, thanhTien);
    }

    public String thucHienThanhToanCoThongBao(String maHoaDon, String phuongThuc, String maPGG, double thanhTien) {
        return thanhToanService.thucHienThanhToan(maHoaDon, phuongThuc, maPGG, thanhTien);
    }
    
    public XacNhanPhieuGiamGiaDTO kiemTraPhieuGiamGia(String maChuSoPGG, double tongTienGoc) {
        return xacNhanPhieuGiamGiaService.kiemTraPhieuGiamGia(maChuSoPGG, tongTienGoc);
    }
    
    public double tinhThanhTien(double tongTienGoc, double discountAmount) {
        return xacNhanPhieuGiamGiaService.tinhThanhTien(tongTienGoc, discountAmount);
    }
}