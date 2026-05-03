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
        String sql = "INSERT INTO PHIENLAMVIEC (MaPhien, ThoiGianBatDau, ThoiGianDuKienKetThuc, TrangThaiPhien, MaKG, MaKH, CapNhatLanCuoi) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

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

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PhienLamViecDAO] Lỗi khi tạo phiên làm việc mới: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<PhienLamViecFullDTO> layDanhSachPhien(String keyword) {
        List<PhienLamViecFullDTO> list = new ArrayList<>();
        String sql = "SELECT p.*, kg.TenKG, kh.HoTenKH " +
                "FROM PHIENLAMVIEC p " +
                "JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                "JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                "WHERE p.MaPhien LIKE ? OR kh.HoTenKH LIKE ? OR kg.TenKG LIKE ? " +
                "ORDER BY p.ThoiGianBatDau DESC";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return list;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String search = "%" + keyword + "%";
            pstmt.setString(1, search);
            pstmt.setString(2, search);
            pstmt.setString(3, search);

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
                    dto.setTenKhongGian(rs.getString("TenKG"));
                    dto.setTenKhachHang(rs.getString("HoTenKH"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean ketThucPhien(String maPhien) {
        String sql = "UPDATE PHIENLAMVIEC SET ThoiGianKetThuc = CURRENT_TIMESTAMP, TrangThaiPhien = 'Hoàn thành' WHERE MaPhien = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return false;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maPhien);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
