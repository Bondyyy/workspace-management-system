package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.HangThanhVienDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HangThanhVienDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<HangThanhVienDTO> getAll() {
        List<HangThanhVienDTO> list = new ArrayList<>();
        String sql = "SELECT MaHangThanhVien, TenHangThanhVien, PhanTramTienGiam, TongChiTieuToiThieu FROM HANGTHANHVIEN";

        Connection conn = getConn();
        if (conn == null)
            return list;

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HangThanhVienDTO dto = new HangThanhVienDTO();
                dto.setMaHangThanhVien(rs.getString("MaHangThanhVien"));
                dto.setTenHangThanhVien(rs.getString("TenHangThanhVien"));
                dto.setPhanTramTienGiam(rs.getDouble("PhanTramTienGiam"));
                dto.setTongChiTieuToiThieu(rs.getDouble("TongChiTieuToiThieu"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<HangThanhVienDTO> search(String keyword) {
        List<HangThanhVienDTO> list = new ArrayList<>();
        String sql = "SELECT MaHangThanhVien, TenHangThanhVien, PhanTramTienGiam, TongChiTieuToiThieu FROM HANGTHANHVIEN WHERE TenHangThanhVien LIKE ?";

        Connection conn = getConn();
        if (conn == null)
            return list;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HangThanhVienDTO dto = new HangThanhVienDTO();
                    dto.setMaHangThanhVien(rs.getString("MaHangThanhVien"));
                    dto.setTenHangThanhVien(rs.getString("TenHangThanhVien"));
                    dto.setPhanTramTienGiam(rs.getDouble("PhanTramTienGiam"));
                    dto.setTongChiTieuToiThieu(rs.getDouble("TongChiTieuToiThieu"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insert(HangThanhVienDTO dto) throws SQLException {
        String sql = "INSERT INTO HANGTHANHVIEN (MaHangThanhVien, TenHangThanhVien, PhanTramTienGiam, TongChiTieuToiThieu) VALUES (?, ?, ?, ?)";
        Connection conn = getConn();
        if (conn == null)
            throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaHangThanhVien());
            ps.setString(2, dto.getTenHangThanhVien());
            ps.setDouble(3, dto.getPhanTramTienGiam());
            ps.setDouble(4, dto.getTongChiTieuToiThieu());
            ps.executeUpdate();
        }
    }

    public void update(HangThanhVienDTO dto) throws SQLException {
        String sql = "UPDATE HANGTHANHVIEN SET TenHangThanhVien = ?, PhanTramTienGiam = ?, TongChiTieuToiThieu = ? WHERE MaHangThanhVien = ?";
        Connection conn = getConn();
        if (conn == null)
            throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getTenHangThanhVien());
            ps.setDouble(2, dto.getPhanTramTienGiam());
            ps.setDouble(3, dto.getTongChiTieuToiThieu());
            ps.setString(4, dto.getMaHangThanhVien());
            ps.executeUpdate();
        }
    }

    public void delete(String maHang) throws SQLException {
        String sql = "DELETE FROM HANGTHANHVIEN WHERE MaHangThanhVien = ?";
        Connection conn = getConn();
        if (conn == null)
            throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHang);
            ps.executeUpdate();
        }
    }
}
