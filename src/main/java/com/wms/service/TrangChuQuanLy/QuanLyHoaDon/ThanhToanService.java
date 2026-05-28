package com.wms.service.TrangChuQuanLy.QuanLyHoaDon;

import com.wms.controller.TrangChuGioiThieu.DangNhapController;
import com.wms.config.DatabaseConnection;
import com.wms.dao.TrangChuQuanLy.QuanLyHoaDon.HoaDonDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyNhanVien.NhanVienDAO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.PhienGiaoDichThanhToan;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.KetQuaThanhToanDTO;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;
import java.sql.Connection;
import java.sql.SQLException;

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

    public String thucHienThanhToan(String maHoaDon, String phuongThuc, String maPGG, double thanhTien) {
        String maNV = layMaNhanVienDangNhap();
        ThongTinHoaDonDTO thongTin = hoaDonDAO.layThongTinChiTietHoaDon(maHoaDon);

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
            if (thongTin != null && thongTin.getMaPhien() != null) {
                return hoaDonDAO.thanhToanVoiPhieuGiamGiaMoi(thongTin.getMaPhien(), maNV, maPGG, phuongThuc);
            }

            return hoaDonDAO.thanhToanTrucTiepMoi(maHoaDon, phuongThuc, maNV, maPGG, thanhTien);
        } finally {
            System.out.println("[ThanhToanService] thanh toan hoa don " + maHoaDon + " mat "
                    + (System.currentTimeMillis() - start) + " ms");
        }
    }

    public PhienGiaoDichThanhToan batDauGiaoDichThanhToan(String maHoaDon, boolean serializable) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(serializable
                    ? Connection.TRANSACTION_SERIALIZABLE
                    : Connection.TRANSACTION_READ_COMMITTED);

            ThongTinHoaDonDTO hoaDonLan1 = hoaDonDAO.layThongTinChiTietHoaDon(conn, maHoaDon);
            if (hoaDonLan1 == null) {
                rollbackAnToan(conn);
                closeAnToan(conn);
                throw new IllegalStateException("Không tìm thấy hóa đơn: " + maHoaDon);
            }

            logBatDauGiaoDich(conn, hoaDonLan1);
            return new PhienGiaoDichThanhToan(conn, maHoaDon, hoaDonLan1, serializable);
        } catch (Exception ex) {
            rollbackAnToan(conn);
            closeAnToan(conn);
            throw new IllegalStateException("Không thể bắt đầu giao dịch thanh toán: " + ex.getMessage(), ex);
        }
    }

    public KetQuaThanhToanDTO xacNhanThanhToanTrongGiaoDich(PhienGiaoDichThanhToan gd,
            String phuongThuc, String maPGG, double thanhTien) {
        if (gd == null || gd.getConnection() == null) {
            return new KetQuaThanhToanDTO(false, "Giao dịch thanh toán không hợp lệ hoặc đã kết thúc");
        }

        Connection conn = gd.getConnection();
        try {
            if (conn.isClosed()) {
                return new KetQuaThanhToanDTO(false, "Giao dịch thanh toán đã đóng");
            }

            String maNV = layMaNhanVienDangNhap();
            ThongTinHoaDonDTO lan1 = gd.getHoaDonLan1();
            ThongTinHoaDonDTO lan2 = hoaDonDAO.layThongTinChiTietHoaDon(conn, gd.getMaHoaDon());
            if (lan2 == null) {
                conn.rollback();
                return new KetQuaThanhToanDTO(false, "Không tìm thấy hóa đơn khi xác nhận thanh toán");
            }

            logXacNhanGiaoDich(conn, gd, lan2);
            if (khacTien(lan1.getTongTien(), lan2.getTongTien())
                    || khacTien(lan1.getThanhTien(), lan2.getThanhTien())) {
                System.out.println("[NON_REPEATABLE_READ_DETECTED] Hoa don bi thay doi trong cung transaction");
            }

            KetQuaThanhToanDTO ketQua;
            if (lan2.getMaPhien() != null && !lan2.getMaPhien().isBlank()) {
                ketQua = hoaDonDAO.thanhToanVoiPhieuGiamGiaMoi(conn, lan2.getMaPhien(), maNV, maPGG, phuongThuc);
            } else {
                ketQua = hoaDonDAO.thanhToanTrucTiepMoi(conn, gd.getMaHoaDon(), phuongThuc, maNV, maPGG, thanhTien);
            }

            if (ketQua != null && ketQua.isSuccess()) {
                conn.commit();
            } else {
                conn.rollback();
            }
            return ketQua != null ? ketQua : new KetQuaThanhToanDTO(false, "Lỗi khi cập nhật thanh toán");
        } catch (Exception ex) {
            rollbackAnToan(conn);
            return new KetQuaThanhToanDTO(false, "Lỗi khi cập nhật thanh toán: " + ex.getMessage());
        } finally {
            closeAnToan(conn);
        }
    }

    public void huyGiaoDichThanhToan(PhienGiaoDichThanhToan gd) {
        if (gd == null || gd.getConnection() == null) {
            return;
        }
        Connection conn = gd.getConnection();
        try {
            if (!conn.isClosed()) {
                conn.rollback();
                System.out.println("[TX PAYMENT CANCEL] rollback transaction maHD=" + gd.getMaHoaDon());
            }
        } catch (Exception ex) {
            System.err.println("[TX PAYMENT CANCEL] rollback loi maHD=" + gd.getMaHoaDon() + ": " + ex.getMessage());
        } finally {
            closeAnToan(conn);
        }
    }

    private String layMaNhanVienDangNhap() {
        NguoiDungDTO user = DangNhapController.getCurrentUser();
        return (user != null && user.getMaND() != null) ? nhanVienDAO.layMaNVTuMaND(user.getMaND()) : null;
    }

    private void logBatDauGiaoDich(Connection conn, ThongTinHoaDonDTO hoaDon) throws SQLException {
        System.out.println("[TX PAYMENT START] maHD=" + hoaDon.getMaHoaDon()
                + ", connHash=" + System.identityHashCode(conn)
                + ", autoCommit=" + conn.getAutoCommit()
                + ", isolation=" + conn.getTransactionIsolation()
                + ", tongTienLan1=" + hoaDon.getTongTien()
                + ", thanhTienLan1=" + hoaDon.getThanhTien());
    }

    private void logXacNhanGiaoDich(Connection conn, PhienGiaoDichThanhToan gd,
            ThongTinHoaDonDTO lan2) throws SQLException {
        ThongTinHoaDonDTO lan1 = gd.getHoaDonLan1();
        System.out.println("[TX PAYMENT CONFIRM] maHD=" + gd.getMaHoaDon()
                + ", connHash=" + System.identityHashCode(conn)
                + ", autoCommit=" + conn.getAutoCommit()
                + ", isolation=" + conn.getTransactionIsolation()
                + ", tongTienLan1=" + lan1.getTongTien()
                + ", tongTienLan2=" + lan2.getTongTien()
                + ", thanhTienLan1=" + lan1.getThanhTien()
                + ", thanhTienLan2=" + lan2.getThanhTien());
    }

    private boolean khacTien(double a, double b) {
        return Math.round(a) != Math.round(b);
    }

    private void rollbackAnToan(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            if (!conn.isClosed()) {
                conn.rollback();
            }
        } catch (Exception ex) {
            System.err.println("[ThanhToanService] rollback loi: " + ex.getMessage());
        }
    }

    private void closeAnToan(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            if (!conn.isClosed()) {
                conn.close();
            }
        } catch (Exception ex) {
            System.err.println("[ThanhToanService] close connection loi: " + ex.getMessage());
        }
    }
}
