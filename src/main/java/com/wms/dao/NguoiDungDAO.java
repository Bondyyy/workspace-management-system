package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.NguoiDung;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NguoiDungDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public NguoiDung findByUsername(String tenTaiKhoan) throws SQLException {
        String sql = """
            SELECT n.MaND, n.TenTaiKhoan, n.MatKhauMaHoa, n.AnhDaiDien, 
                   n.GioiTinh, n.Email, n.SDT, n.NgaySinh, 
                   n.ThoiGianTao, n.CapNhatLanCuoi, n.LanCuoiDangNhap, n.TrangThaiND,
                   v.TenVaiTro
            FROM NGUOIDUNG n
            LEFT JOIN NGUOIDUNG_VAITRO nv ON n.MaND = nv.MaND
            LEFT JOIN VAITRO v ON nv.MaVaiTro = v.MaVaiTro
            WHERE n.TenTaiKhoan = ?
        """;

        NguoiDung user = null;
        List<String> vaiTros = new ArrayList<>();

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, tenTaiKhoan);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (user == null) {
                    user = new NguoiDung();
                    user.setMaND(rs.getString("MaND"));
                    user.setTenTaiKhoan(rs.getString("TenTaiKhoan"));
                    user.setMatKhauMaHoa(rs.getString("MatKhauMaHoa"));
                    user.setAnhDaiDien(rs.getString("AnhDaiDien"));
                    user.setGioiTinh(rs.getString("GioiTinh"));
                    user.setEmail(rs.getString("Email"));
                    user.setSdt(rs.getString("SDT"));
                    user.setNgaySinh(rs.getDate("NgaySinh"));
                    user.setThoiGianTao(rs.getTimestamp("ThoiGianTao"));
                    user.setCapNhatLanCuoi(rs.getTimestamp("CapNhatLanCuoi"));
                    user.setLanCuoiDangNhap(rs.getTimestamp("LanCuoiDangNhap"));
                    user.setTrangThaiND(rs.getString("TrangThaiND"));
                }
                
                String tenVaiTro = rs.getString("TenVaiTro");
                if (tenVaiTro != null) {
                    vaiTros.add(tenVaiTro);
                }
            }
        }

        if (user != null) {
            user.setVaiTro(vaiTros);
        }
        return user;
    }

    public void updateLastLogin(String maND) throws SQLException {
        String sql = "UPDATE NGUOIDUNG SET LanCuoiDangNhap = CURRENT_TIMESTAMP WHERE MaND = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maND);
            ps.executeUpdate();
        }
    }
}