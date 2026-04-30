package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.ThanhToan_KhuyenMai.PhieuGiamGiaDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhieuGiamGiaDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<PhieuGiamGiaDTO> layDanhSach() {
        List<PhieuGiamGiaDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM PHIEUGIAMGIA ORDER BY NgayTaoPGG DESC";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi lấy danh sách: " + e.getMessage());
        }
        return list;
    }

    public boolean themMoi(PhieuGiamGiaDTO dto) {
        String sql = "INSERT INTO PHIEUGIAMGIA (MaPGG, MaChuSoPGG, GiaTriGiamGia, GiaTriApDungToiThieu, " +
                     "NgayBatDauApDung, NgayKetThucApDung, SLDaDung, SLToiDa, NgayTaoPGG, MaNV) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, ?)";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaPGG());
            ps.setString(2, dto.getMaChuSoPGG());
            ps.setDouble(3, dto.getGiaTriGiamGia());
            ps.setDouble(4, dto.getGiaTriApDungToiThieu());
            ps.setTimestamp(5, new Timestamp(dto.getNgayBatDauApDung().getTime()));
            ps.setTimestamp(6, new Timestamp(dto.getNgayKetThucApDung().getTime()));
            ps.setInt(7, 0); // SL đã dùng ban đầu là 0
            ps.setInt(8, dto.getSlToiDa());
            ps.setString(9, dto.getMaNV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi thêm mới: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhat(PhieuGiamGiaDTO dto) {
        String sql = "UPDATE PHIEUGIAMGIA SET MaChuSoPGG = ?, GiaTriGiamGia = ?, GiaTriApDungToiThieu = ?, " +
                     "NgayBatDauApDung = ?, NgayKetThucApDung = ?, SLToiDa = ? WHERE MaPGG = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaChuSoPGG());
            ps.setDouble(2, dto.getGiaTriGiamGia());
            ps.setDouble(3, dto.getGiaTriApDungToiThieu());
            ps.setTimestamp(4, new Timestamp(dto.getNgayBatDauApDung().getTime()));
            ps.setTimestamp(5, new Timestamp(dto.getNgayKetThucApDung().getTime()));
            ps.setInt(6, dto.getSlToiDa());
            ps.setString(7, dto.getMaPGG());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi cập nhật: " + e.getMessage());
            return false;
        }
    }

    public boolean xoa(String maPGG) {
        String sql = "DELETE FROM PHIEUGIAMGIA WHERE MaPGG = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPGG);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi xóa: " + e.getMessage());
            return false;
        }
    }

    public PhieuGiamGiaDTO timTheoMa(String maPGG) {
        String sql = "SELECT * FROM PHIEUGIAMGIA WHERE MaPGG = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPGG);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi tìm theo mã: " + e.getMessage());
        }
        return null;
    }

    private PhieuGiamGiaDTO mapRow(ResultSet rs) throws SQLException {
        PhieuGiamGiaDTO dto = new PhieuGiamGiaDTO();
        dto.setMaPGG(rs.getString("MaPGG"));
        dto.setMaChuSoPGG(rs.getString("MaChuSoPGG"));
        dto.setGiaTriGiamGia(rs.getDouble("GiaTriGiamGia"));
        dto.setGiaTriApDungToiThieu(rs.getDouble("GiaTriApDungToiThieu"));
        dto.setNgayBatDauApDung(rs.getTimestamp("NgayBatDauApDung"));
        dto.setNgayKetThucApDung(rs.getTimestamp("NgayKetThucApDung"));
        dto.setSlDaDung(rs.getInt("SLDaDung"));
        dto.setSlToiDa(rs.getInt("SLToiDa"));
        dto.setNgayTaoPGG(rs.getTimestamp("NgayTaoPGG"));
        dto.setMaNV(rs.getString("MaNV"));
        return dto;
    }
}
