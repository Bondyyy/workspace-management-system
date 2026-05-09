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

    public NguoiDungDTO timTheoTenTaiKhoan(String identifier) throws SQLException {
        String sql = """
                    SELECT n.MaND, n.TenTaiKhoan, n.MatKhauMaHoa, n.AnhDaiDien,
                           n.GioiTinh, n.Email, n.SDT, n.NgaySinh,
                           n.ThoiGianTao, n.CapNhatLanCuoi, n.LanCuoiDangNhap, n.TrangThaiND,
                           v.TenVaiTro, v.MaVaiTro, kh.HoTenKH, nv_table.MaNV
                    FROM NGUOIDUNG n
                    LEFT JOIN CHITIETVAITRO cvt ON n.MaND = cvt.MaND
                    LEFT JOIN VAITRO v ON cvt.MaVaiTro = v.MaVaiTro
                    LEFT JOIN KHACHHANG kh ON n.MaND = kh.MaND
                    LEFT JOIN NHANVIEN nv_table ON n.MaND = nv_table.MaND
                    WHERE n.TenTaiKhoan = ? OR n.Email = ? OR n.SDT = ?
                """;

        NguoiDungDTO user = null;
        List<String> vaiTros = new ArrayList<>();

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ps.setString(3, identifier);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (user == null) {
                    user = new NguoiDungDTO();
                    user.setMaND(rs.getString("MaND"));
                    user.setTenTaiKhoan(rs.getString("TenTaiKhoan"));
                    user.setMatKhauMaHoa(rs.getString("MatKhauMaHoa"));
                    user.setAnhDaiDien(rs.getBytes("AnhDaiDien"));
                    user.setGioiTinh(rs.getString("GioiTinh"));
                    user.setEmail(rs.getString("Email"));
                    user.setSdt(rs.getString("SDT"));
                    user.setNgaySinh(rs.getDate("NgaySinh"));
                    user.setThoiGianTao(rs.getTimestamp("ThoiGianTao"));
                    user.setCapNhatLanCuoi(rs.getTimestamp("CapNhatLanCuoi"));
                    user.setLanCuoiDangNhap(rs.getTimestamp("LanCuoiDangNhap"));
                    user.setTrangThaiND(rs.getString("TrangThaiND"));
                    String hoTen = rs.getString("HoTenKH");
                    if (hoTen == null || hoTen.trim().isEmpty()) {
                        hoTen = rs.getString("TenTaiKhoan");
                    }
                    if (hoTen == null || hoTen.trim().isEmpty()) {
                        hoTen = "Admin"; // Fallback cuối cùng
                    }
                    user.setHoTen(hoTen);
                    user.setMaNV(rs.getString("MaNV"));
                }

                String tenVaiTro = rs.getString("TenVaiTro");
                if (tenVaiTro != null) {
                    vaiTros.add(tenVaiTro);
                }
                String maVaiTro = rs.getString("MaVaiTro");
                if (maVaiTro != null) {
                    vaiTros.add(maVaiTro);
                }
            }
        }

        if (user != null) {
            user.setVaiTro(vaiTros);
            // Load danh sách chức năng của người dùng
            user.setChucNang(layDanhSachChucNangCuaNguoiDung(user.getMaND()));
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

    public boolean kiemTraSdtTonTai(String sdt) throws SQLException {
        Connection conn = getConn();
        if (conn == null) {
            throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");
        }
        String sql = "SELECT 1 FROM NGUOIDUNG WHERE SDT = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sdt);
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

            // Đảm bảo vai trò Khách hàng (VT00) tồn tại trong hệ thống
            String sqlCheckVT = "SELECT COUNT(*) FROM VAITRO WHERE MaVaiTro = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheckVT)) {
                psCheck.setString(1, com.wms.config.AppConstants.ROLE_CUSTOMER_CODE);
                ResultSet rsCheck = psCheck.executeQuery();
                if (rsCheck.next() && rsCheck.getInt(1) == 0) {
                    String sqlInsVT = "INSERT INTO VAITRO (MaVaiTro, TenVaiTro, MoTa) VALUES (?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(sqlInsVT)) {
                        psIns.setString(1, com.wms.config.AppConstants.ROLE_CUSTOMER_CODE);
                        psIns.setString(2, com.wms.config.AppConstants.ROLE_CUSTOMER_NAME);
                        psIns.setString(3, "Quyền hạn mặc định cho người đăng ký mới");
                        psIns.executeUpdate();
                    }
                }
            }

            String sqlVaiTro = "INSERT INTO CHITIETVAITRO (MaND, MaVaiTro) VALUES (?, ?)";
            try (PreparedStatement psVT = conn.prepareStatement(sqlVaiTro)) {
                psVT.setString(1, maND);
                psVT.setString(2, com.wms.config.AppConstants.ROLE_CUSTOMER_CODE);
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

    public void capNhatMatKhauTheoEmail(String email, String matKhauMaHoa) throws SQLException {
        String sql = "UPDATE NGUOIDUNG SET MatKhauMaHoa = ?, CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE Email = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, matKhauMaHoa);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    public java.util.List<String> layDanhSachChucNangCuaNguoiDung(String maND) {
        java.util.List<String> list = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT ctcn.MaChucNang " +
                "FROM CHITIETVAITRO cvt " +
                "JOIN CHITIETNHOMCHUCNANG ctncn ON cvt.MaVaiTro = ctncn.MaVaiTro " +
                "JOIN CHITIETCHUCNANG ctcn ON ctncn.MaNhomChucNang = ctcn.MaNhomChucNang " +
                "WHERE cvt.MaND = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maND);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("MaChucNang"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[NguoiDungDAO] Lỗi lấy chức năng người dùng: " + e.getMessage());
        }
        return list;
    }
}