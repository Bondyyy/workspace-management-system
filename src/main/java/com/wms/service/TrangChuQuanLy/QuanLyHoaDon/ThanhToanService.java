package com.wms.service.TrangChuQuanLy.QuanLyHoaDon;

import com.wms.controller.TrangChuGioiThieu.DangNhapController;
import com.wms.dao.TrangChuQuanLy.QuanLyHoaDon.HoaDonDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyNhanVien.NhanVienDAO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.KetQuaThanhToanDTO;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;

public class ThanhToanService {

    public static final String THONG_BAO_DA_TRA_TRUOC = "Hóa đơn này đã được thanh toán trước qua đặt chỗ.";

    private final HoaDonDAO hoaDonDAO;
    private final NhanVienDAO nhanVienDAO;

    public ThanhToanService() {
        this.hoaDonDAO = new HoaDonDAO();
        this.nhanVienDAO = new NhanVienDAO();
    }

    public ThongTinHoaDonDTO layChiTietHoaDon(String maHoaDon) {
        return hoaDonDAO.layThongTinChiTietHoaDon(maHoaDon);
    }

    public String thucHienThanhToan(String maHoaDon, String phuongThuc, String maPGG, double thanhTien) {
        String maNV = layMaNhanVienDangNhap();
        ThongTinHoaDonDTO thongTin = hoaDonDAO.layThongTinChiTietHoaDon(maHoaDon);

        if (thongTin != null && daTraTruocKhongConPhaiThu(thongTin)) {
            return THONG_BAO_DA_TRA_TRUOC;
        }

        if (thongTin != null && thongTin.getMaPhien() != null) {
            try {
                return hoaDonDAO.thanhToanVoiPhieuGiamGia(thongTin.getMaPhien(), maNV, maPGG, phuongThuc)
                        ? null
                        : "Lỗi khi cập nhật thanh toán";
            } catch (IllegalStateException ex) {
                return ex.getMessage();
            }
        }

        return hoaDonDAO.xacNhanThanhToan(maHoaDon, phuongThuc, maNV, maPGG, thanhTien)
                ? null
                : "Lỗi khi cập nhật thanh toán";
    }

    public KetQuaThanhToanDTO thucHienThanhToanMoi(String maHoaDon, String phuongThuc, String maPGG, double thanhTien) {
        long start = System.currentTimeMillis();
        String maNV = layMaNhanVienDangNhap();
        ThongTinHoaDonDTO thongTin = hoaDonDAO.layThongTinChiTietHoaDon(maHoaDon);

        try {
            if (thongTin != null && daTraTruocKhongConPhaiThu(thongTin)) {
                return new KetQuaThanhToanDTO(false, THONG_BAO_DA_TRA_TRUOC);
            }

            if (thongTin != null && thongTin.getMaPhien() != null) {
                return hoaDonDAO.thanhToanVoiPhieuGiamGiaMoi(thongTin.getMaPhien(), maNV, maPGG, phuongThuc);
            }

            return hoaDonDAO.thanhToanTrucTiepMoi(maHoaDon, phuongThuc, maNV, maPGG, thanhTien);
        } finally {
            System.out.println("[ThanhToanService] thanh toan hoa don " + maHoaDon + " mat "
                    + (System.currentTimeMillis() - start) + " ms");
        }
    }

    private boolean daTraTruocKhongConPhaiThu(ThongTinHoaDonDTO thongTin) {
        if (thongTin.getSoTienDaTraTruoc() <= 0) {
            return false;
        }
        return thongTin.getThanhTien() <= 0;
    }

    private String layMaNhanVienDangNhap() {
        NguoiDungDTO user = DangNhapController.getCurrentUser();
        return (user != null && user.getMaND() != null) ? nhanVienDAO.layMaNVTuMaND(user.getMaND()) : null;
    }
}
