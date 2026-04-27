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

    public void themNguoiDung(NguoiDungDTO user, String hoTen) throws SQLException {
        String maND = java.util.UUID.randomUUID().toString();
        user.setMaND(maND); // Set generated ID

        String sqlND = "INSERT INTO NGUOIDUNG (MaND, TenTaiKhoan, MatKhauMaHoa, Email, TrangThaiND, ThoiGianTao, CapNhatLanCuoi) " +
                       "VALUES (?, ?, ?, ?, 'Đang hoạt động', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
                       
        // [ĐÃ SỬA]: Khởi tạo thêm TongChiTieu = 0 để các Trigger liên quan đến hạng thành viên không bị lỗi toán học do giá trị NULL
        String sqlKH = "INSERT INTO KHACHHANG (MaKH, HoTenKH, TongChiTieu, CapNhatLanCuoi, MaND) " +
                       "VALUES (?, ?, 0, CURRENT_TIMESTAMP, ?)";
        
        Connection conn = getConn();
        if (conn == null) {
            throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");
        }
        
        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false); // Bắt đầu transaction (Transaction: đảm bảo ghi cả 2 bảng thành công hoặc không ghi gì cả)
            
            // 1. Insert vào NGUOIDUNG
            try (PreparedStatement ps = conn.prepareStatement(sqlND)) {
                ps.setString(1, maND);
                ps.setString(2, user.getTenTaiKhoan());
                ps.setString(3, user.getMatKhauMaHoa());
                ps.setString(4, user.getEmail());
                ps.executeUpdate();
            }
            
            // 2. Insert vào KHACHHANG
            try (PreparedStatement psKH = conn.prepareStatement(sqlKH)) {
                psKH.setString(1, java.util.UUID.randomUUID().toString()); // Sinh MaKH ngẫu nhiên
                psKH.setString(2, hoTen);
                psKH.setString(3, maND); // Khóa ngoại liên kết tới NGUOIDUNG
                psKH.executeUpdate();
            }
            
            conn.commit(); // Hoàn tất transaction
        } catch (SQLException e) {
            conn.rollback(); // Rollback nếu có lỗi ở bất kỳ thao tác insert nào
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit); // Trả lại cấu hình ban đầu
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