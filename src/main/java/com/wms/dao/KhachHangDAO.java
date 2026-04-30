package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.NhanSu_KhachHang.HoiVienDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<HoiVienDTO> getAll() {
        List<HoiVienDTO> list = new ArrayList<>();
        String sql = "SELECT kh.MaKH, kh.MaND, kh.HoTenKH, nd.SDT, nd.Email, nd.NgaySinh, h.TenHangThanhVien " +
                     "FROM KHACHHANG kh " +
                     "JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                     "LEFT JOIN HANGTHANHVIEN h ON kh.MaHangThanhVien = h.MaHangThanhVien";

        Connection conn = getConn();
        if (conn == null) return list;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                HoiVienDTO dto = new HoiVienDTO();
                dto.setMaKH(rs.getString("MaKH"));
                dto.setMaND(rs.getString("MaND"));
                dto.setHoTen(rs.getString("HoTenKH"));
                dto.setSdt(rs.getString("SDT"));
                dto.setEmail(rs.getString("Email"));
                dto.setNgaySinh(rs.getDate("NgaySinh"));
                dto.setHangThanhVien(rs.getString("TenHangThanhVien"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<HoiVienDTO> search(String keyword) {
        List<HoiVienDTO> list = new ArrayList<>();
        String sql = "SELECT kh.MaKH, kh.MaND, kh.HoTenKH, nd.SDT, nd.Email, nd.NgaySinh, h.TenHangThanhVien " +
                     "FROM KHACHHANG kh " +
                     "JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                     "LEFT JOIN HANGTHANHVIEN h ON kh.MaHangThanhVien = h.MaHangThanhVien " +
                     "WHERE kh.MaKH LIKE ? OR nd.SDT LIKE ?";

        Connection conn = getConn();
        if (conn == null) return list;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String q = "%" + keyword + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoiVienDTO dto = new HoiVienDTO();
                    dto.setMaKH(rs.getString("MaKH"));
                    dto.setMaND(rs.getString("MaND"));
                    dto.setHoTen(rs.getString("HoTenKH"));
                    dto.setSdt(rs.getString("SDT"));
                    dto.setEmail(rs.getString("Email"));
                    dto.setNgaySinh(rs.getDate("NgaySinh"));
                    dto.setHangThanhVien(rs.getString("TenHangThanhVien"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void update(HoiVienDTO dto) throws SQLException {
        String sqlKhachHang = "UPDATE KHACHHANG SET HoTenKH = ? WHERE MaKH = ?";
        String sqlNguoiDung = "UPDATE NGUOIDUNG SET SDT = ?, Email = ?, NgaySinh = ? WHERE MaND = ?";

        Connection conn = getConn();
        if (conn == null) throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");
        
        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlKhachHang)) {
                ps1.setString(1, dto.getHoTen());
                ps1.setString(2, dto.getMaKH());
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(sqlNguoiDung)) {
                ps2.setString(1, dto.getSdt());
                ps2.setString(2, dto.getEmail());
                if (dto.getNgaySinh() != null) {
                    ps2.setDate(3, dto.getNgaySinh());
                } else {
                    ps2.setNull(3, Types.DATE);
                }
                ps2.setString(4, dto.getMaND());
                ps2.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    public void insert(HoiVienDTO dto) throws SQLException {
        String maND = java.util.UUID.randomUUID().toString();
        String maKH = java.util.UUID.randomUUID().toString();
        dto.setMaND(maND);
        dto.setMaKH(maKH);

        String sqlND = "INSERT INTO NGUOIDUNG (MaND, TenTaiKhoan, MatKhauMaHoa, SDT, Email, NgaySinh, TrangThaiND, ThoiGianTao, CapNhatLanCuoi) " +
                       "VALUES (?, ?, ?, ?, ?, ?, 'Đang hoạt động', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        String sqlKH = "INSERT INTO KHACHHANG (MaKH, HoTenKH, CapNhatLanCuoi, MaND) " +
                       "VALUES (?, ?, CURRENT_TIMESTAMP, ?)";

        Connection conn = getConn();
        if (conn == null) throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");
        
        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlND)) {
                String defaultUsername = (dto.getSdt() != null && !dto.getSdt().isEmpty()) ? dto.getSdt() : dto.getEmail();
                if (defaultUsername == null || defaultUsername.isEmpty()) {
                    defaultUsername = "user_" + System.currentTimeMillis();
                }
                ps1.setString(1, maND);
                ps1.setString(2, defaultUsername);
                ps1.setString(3, com.wms.util.PasswordUtil.hash("123456"));
                ps1.setString(4, dto.getSdt());
                ps1.setString(5, dto.getEmail());
                if (dto.getNgaySinh() != null) {
                    ps1.setDate(6, dto.getNgaySinh());
                } else {
                    ps1.setNull(6, Types.DATE);
                }
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(sqlKH)) {
                ps2.setString(1, maKH);
                ps2.setString(2, dto.getHoTen());
                ps2.setString(3, maND);
                ps2.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    public void delete(String maKH, String maND) throws SQLException {
        String sqlKhachHang = "DELETE FROM KHACHHANG WHERE MaKH = ?";
        String sqlNguoiDung = "DELETE FROM NGUOIDUNG WHERE MaND = ?";
        String sqlVaiTro = "DELETE FROM NGUOIDUNG_VAITRO WHERE MaND = ?";

        Connection conn = getConn();
        if (conn == null) throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");
        
        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps3 = conn.prepareStatement(sqlVaiTro)) {
                ps3.setString(1, maND);
                ps3.executeUpdate();
            }

            try (PreparedStatement ps1 = conn.prepareStatement(sqlKhachHang)) {
                ps1.setString(1, maKH);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(sqlNguoiDung)) {
                ps2.setString(1, maND);
                ps2.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }
}
