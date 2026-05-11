package com.wms.dao.TrangChuQuanLy.QuanLyHoiVien;

import com.wms.config.DatabaseConnection;
import com.wms.dao.TrangChuQuanLy.QuanLyNhanVien.NguoiDungDAO;
import com.wms.model.TrangChuQuanLy.QuanLyHoiVien.HoiVienDTO;
import com.wms.util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<HoiVienDTO> getAll() {
        List<HoiVienDTO> list = new ArrayList<>();
        String sql = "SELECT kh.MaKH, kh.MaND, kh.HoTenKH, kh.TongChiTieu, kh.MaHangThanhVien, " +
                     "nd.SDT, nd.Email, nd.NgaySinh, nd.GioiTinh, nd.AnhDaiDien, nd.TrangThaiND, " +
                     "h.TenHangThanhVien " +
                     "FROM KHACHHANG kh " +
                     "JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                     "LEFT JOIN HANGTHANHVIEN h ON kh.MaHangThanhVien = h.MaHangThanhVien " +
                     "WHERE kh.MaKH NOT LIKE 'KH_ADMIN_%' " +
                     "ORDER BY kh.MaKH DESC";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                HoiVienDTO dto = new HoiVienDTO();
                dto.setMaKH(rs.getString("MaKH"));
                dto.setMaND(rs.getString("MaND"));
                dto.setHoTen(rs.getString("HoTenKH"));
                dto.setSdt(rs.getString("SDT"));
                dto.setEmail(rs.getString("Email"));
                dto.setNgaySinh(rs.getDate("NgaySinh"));
                dto.setGioiTinh(rs.getString("GioiTinh"));
                dto.setAnhDaiDien(rs.getBytes("AnhDaiDien"));
                dto.setTrangThai(rs.getString("TrangThaiND"));
                dto.setTongChiTieu(rs.getDouble("TongChiTieu"));
                dto.setMaHangThanhVien(rs.getString("MaHangThanhVien"));
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
        String sql = "SELECT kh.MaKH, kh.MaND, kh.HoTenKH, kh.TongChiTieu, kh.MaHangThanhVien, " +
                     "nd.SDT, nd.Email, nd.NgaySinh, nd.GioiTinh, nd.AnhDaiDien, nd.TrangThaiND, " +
                     "h.TenHangThanhVien " +
                     "FROM KHACHHANG kh " +
                     "JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                     "LEFT JOIN HANGTHANHVIEN h ON kh.MaHangThanhVien = h.MaHangThanhVien " +
                     "WHERE kh.MaKH NOT LIKE 'KH_ADMIN_%' " +
                     "AND (kh.HoTenKH LIKE ? OR nd.SDT LIKE ? OR nd.Email LIKE ?) " +
                     "ORDER BY kh.MaKH DESC";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String q = "%" + keyword + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoiVienDTO dto = new HoiVienDTO();
                    dto.setMaKH(rs.getString("MaKH"));
                    dto.setMaND(rs.getString("MaND"));
                    dto.setHoTen(rs.getString("HoTenKH"));
                    dto.setSdt(rs.getString("SDT"));
                    dto.setEmail(rs.getString("Email"));
                    dto.setNgaySinh(rs.getDate("NgaySinh"));
                    dto.setGioiTinh(rs.getString("GioiTinh"));
                    dto.setAnhDaiDien(rs.getBytes("AnhDaiDien"));
                    dto.setTrangThai(rs.getString("TrangThaiND"));
                    dto.setTongChiTieu(rs.getDouble("TongChiTieu"));
                    dto.setMaHangThanhVien(rs.getString("MaHangThanhVien"));
                    dto.setHangThanhVien(rs.getString("TenHangThanhVien"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public String timMaKHTheoMaND(String maND) {
        if (maND == null || maND.isEmpty()) return null;
        String sql = "SELECT MaKH FROM KHACHHANG WHERE MaND = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("MaKH");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String taoMaKHMoi(Connection conn) throws SQLException {
        boolean isStandalone = (conn == null);
        Connection localConn = isStandalone ? getConn() : conn;
        String sql = "SELECT MAX(MaKH) AS MaxMa FROM KHACHHANG WHERE MaKH LIKE 'HV%'";
        try (PreparedStatement ps = localConn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maxMa = rs.getString("MaxMa");
                if (maxMa != null) {
                    try {
                        int soThuTu = Integer.parseInt(maxMa.substring(2)) + 1;
                        return String.format("HV%06d", soThuTu);
                    } catch (NumberFormatException e) {}
                }
            }
        } finally {
            if (isStandalone && localConn != null) localConn.close();
        }
        return "HV000001";
    }

    public void insert(HoiVienDTO dto) throws SQLException {
        NguoiDungDAO ndDAO = new NguoiDungDAO();
        if (ndDAO.kiemTraEmailTonTai(dto.getEmail())) {
            throw new SQLException("Email " + dto.getEmail() + " đã tồn tại!");
        }
        if (ndDAO.kiemTraSdtTonTai(dto.getSdt())) {
            throw new SQLException("Số điện thoại " + dto.getSdt() + " đã tồn tại!");
        }

        String maND = java.util.UUID.randomUUID().toString();
        
        Connection conn = getConn();
        if (conn == null) throw new SQLException("Lỗi kết nối CSDL!");
        
        String maKH = taoMaKHMoi(conn);
        dto.setMaND(maND);
        dto.setMaKH(maKH);

        String sqlND = "INSERT INTO NGUOIDUNG (MaND, TenTaiKhoan, MatKhauMaHoa, SDT, Email, NgaySinh, GioiTinh, AnhDaiDien, TrangThaiND, ThoiGianTao, CapNhatLanCuoi) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        String sqlKH = "INSERT INTO KHACHHANG (MaKH, HoTenKH, MaHangThanhVien, TongChiTieu, CapNhatLanCuoi, MaND) " +
                       "VALUES (?, ?, 'HTV01', 0, CURRENT_TIMESTAMP, ?)";

        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlND)) {
                String username = (dto.getSdt() != null && !dto.getSdt().isEmpty()) ? dto.getSdt() : dto.getEmail();
                ps1.setString(1, maND);
                ps1.setString(2, username);
                ps1.setString(3, PasswordUtil.hash("123456"));
                ps1.setString(4, dto.getSdt());
                ps1.setString(5, dto.getEmail());
                ps1.setDate(6, dto.getNgaySinh());
                ps1.setString(7, dto.getGioiTinh());
                ps1.setBytes(8, dto.getAnhDaiDien());
                ps1.setString(9, "Đang hoạt động");
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
            conn.close();
        }
    }

    public void update(HoiVienDTO dto) throws SQLException {
        String sqlKH = "UPDATE KHACHHANG SET HoTenKH = ?, CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaKH = ?";
        String sqlND = "UPDATE NGUOIDUNG SET SDT = ?, Email = ?, NgaySinh = ?, GioiTinh = ?, AnhDaiDien = ?, TrangThaiND = ?, CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaND = ?";

        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = conn.prepareStatement(sqlKH)) {
                    ps1.setString(1, dto.getHoTen());
                    ps1.setString(2, dto.getMaKH());
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement(sqlND)) {
                    ps2.setString(1, dto.getSdt());
                    ps2.setString(2, dto.getEmail());
                    ps2.setDate(3, dto.getNgaySinh());
                    ps2.setString(4, dto.getGioiTinh());
                    ps2.setBytes(5, dto.getAnhDaiDien());
                    ps2.setString(6, dto.getTrangThai());
                    ps2.setString(7, dto.getMaND());
                    ps2.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void delete(String maKH, String maND) throws SQLException {
        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                // Xóa các bảng liên quan trước nếu có (ví dụ hóa đơn, đặt chỗ...)
                // Ở đây giả định xóa được trực tiếp hoặc có ON DELETE CASCADE
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM CHITIETVAITRO WHERE MaND = ?")) {
                    ps.setString(1, maND);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM KHACHHANG WHERE MaKH = ?")) {
                    ps.setString(1, maKH);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM NGUOIDUNG WHERE MaND = ?")) {
                    ps.setString(1, maND);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
