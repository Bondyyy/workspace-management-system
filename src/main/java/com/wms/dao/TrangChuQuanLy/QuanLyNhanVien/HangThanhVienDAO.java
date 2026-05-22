package com.wms.dao.TrangChuQuanLy.QuanLyNhanVien;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyNhanVien.HangThanhVienDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HangThanhVienDAO {

    public List<HangThanhVienDTO> getAll() {
        List<HangThanhVienDTO> list = new ArrayList<>();
        String sql = "SELECT MaHangThanhVien, TenHangThanhVien, PhanTramTienGiam, TongChiTieuToiThieu FROM HANGTHANHVIEN ORDER BY TongChiTieuToiThieu ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
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

    public void updateDiscount(String maHang, double discount) throws SQLException {
        String sql = "UPDATE HANGTHANHVIEN SET PhanTramTienGiam = ?, CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaHangThanhVien = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, discount);
            ps.setString(2, maHang);
            ps.executeUpdate();
        }
    }

    public String getMaHangByName(String name) {
        String sql = "SELECT MaHangThanhVien FROM HANGTHANHVIEN WHERE TenHangThanhVien = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("MaHangThanhVien");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
