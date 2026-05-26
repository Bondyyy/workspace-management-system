package com.wms.manual;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("manual")
@Disabled("Manual Oracle test: opens a real PHIENLAMVIEC for KG0004 in the configured database.")
class MoPhienTrucTiepOracleManualTest {

    @Test
    void moPhienKg0004HaiGioTheoDuLieuAnh() throws Exception {
        Properties db = new Properties();
        try (FileInputStream fs = new FileInputStream("db.properties")) {
            db.load(fs);
        }

        try (Connection conn = DriverManager.getConnection(
                db.getProperty("url"),
                db.getProperty("user"),
                db.getProperty("pass"))) {
            conn.createStatement().execute("ALTER SESSION SET TIME_ZONE = '+07:00'");

            String maKH = timMaKhachHang(conn, "0912345679");
            assertNotNull(maKH, "Phai tim thay MaKH tu so dien thoai 0912345679.");

            try {
                String out = goiMoPhien(conn, maKH);
                assertTrue(
                        out != null && out.toLowerCase(Locale.ROOT).contains("thanh cong"),
                        "p_outMessage phai chua 'thanh cong', thuc te: " + out);
            } finally {
                donBanGhiTest(conn);
            }
        }
    }

    private String timMaKhachHang(Connection conn, String sdt) throws Exception {
        String sql = """
                SELECT kh.MaKH
                FROM KHACHHANG kh
                JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND
                WHERE nd.SDT = ?
                FETCH FIRST 1 ROW ONLY
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sdt);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private String goiMoPhien(Connection conn, String maKH) throws Exception {
        String call = "{call sp_MoPhienLamViecTrucTiep(?, ?, ?, ?, ?, ?, ?)}";
        LocalDateTime batDau = LocalDateTime.of(2026, 5, 26, 13, 54);
        LocalDateTime ketThuc = batDau.plusHours(2);
        try (CallableStatement cs = conn.prepareCall(call)) {
            cs.setString(1, "KG0004");
            cs.setString(2, maKH);
            cs.setTimestamp(3, Timestamp.valueOf(batDau));
            cs.setTimestamp(4, Timestamp.valueOf(ketThuc));
            cs.setString(5, "TEST_PH_KG0004");
            cs.setNull(6, java.sql.Types.VARCHAR);
            cs.registerOutParameter(7, java.sql.Types.VARCHAR);
            cs.execute();
            return cs.getString(7);
        }
    }

    private void donBanGhiTest(Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("""
                UPDATE PHIENLAMVIEC
                SET TrangThaiPhien = 'Đã kết thúc',
                    ThoiGianKetThuc = NVL(ThoiGianKetThuc, CURRENT_TIMESTAMP)
                WHERE MaPhien = 'TEST_PH_KG0004'
                """)) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM HOADON WHERE MaPhien = 'TEST_PH_KG0004'")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM PHIENLAMVIEC WHERE MaPhien = 'TEST_PH_KG0004'")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("""
                UPDATE KHONGGIAN kg
                SET TrangThaiKG = 'Trống'
                WHERE kg.MaKG = 'KG0004'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM PHIENLAMVIEC p
                      WHERE p.MaKG = kg.MaKG
                        AND p.TrangThaiPhien = 'Đang hoạt động'
                  )
                """)) {
            ps.executeUpdate();
        }
    }
}
