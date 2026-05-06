package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.DichVuTrongPhienDTO;
import com.wms.model.PhienLamViecFullDTO;
import com.wms.model.PhienLamViecDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhienLamViecDAO {

    public boolean taoPhienLamViecMoi(PhienLamViecDTO phien) {
        String sql = "INSERT INTO PHIENLAMVIEC (MaPhien, ThoiGianBatDau, ThoiGianDuKienKetThuc, TrangThaiPhien, MaKG, MaKH, MaDatCho, CapNhatLanCuoi) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return false;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phien.getMaPhien());
            pstmt.setTimestamp(2, phien.getThoiGianBatDau());
            pstmt.setTimestamp(3, phien.getThoiGianDuKienKetThuc());
            pstmt.setString(4, phien.getTrangThaiPhien());
            pstmt.setString(5, phien.getMaKG());
            pstmt.setString(6, phien.getMaKH());
            pstmt.setString(7, phien.getMaDatCho());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PhienLamViecDAO] Lỗi khi tạo phiên làm việc mới: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<PhienLamViecFullDTO> layDanhSachPhien(String keyword, String maCN) {
        List<PhienLamViecFullDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, kg.TenKG, kh.HoTenKH, dc.TrangThaiDatTruoc, h.TrangThaiThanhToan " +
                "FROM PHIENLAMVIEC p " +
                "JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                "JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                "LEFT JOIN DATCHO dc ON p.MaDatCho = dc.MaDatCho " +
                "LEFT JOIN HOADON h ON p.MaPhien = h.MaPhien " +
                "WHERE (p.MaPhien LIKE ? OR kh.HoTenKH LIKE ? OR kg.TenKG LIKE ?)");

        if (maCN != null && !maCN.isEmpty()) {
            sql.append(" AND kg.MaCN = ?");
        }

        sql.append(" ORDER BY p.ThoiGianBatDau DESC");

        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return list;

        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            String search = "%" + keyword + "%";
            pstmt.setString(1, search);
            pstmt.setString(2, search);
            pstmt.setString(3, search);
            if (maCN != null && !maCN.isEmpty()) {
                pstmt.setString(4, maCN);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PhienLamViecFullDTO dto = new PhienLamViecFullDTO();
                    dto.setMaPhien(rs.getString("MaPhien"));
                    dto.setThoiGianBatDau(rs.getTimestamp("ThoiGianBatDau"));
                    dto.setThoiGianDuKienKetThuc(rs.getTimestamp("ThoiGianDuKienKetThuc"));
                    dto.setThoiGianKetThuc(rs.getTimestamp("ThoiGianKetThuc"));
                    dto.setTrangThaiPhien(rs.getString("TrangThaiPhien"));
                    dto.setMaKG(rs.getString("MaKG"));
                    dto.setMaKH(rs.getString("MaKH"));
                    dto.setMaDatCho(rs.getString("MaDatCho"));
                    dto.setTenKhongGian(rs.getString("TenKG"));
                    dto.setTenKhachHang(rs.getString("HoTenKH"));
                    dto.setTrangThaiDatCho(rs.getString("TrangThaiDatTruoc"));
                    dto.setTrangThaiThanhToan(rs.getString("TrangThaiThanhToan"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean ketThucPhien(String maPhien) {
        String sqlPhien = "UPDATE PHIENLAMVIEC SET ThoiGianKetThuc = CURRENT_TIMESTAMP, TrangThaiPhien = 'Đã kết thúc' WHERE MaPhien = ?";
        String sqlHoaDon = "UPDATE HOADON SET TongTien = 0 WHERE MaPhien = ?"; // Kích hoạt Trigger tính toán

        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return false;

        try {
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Cập nhật phiên làm việc
            try (PreparedStatement pstmtPhien = conn.prepareStatement(sqlPhien)) {
                pstmtPhien.setString(1, maPhien);
                if (pstmtPhien.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 2. Cú hích để cập nhật hóa đơn (Kích hoạt Trigger tính tiền)
            try (PreparedStatement pstmtHoaDon = conn.prepareStatement(sqlHoaDon)) {
                pstmtHoaDon.setString(1, maPhien);
                pstmtHoaDon.executeUpdate(); // Có thể có phiên không có hóa đơn (ví dụ khách vãng lai không tính tiền) nên không cần check > 0
            }

            conn.commit(); // Hoàn tất
            return true;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<DichVuTrongPhienDTO> layDichVuCuaPhien(String maPhien) {
        List<DichVuTrongPhienDTO> list = new ArrayList<>();
        String sql = "SELECT dv.TenDV, ct.SoLuong, dv.DonGia " +
                "FROM CHITIETDICHVU ct " +
                "JOIN DICHVU dv ON ct.MaDV = dv.MaDV " +
                "WHERE ct.MaPhien = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return list;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maPhien);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String ten = rs.getString("TenDV");
                    int sl = rs.getInt("SoLuong");
                    double dg = rs.getDouble("DonGia");
                    list.add(new DichVuTrongPhienDTO(ten, sl, dg, sl * dg));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
