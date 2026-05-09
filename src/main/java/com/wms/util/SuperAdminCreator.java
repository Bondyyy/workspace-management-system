package com.wms.util;

import com.wms.dao.TrangChuQuanLy.QuanLyNhanVien.VaiTroDAO;
import com.wms.model.TrangChuQuanLy.QuanLyNhanVien.VaiTroDTO;
import com.wms.config.DatabaseConnection;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SuperAdminCreator {
    public static void initialize() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            VaiTroDAO vtDao = new VaiTroDAO();

            vtDao.khoiTaoDuLieuChucNang();

            List<String> fullQuyen = new ArrayList<>();
            for (int i = 1; i <= 11; i++) {
                fullQuyen.add(String.format("CN%02d", i));
            }

            boolean roleExists = false;
            try (PreparedStatement ps = conn.prepareStatement("SELECT MaVaiTro FROM VAITRO WHERE MaVaiTro = 'VT01'");
                    ResultSet rs = ps.executeQuery()) {
                roleExists = rs.next();
            }

            if (!roleExists) {
                System.out.println("Dang tao vai tro VT01...");
                VaiTroDTO adminRole = new VaiTroDTO();
                adminRole.setMaVaiTro("VT01");
                adminRole.setTenVaiTro("Quản trị hệ thống");
                adminRole.setMoTa("Toàn quyền quản lý hệ thống");
                vtDao.themVaiTro(adminRole, fullQuyen);
            } else {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("UPDATE VAITRO SET TenVaiTro = 'Quản trị hệ thống' WHERE MaVaiTro = 'VT01'");
                }
                vtDao.capNhatChucNangCuaVaiTro("VT01", fullQuyen);
            }

            String username;
            String password;

            try (InputStream input = SuperAdminCreator.class.getClassLoader()
                    .getResourceAsStream("config.properties")) {
                if (input == null) {
                    throw new RuntimeException("Không tìm thấy file config.properties!");
                }

                Properties prop = new Properties();
                prop.load(input);

                username = prop.getProperty("superadmin.username");
                password = prop.getProperty("superadmin.password");

                if (username == null || username.trim().isEmpty() ||
                        password == null || password.trim().isEmpty()) {
                    throw new RuntimeException(
                            "Thiếu thông tin superadmin.username hoặc superadmin.password trong file config!");
                }

            } catch (Exception ex) {
                System.err.println("LỖI: " + ex.getMessage());
                return;
            }
            String hashedPassword = PasswordUtil.hash(password);
            String maND = null;

            try (PreparedStatement ps = conn.prepareStatement("SELECT MaND FROM NGUOIDUNG WHERE TenTaiKhoan = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        maND = rs.getString("MaND");
                }
            }

            if (maND != null) {
                try (PreparedStatement psUp = conn
                        .prepareStatement("UPDATE NGUOIDUNG SET MatKhauMaHoa = ?, Email = 'admin@spring.com', SDT = '0000000000', GioiTinh = 'Khác' WHERE MaND = ?")) {
                    psUp.setString(1, hashedPassword);
                    psUp.setString(2, maND);
                    psUp.executeUpdate();
                }
            } else {
                maND = "ND_ADMIN_" + System.currentTimeMillis();
                String sqlInsUser = "INSERT INTO NGUOIDUNG (MaND, TenTaiKhoan, MatKhauMaHoa, Email, SDT, GioiTinh, TrangThaiND, ThoiGianTao, CapNhatLanCuoi) "
                        +
                        "VALUES (?, ?, ?, 'admin@spring.com', '0000000000', 'Khác', 'Đang hoạt động', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
                try (PreparedStatement psIU = conn.prepareStatement(sqlInsUser)) {
                    psIU.setString(1, maND);
                    psIU.setString(2, username);
                    psIU.setString(3, hashedPassword);
                    psIU.executeUpdate();
                }
            }

            // Đảm bảo có bản ghi KHACHHANG (để lấy tên hiển thị)
            boolean khExists = false;
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM KHACHHANG WHERE MaND = ?")) {
                ps.setString(1, maND);
                try (ResultSet rs = ps.executeQuery()) { khExists = rs.next(); }
            }
            if (!khExists) {
                String sqlInsKH = "INSERT INTO KHACHHANG (MaKH, HoTenKH, LoaiKH, MaHangThanhVien, TongChiTieu, CapNhatLanCuoi, MaND) VALUES (?, ?, 'VIP', 'HTV00', 0, CURRENT_TIMESTAMP, ?)";
                try (PreparedStatement psKH = conn.prepareStatement(sqlInsKH)) {
                    psKH.setString(1, "KH_ADMIN_" + System.currentTimeMillis());
                    psKH.setString(2, "Spring Admin");
                    psKH.setString(3, maND);
                    psKH.executeUpdate();
                }
            } else {
                try (PreparedStatement psUpKH = conn.prepareStatement("UPDATE KHACHHANG SET HoTenKH = 'Spring Admin' WHERE MaND = ?")) {
                    psUpKH.setString(1, maND);
                    psUpKH.executeUpdate();
                }
            }

            // Đảm bảo có bản ghi NHANVIEN (để tránh lỗi FK khi thêm PGG, hóa đơn...)
            boolean nvExists = false;
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM NHANVIEN WHERE MaND = ?")) {
                ps.setString(1, maND);
                try (ResultSet rs = ps.executeQuery()) { nvExists = rs.next(); }
            }
            if (!nvExists) {
                String sqlInsNV = "INSERT INTO NHANVIEN (MaNV, LoaiNV, NgayVaoLam, TrangThaiLamViec, CaLamViec, LuongCoBan, MaND, MaCN) VALUES (?, 'Quản lý', CURRENT_DATE, 'Đang làm việc', 'Hành chính', 99999999, ?, 'CN001')";
                try (PreparedStatement psNV = conn.prepareStatement(sqlInsNV)) {
                    psNV.setString(1, "NV_ADMIN");
                    psNV.setString(2, maND);
                    psNV.executeUpdate();
                }
            }

            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM CHITIETVAITRO WHERE MaND = ?")) {
                psDel.setString(1, maND);
                psDel.executeUpdate();
            }
            try (PreparedStatement psIL = conn
                    .prepareStatement("INSERT INTO CHITIETVAITRO (MaND, MaVaiTro) VALUES (?, 'VT01')")) {
                psIL.setString(1, maND);
                psIL.executeUpdate();
            }

        } catch (Exception e) {
            System.err.println("[!] LOI KHOI TAO: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        initialize();
    }
}