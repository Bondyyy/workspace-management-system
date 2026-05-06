package com.wms.util;

import com.wms.dao.VaiTroDAO;
import com.wms.model.VaiTroDTO;
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
                        .prepareStatement("UPDATE NGUOIDUNG SET MatKhauMaHoa = ? WHERE MaND = ?")) {
                    psUp.setString(1, hashedPassword);
                    psUp.setString(2, maND);
                    psUp.executeUpdate();
                }
            } else {
                maND = "ND_ADMIN_" + System.currentTimeMillis();
                String sqlInsUser = "INSERT INTO NGUOIDUNG (MaND, TenTaiKhoan, MatKhauMaHoa, Email, TrangThaiND, ThoiGianTao, CapNhatLanCuoi) "
                        +
                        "VALUES (?, ?, ?, 'admin@spring.com', 'Đang hoạt động', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
                try (PreparedStatement psIU = conn.prepareStatement(sqlInsUser)) {
                    psIU.setString(1, maND);
                    psIU.setString(2, username);
                    psIU.setString(3, hashedPassword);
                    psIU.executeUpdate();
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