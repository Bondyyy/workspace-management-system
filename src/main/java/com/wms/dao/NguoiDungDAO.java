package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.NguoiDungDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NguoiDungDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public NguoiDungDTO timTheoTenTaiKhoan(String tenTaiKhoan) throws SQLException {
        String sql = """
                    SELECT n.MaND, n.TenTaiKhoan, n.MatKhauMaHoa, n.AnhDaiDien,
                           n.GioiTinh, n.Email, n.SDT, n.NgaySinh,
                           n.ThoiGianTao, n.CapNhatLanCuoi, n.LanCuoiDangNhap, n.TrangThaiND,
                           v.TenVaiTro
                    FROM NGUOIDUNG n
                    LEFT JOIN CHITIETVAITRO nv ON n.MaND = nv.MaND
                    LEFT JOIN VAITRO v ON nv.MaVaiTro = v.MaVaiTro
                    WHERE n.TenTaiKhoan = ?
                """;

        NguoiDungDTO user = null;
        List<String> vaiTros = new ArrayList<>();

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, tenTaiKhoan);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (user == null) {
                    user = new NguoiDungDTO();
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

    public boolean kiemTraTaiKhoanTonTai(String tenTaiKhoan) throws SQLException {
        Connection conn = getConn();
        if (conn == null) {
            throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");
        }
        String sql = "SELECT 1 FROM NGUOIDUNG WHERE TenTaiKhoan = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenTaiKhoan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean kiemTraEmailTonTai(String email) throws SQLException {
        Connection conn = getConn();
        if (conn == null) {
            throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");
        }
        String sql = "SELECT 1 FROM NGUOIDUNG WHERE Email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void themNguoiDung(NguoiDungDTO user, String hoTen) throws SQLException {
        String maND = java.util.UUID.randomUUID().toString();
        user.setMaND(maND); // Set generated ID

        String sqlND = "INSERT INTO NGUOIDUNG (MaND, TenTaiKhoan, MatKhauMaHoa, Email, TrangThaiND, ThoiGianTao, CapNhatLanCuoi) "
                +
                "VALUES (?, ?, ?, ?, 'Đang hoạt động', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        String sqlKH = "INSERT INTO KHACHHANG (MaKH, HoTenKH, TongChiTieu, CapNhatLanCuoi, MaND) " +
                "VALUES (?, ?, 0, CURRENT_TIMESTAMP, ?)";

        Connection conn = getConn();
        if (conn == null) {
            throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");
        }

        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlND)) {
                ps.setString(1, maND);
                ps.setString(2, user.getTenTaiKhoan());
                ps.setString(3, user.getMatKhauMaHoa());
                ps.setString(4, user.getEmail());
                ps.executeUpdate();
            }

            try (PreparedStatement psKH = conn.prepareStatement(sqlKH)) {
                psKH.setString(1, java.util.UUID.randomUUID().toString()); // Sinh MaKH ngẫu nhiên
                psKH.setString(2, hoTen);
                psKH.setString(3, maND);
                psKH.executeUpdate();
            }

            String sqlVaiTro = "INSERT INTO CHITIETVAITRO (MaND, MaVaiTro) VALUES (?, 'VT02')";
            try (PreparedStatement psVT = conn.prepareStatement(sqlVaiTro)) {
                psVT.setString(1, maND);
                psVT.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    public void updateLastLogin(String maND) throws SQLException {
        String sql = "UPDATE NGUOIDUNG SET LanCuoiDangNhap = CURRENT_TIMESTAMP WHERE MaND = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maND);
            ps.executeUpdate();
        }
    }
}