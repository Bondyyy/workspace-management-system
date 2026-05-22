package com.wms.util;

import com.wms.config.DatabaseConnection;
import com.wms.dao.TrangChuQuanLy.QuanLyVaiTro.VaiTroDAO;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.VaiTroDTO;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SuperAdminCreator {

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Không thể kết nối CSDL.");
            }

            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                DataInitializer.initializeAll(conn);
                ensureAdminRole(conn);

                Properties config = loadConfig();
                String username = config.getProperty("superadmin.username");
                String password = config.getProperty("superadmin.password");

                if (username == null || username.isBlank() || password == null || password.isBlank()) {
                    throw new IllegalStateException(
                            "Thiếu superadmin.username hoặc superadmin.password trong config.properties.");
                }

                DbLabels labels = loadDbLabels(conn);

                // Dọn dẹp các tài khoản admin cũ dạng ND_ADMIN_[Timestamp] để tránh xung đột
                cleanLegacyAdminData(conn, username.trim());

                String maND = upsertAdminUser(conn, username.trim(), password.trim(), labels);
                ensureAdminCustomer(conn, maND);
                ensureAdminEmployee(conn, maND, labels);
                ensureAdminRoleMapping(conn, maND);

                conn.commit();
                System.out.println("[SuperAdminCreator] Da san sang tai khoan admin: " + username);
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        } catch (Exception e) {
            System.err.println("[!] LOI KHOI TAO SUPER ADMIN: " + e.getMessage());
        }
    }

    private static void cleanLegacyAdminData(Connection conn, String username) throws Exception {
        List<String> legacyMaNDs = new ArrayList<>();
        // Tìm các mã ND admin cũ (chứa dấu gạch dưới và số timestamp hoặc khớp tài khoản nhưng không phải mã cố định)
        String sqlSelect = "SELECT MaND FROM NGUOIDUNG WHERE (TenTaiKhoan = ? OR MaND LIKE 'ND_ADMIN_%') AND MaND <> 'ND_ADMIN'";
        try (PreparedStatement ps = conn.prepareStatement(sqlSelect)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    legacyMaNDs.add(rs.getString("MaND"));
                }
            }
        }

        if (!legacyMaNDs.isEmpty()) {
            System.out.println("[SuperAdminCreator] Dang don dep du lieu admin cu: " + legacyMaNDs);
            for (String oldMaND : legacyMaNDs) {
                // Xóa chi tiết vai trò
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM CHITIETVAITRO WHERE MaND = ?")) {
                    ps.setString(1, oldMaND);
                    ps.executeUpdate();
                }
                // Xóa nhân viên
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM NHANVIEN WHERE MaND = ?")) {
                    ps.setString(1, oldMaND);
                    ps.executeUpdate();
                }
                // Xóa khách hàng
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM KHACHHANG WHERE MaND = ?")) {
                    ps.setString(1, oldMaND);
                    ps.executeUpdate();
                }
                // Xóa người dùng
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM NGUOIDUNG WHERE MaND = ?")) {
                    ps.setString(1, oldMaND);
                    ps.executeUpdate();
                }
            }
        }

        // Xóa bất kỳ tài khoản khách hàng admin legacy nào bắt đầu bằng KH_ADMIN_
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM KHACHHANG WHERE MaKH LIKE 'KH_ADMIN_%' AND MaKH <> 'KH_ADMIN'")) {
            ps.executeUpdate();
        }
    }

    private static Properties loadConfig() throws Exception {
        try (InputStream input = SuperAdminCreator.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IllegalStateException("Không tìm thấy file config.properties!");
            }

            Properties prop = new Properties();
            prop.load(input);
            return prop;
        }
    }

    private static void ensureAdminRole(Connection conn) throws Exception {
        List<String> fullQuyen = new ArrayList<>();
        // Lấy danh sách quyền theo yêu cầu của user cho VT01
        String[] adminRights = {"CN01", "CN02", "CN09", "CN11", "CN12", "CN13", "CN14"};
        for (String right : adminRights) {
            fullQuyen.add(right);
        }

        boolean roleExists;
        try (PreparedStatement ps = conn.prepareStatement("SELECT MaVaiTro FROM VAITRO WHERE MaVaiTro = 'VT01'");
             ResultSet rs = ps.executeQuery()) {
            roleExists = rs.next();
        }

        String tenVaiTro = Normalizer.normalize("Quản trị viên Hệ thống", Normalizer.Form.NFC);
        String moTa = Normalizer.normalize("Toàn quyền quản lý hệ thống", Normalizer.Form.NFC);

        if (!roleExists) {
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO VAITRO (MaVaiTro, TenVaiTro, MoTa) VALUES ('VT01', ?, ?)")) {
                ps.setString(1, tenVaiTro);
                ps.setString(2, moTa);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO NHOMCHUCNANG (MaNhomChucNang, TenNhomChucNang, MoTa) VALUES ('VT01', ?, ?)")) {
                ps.setString(1, tenVaiTro);
                ps.setString(2, moTa);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO CHITIETNHOMCHUCNANG (MaVaiTro, MaNhomChucNang, MoTa) VALUES ('VT01', 'VT01', ?)")) {
                ps.setString(1, "Liên kết vai trò với nhóm chức năng mặc định");
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE VAITRO SET TenVaiTro = ?, MoTa = ? WHERE MaVaiTro = 'VT01'")) {
                ps.setString(1, tenVaiTro);
                ps.setString(2, moTa);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE NHOMCHUCNANG SET TenNhomChucNang = ?, MoTa = ? WHERE MaNhomChucNang = 'VT01'")) {
                ps.setString(1, tenVaiTro);
                ps.setString(2, moTa);
                ps.executeUpdate();
            }
        }

        // Cập nhật chi tiết chức năng
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM CHITIETCHUCNANG WHERE MaNhomChucNang = 'VT01'")) {
            ps.executeUpdate();
        }
        if (!fullQuyen.isEmpty()) {
            String sql = "INSERT INTO CHITIETCHUCNANG (MaNhomChucNang, MaChucNang, MoTa) VALUES ('VT01', ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (String maCN : fullQuyen) {
                    ps.setString(1, maCN);
                    ps.setString(2, "Cấp quyền chức năng cho nhóm");
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    private static String upsertAdminUser(Connection conn, String username, String password, DbLabels labels)
            throws Exception {
        String hashedPassword = PasswordUtil.hash(password);
        String maND = null;

        try (PreparedStatement ps = conn.prepareStatement("SELECT MaND FROM NGUOIDUNG WHERE TenTaiKhoan = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    maND = rs.getString("MaND");
                }
            }
        }

        if (maND != null) {
            try (PreparedStatement ps = conn.prepareStatement("""
                    UPDATE NGUOIDUNG
                    SET HoTen = ?, MatKhauMaHoa = ?, Email = ?, SDT = ?, GioiTinh = ?, TrangThaiND = ?,
                        CapNhatLanCuoi = CURRENT_TIMESTAMP
                    WHERE MaND = ?
                    """)) {
                ps.setString(1, "Spring Admin");
                ps.setString(2, hashedPassword);
                ps.setString(3, "springchaonhe@gmail.com");
                ps.setString(4, "0000000000");
                ps.setString(5, labels.gioiTinhKhac());
                ps.setString(6, labels.trangThaiNguoiDungHoatDong());
                ps.setString(7, maND);
                ps.executeUpdate();
            }
            return maND;
        }

        maND = "ND_ADMIN";
        try (PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO NGUOIDUNG
                    (MaND, HoTen, TenTaiKhoan, MatKhauMaHoa, Email, SDT, GioiTinh, TrangThaiND,
                     ThoiGianTao, CapNhatLanCuoi)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """)) {
            ps.setString(1, maND);
            ps.setString(2, "Spring Admin");
            ps.setString(3, username);
            ps.setString(4, hashedPassword);
            ps.setString(5, "springchaonhe@gmail.com");
            ps.setString(6, "0000000000");
            ps.setString(7, labels.gioiTinhKhac());
            ps.setString(8, labels.trangThaiNguoiDungHoatDong());
            ps.executeUpdate();
        }
        return maND;
    }

    private static void ensureAdminCustomer(Connection conn, String maND) throws Exception {
        boolean exists;
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM KHACHHANG WHERE MaND = ?")) {
            ps.setString(1, maND);
            try (ResultSet rs = ps.executeQuery()) {
                exists = rs.next();
            }
        }

        if (!exists) {
            try (PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO KHACHHANG
                        (MaKH, LoaiKH, MaHangThanhVien, TongChiTieu, CapNhatLanCuoi, MaND)
                    VALUES (?, 'VIP', 'HTV00', 0, CURRENT_TIMESTAMP, ?)
                    """)) {
                ps.setString(1, "KH_ADMIN");
                ps.setString(2, maND);
                ps.executeUpdate();
            }
        }
    }

    private static void ensureAdminEmployee(Connection conn, String maND, DbLabels labels) throws Exception {
        boolean exists;
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM NHANVIEN WHERE MaND = ?")) {
            ps.setString(1, maND);
            try (ResultSet rs = ps.executeQuery()) {
                exists = rs.next();
            }
        }

        if (!exists) {
            try (PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO NHANVIEN
                        (MaNV, LoaiNV, NgayVaoLam, TrangThaiLamViec, CaLamViec, LuongCoBan, MaND, MaCN)
                    VALUES (?, ?, CURRENT_DATE, ?, ?, 99999999, ?, NULL)
                    """)) {
                ps.setString(1, "NV_ADMIN");
                ps.setString(2, labels.loaiNhanVienQuanLy());
                ps.setString(3, "Đang làm việc");
                ps.setString(4, "Hành chính");
                ps.setString(5, maND);
                ps.executeUpdate();
            }
        }
    }

    private static void ensureAdminRoleMapping(Connection conn, String maND) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM CHITIETVAITRO WHERE MaND = ?")) {
            ps.setString(1, maND);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO CHITIETVAITRO (MaND, MaVaiTro, MoTa) VALUES (?, 'VT01', ?)")) {
            ps.setString(1, maND);
            ps.setString(2, "Quyền quản trị tối cao của hệ thống");
            ps.executeUpdate();
        }
    }

    public static void main(String[] args) {
        initialize();
    }

    private static DbLabels loadDbLabels(Connection conn) {
        List<String> trangThaiND = layGiaTriRangBuoc(conn, "CHK_ND_TRANGTHAI");
        List<String> gioiTinh = layGiaTriRangBuoc(conn, "CHK_ND_GIOITINH");
        List<String> loaiNV = layGiaTriRangBuoc(conn, "CHK_NV_LOAINV");

        return new DbLabels(
                pick(trangThaiND, "dang hoat dong", 0, "Đang hoạt động"),
                pick(gioiTinh, "khac", Math.max(0, gioiTinh.size() - 1), "Khác"),
                pick(loaiNV, "quan ly", Math.min(1, Math.max(0, loaiNV.size() - 1)), "Quản lý")
        );
    }

    private static List<String> layGiaTriRangBuoc(Connection conn, String constraintName) {
        List<String> values = new ArrayList<>();
        String sql = """
                SELECT search_condition_vc
                FROM user_constraints
                WHERE constraint_name = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, constraintName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Matcher matcher = Pattern.compile("'([^']*)'").matcher(rs.getString(1));
                    while (matcher.find()) {
                        values.add(matcher.group(1));
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("[SuperAdminCreator] Không đọc được constraint " + constraintName + ": " + ex.getMessage());
        }
        return values;
    }

    private static String pick(List<String> values, String normalizedNeedle, int fallbackIndex, String fallbackValue) {
        for (String value : values) {
            if (chuanHoa(value).contains(normalizedNeedle)) {
                return value;
            }
        }
        if (!values.isEmpty()) {
            int index = Math.max(0, Math.min(fallbackIndex, values.size() - 1));
            return values.get(index);
        }
        return fallbackValue;
    }

    private static String chuanHoa(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record DbLabels(
            String trangThaiNguoiDungHoatDong,
            String gioiTinhKhac,
            String loaiNhanVienQuanLy
    ) {
    }
}
