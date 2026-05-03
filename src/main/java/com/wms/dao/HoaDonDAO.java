package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.HoaDonDTO;
import com.wms.model.ThongTinHoaDonDTO;
import com.wms.model.DichVuDaDungDTO;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    public List<HoaDonDTO> layDanhSachHoaDon(String searchText, String statusFilter) {
        List<HoaDonDTO> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) return list;

        StringBuilder sql = new StringBuilder(
            "SELECT h.*, kh.HoTenKH " +
            "FROM HOADON h " +
            "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
            "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
            "WHERE 1=1 "
        );

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append("AND (h.MaHoaDon LIKE ? OR kh.HoTenKH LIKE ? OR h.SoHD LIKE ?) ");
        }
        if (statusFilter != null && !statusFilter.equals("Tất cả")) {
            sql.append("AND h.TrangThaiThanhToan = ? ");
        }
        sql.append("ORDER BY h.NgayLapHoaDon DESC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (searchText != null && !searchText.trim().isEmpty()) {
                String q = "%" + searchText.trim() + "%";
                ps.setString(idx++, q);
                ps.setString(idx++, q);
                ps.setString(idx++, q);
            }
            if (statusFilter != null && !statusFilter.equals("Tất cả")) {
                ps.setString(idx++, statusFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoaDonDTO hd = new HoaDonDTO();
                    hd.setMaHoaDon(rs.getString("MaHoaDon"));
                    hd.setSoHD(rs.getString("SoHD"));
                    hd.setTongTien(rs.getDouble("TongTien"));
                    hd.setThanhTien(rs.getDouble("ThanhTien"));
                    hd.setNgayLapHoaDon(rs.getTimestamp("NgayLapHoaDon"));
                    hd.setPhuongThucThanhToan(rs.getString("PhuongThucThanhToan"));
                    hd.setTrangThaiThanhToan(rs.getString("TrangThaiThanhToan"));
                    hd.setMaPhien(rs.getString("MaPhien"));
                    hd.setMaPGG(rs.getString("MaPGG"));
                    hd.setMaNV(rs.getString("MaNV"));
                    hd.setHoTenKH(rs.getString("HoTenKH"));
                    list.add(hd);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Hàm lưu hóa đơn đang chờ thanh toán vào Database
    public boolean taoHoaDonMoi(HoaDonDTO hd) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) return false;

        String sql = "INSERT INTO HOADON (MaHoaDon, SoHD, TongTien, ThanhTien, NgayLapHoaDon, TrangThaiThanhToan, MaPhien) " +
                     "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hd.getMaHoaDon());
            pstmt.setString(2, hd.getSoHD());
            pstmt.setDouble(3, hd.getTongTien());
            pstmt.setDouble(4, hd.getThanhTien());
            pstmt.setString(5, hd.getTrangThaiThanhToan());
            pstmt.setString(6, hd.getMaPhien());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ThongTinHoaDonDTO layThongTinChiTietHoaDon(String maHoaDon) {
        ThongTinHoaDonDTO thongTin = null;
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) return null;

        String sqlChung = "SELECT h.MaHoaDon, h.TongTien, h.ThanhTien, p.MaPhien, " +
                          "p.ThoiGianBatDau, p.ThoiGianKetThuc, " +
                          "kh.HoTenKH, kg.TenKG " +
                          "FROM HOADON h " +
                          "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                          "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                          "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                          "WHERE h.MaHoaDon = ?";

        String sqlDichVu = "SELECT dv.TenDV, ct.SoLuong, dv.DonGia " +
                           "FROM CHITIETDICHVU ct " +
                           "JOIN DICHVU dv ON ct.MaDV = dv.MaDV " +
                           "WHERE ct.MaPhien = ?";

        try (PreparedStatement psChung = conn.prepareStatement(sqlChung)) {
            psChung.setString(1, maHoaDon);
            try (ResultSet rsChung = psChung.executeQuery()) {
                if (rsChung.next()) {
                    thongTin = new ThongTinHoaDonDTO();
                    thongTin.setMaHoaDon(rsChung.getString("MaHoaDon"));
                    thongTin.setHoTenKH(rsChung.getString("HoTenKH"));
                    thongTin.setTenKhongGian(rsChung.getString("TenKG"));
                    thongTin.setTongTien(rsChung.getDouble("TongTien"));
                    thongTin.setThanhTien(rsChung.getDouble("ThanhTien"));

                    String maPhien = rsChung.getString("MaPhien");
                    Timestamp tBĐ = rsChung.getTimestamp("ThoiGianBatDau");
                    Timestamp tKT = rsChung.getTimestamp("ThoiGianKetThuc");

                    if (tBĐ != null && tKT != null) {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        String chuoiThoiGian = timeFormat.format(tBĐ) + " - " + timeFormat.format(tKT) + " (" + dateFormat.format(tBĐ) + ")";
                        thongTin.setThoiGianSửDung(chuoiThoiGian);
                        
                        long diffMillis = tKT.getTime() - tBĐ.getTime();
                        double soGio = (double) diffMillis / (1000 * 60 * 60);
                        thongTin.setTongSoGio(Math.round(soGio * 10.0) / 10.0);
                    }

                    try (PreparedStatement psDV = conn.prepareStatement(sqlDichVu)) {
                        psDV.setString(1, maPhien);
                        try (ResultSet rsDV = psDV.executeQuery()) {
                            while (rsDV.next()) {
                                DichVuDaDungDTO dv = new DichVuDaDungDTO(
                                    rsDV.getString("TenDV"),
                                    rsDV.getInt("SoLuong"),
                                    rsDV.getDouble("DonGia")
                                );
                                thongTin.getDanhSachDichVu().add(dv);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return thongTin;
    }

    public boolean xacNhanThanhToan(String maHoaDon, String phuongThucThanhToan, String maNV, String maPGG, double thanhTien) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) return false;

        String sql = "UPDATE HOADON SET PhuongThucThanhToan = ?, MaNV = ?, MaPGG = ?, ThanhTien = ?, TrangThaiThanhToan = 'Đã thanh toán', NgayLapHoaDon = CURRENT_TIMESTAMP WHERE MaHoaDon = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phuongThucThanhToan);
            pstmt.setString(2, maNV);
            pstmt.setString(3, maPGG);
            pstmt.setDouble(4, thanhTien);
            pstmt.setString(5, maHoaDon);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean huyHoaDon(String maHoaDon) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) return false;

        String sql = "UPDATE HOADON SET TrangThaiThanhToan = 'Đã hủy' WHERE MaHoaDon = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHoaDon);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoaHoaDon(String maHoaDon) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) return false;

        String sql = "DELETE FROM HOADON WHERE MaHoaDon = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHoaDon);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
