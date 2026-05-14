package com.wms.dao.TrangChuQuanLy.QuanLyLoaiDichVu;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyLoaiDichVu.LoaiDichVuDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoaiDichVuDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<LoaiDichVuDTO> layTatCa() throws SQLException {
        List<LoaiDichVuDTO> list = new ArrayList<>();
        String sql = "SELECT MaLoaiDV, TenLoaiDV, TrangThaiLDV FROM LOAIDICHVU ORDER BY MaLoaiDV";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToDTO(rs));
            }
        }
        return list;
    }

    public List<LoaiDichVuDTO> search(String keyword) throws SQLException {
        List<LoaiDichVuDTO> list = new ArrayList<>();
        String sql = "SELECT MaLoaiDV, TenLoaiDV, TrangThaiLDV FROM LOAIDICHVU " +
                     "WHERE LOWER(MaLoaiDV) LIKE ? OR LOWER(TenLoaiDV) LIKE ? ORDER BY MaLoaiDV";
        String searchKey = "%" + keyword.toLowerCase() + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, searchKey);
            ps.setString(2, searchKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDTO(rs));
                }
            }
        }
        return list;
    }

    public boolean them(LoaiDichVuDTO loai) throws SQLException {
        String sql = "INSERT INTO LOAIDICHVU (MaLoaiDV, TenLoaiDV, TrangThaiLDV) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, loai.getMaLoaiDV());
            ps.setString(2, loai.getTenLoaiDV());
            ps.setString(3, loai.getTrangThaiLDV());
            ps.executeUpdate();
            return true;
        }
    }

    public boolean capNhat(LoaiDichVuDTO loai) throws SQLException {
        String sql = "UPDATE LOAIDICHVU SET TenLoaiDV = ?, TrangThaiLDV = ? WHERE MaLoaiDV = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, loai.getTenLoaiDV());
            ps.setString(2, loai.getTrangThaiLDV());
            ps.setString(3, loai.getMaLoaiDV());
            ps.executeUpdate();
            return true;
        }
    }

    public String layMaxMaLoaiDV() throws SQLException {
        String sql = "SELECT MAX(MaLoaiDV) FROM LOAIDICHVU";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString(1);
            }
        }
        return null;
    }

    public String generateNextMa() throws SQLException {
        String sql = "SELECT MAX(MaLoaiDV) FROM LOAIDICHVU";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maxMa = rs.getString(1);
                if (maxMa != null && maxMa.startsWith("LDV")) {
                    int num = Integer.parseInt(maxMa.substring(3)) + 1;
                    return String.format("LDV%03d", num);
                }
            }
        }
        return "LDV001";
    }

    private LoaiDichVuDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        LoaiDichVuDTO loai = new LoaiDichVuDTO();
        loai.setMaLoaiDV(rs.getString("MaLoaiDV"));
        loai.setTenLoaiDV(rs.getString("TenLoaiDV"));
        loai.setTrangThaiLDV(rs.getString("TrangThaiLDV"));
        return loai;
    }
}
