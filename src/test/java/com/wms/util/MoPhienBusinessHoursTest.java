package com.wms.util;

import com.wms.dao.TrangChuQuanLy.QuanLyPhien.PhienLamViecDAO;
import com.wms.service.TrangChuQuanLy.QuanLyPhien.PhienLamViecService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class MoPhienBusinessHoursTest {

    private JdbcTemplate jdbcTemplate;
    private DriverManagerDataSource dataSource;
    private String testBranchId = "TEST_CN_01";
    private String testSpaceId = "TEST_KG_01";
    private String testCustomerId = "TEST_KH_01";

    private JdbcTemplate getJdbcTemplate() throws Exception {
        Properties p = new Properties();
        try (FileInputStream fs = new FileInputStream("db.properties")) {
            p.load(fs);
        }
        dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setUrl(p.getProperty("url"));
        dataSource.setUsername(p.getProperty("user"));
        dataSource.setPassword(p.getProperty("pass"));
        
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        jt.execute("ALTER SESSION SET TIME_ZONE = '+07:00'");
        return jt;
    }

    @BeforeEach
    public void setUp() throws Exception {
        jdbcTemplate = getJdbcTemplate();

        System.out.println("\n=== THIẾT LẬP KIỂM THỬ: ĐỒNG BỘ GIỜ HOẠT ĐỘNG ===");

        // 1. Biên dịch Procedure SP_MoPhienLamViecTrucTiep
        try {
            String sqlPath = "Database/04_procedures/QuanLyPhien/SP_MoPhienLamViecTrucTiep.sql";
            String procSql = Files.readString(Path.of(sqlPath)).trim();
            // Loại bỏ dấu gạch chéo '/' cuối file nếu có để JDBC thực thi mượt mà
            if (procSql.endsWith("/")) {
                procSql = procSql.substring(0, procSql.length() - 1).trim();
            }
            jdbcTemplate.execute(procSql);
            System.out.println("[Test Setup] Đã biên dịch thành công SP_MoPhienLamViecTrucTiep lên CSDL.");
        } catch (Exception e) {
            System.err.println("[-] Biên dịch procedure thất bại: " + e.getMessage());
            fail("Biên dịch SP_MoPhienLamViecTrucTiep thất bại: " + e.getMessage());
        }

        // Dọn dẹp trước phòng hờ
        cleanupData();

        // 2. Tạo Chi nhánh kiểm thử mặc định: mở 07:00, đóng 22:00
        jdbcTemplate.update(
            "INSERT INTO CHINHANH (MaCN, TenCN, DiaChi, ThoiGianMoCua, ThoiGianDongCua, DuongDayNong, TrangThai) " +
            "VALUES (?, 'Chi nhanh Kiem thu Gio Hoat Dong', 'Test Address', '07:00', '22:00', '19001000', 'Đang hoạt động')",
            testBranchId
        );

        // 3. Tạo Loại không gian và Không gian kiểm thử
        // Lấy loại không gian đầu tiên có sẵn trong DB để liên kết
        String maLoaiKG = null;
        try {
            maLoaiKG = jdbcTemplate.queryForObject("SELECT MaLoaiKG FROM LOAIKHONGGIAN ORDER BY MaLoaiKG FETCH FIRST 1 ROW ONLY", String.class);
        } catch (Exception e) {
            // Nếu chưa có loại không gian, tạo mới một loại
            maLoaiKG = "LKG001";
            jdbcTemplate.update(
                "INSERT INTO LOAIKHONGGIAN (MaLoaiKG, TenLoaiKG, SucChua, DonGiaTheoGio, TrangThai) VALUES (?, 'Test Type', 4, 50000, 'Đang hoạt động')",
                maLoaiKG
            );
        }

        jdbcTemplate.update(
            "INSERT INTO KHONGGIAN (MaKG, TenKG, ToaDoX, ToaDoY, ChieuDai, ChieuRong, TrangThaiKG, MaCN, MaLoaiKG) " +
            "VALUES (?, 'Ban Kiem Thu 1', 1, 1, 1, 1, 'Trống', ?, ?)",
            testSpaceId, testBranchId, maLoaiKG
        );

        // 4. Tạo Khách hàng kiểm thử
        String testUserId = "TEST_ND_01";
        jdbcTemplate.update(
            "INSERT INTO NGUOIDUNG (MaND, HoTen, TenTaiKhoan, Email, SDT, TrangThaiND) " +
            "VALUES (?, 'Khach Hang Kiem Thu', 'testuser123', 'test123@gmail.com', '0909999999', 'Đang hoạt động')",
            testUserId
        );

        jdbcTemplate.update(
            "INSERT INTO KHACHHANG (MaKH, LoaiKH, TongChiTieu, CapNhatLanCuoi, MaHangThanhVien, MaND) VALUES (?, 'Khách vãng lai', 0, CURRENT_TIMESTAMP, NULL, ?)",
            testCustomerId, testUserId
        );

        System.out.println("[Test Setup] Đã tạo thành công dữ liệu mock: Chi nhánh mở 07:00 - đóng 22:00.");
    }

    @AfterEach
    public void tearDown() {
        cleanupData();
    }

    private void cleanupData() {
        System.out.println("[Test Cleanup] Bat dau don dep du lieu...");
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM HOADON WHERE MaPhien IN (SELECT MaPhien FROM PHIENLAMVIEC WHERE MaKG = ?)",
                Integer.class, testSpaceId
            );
            if (count != null && count > 0) {
                try {
                    jdbcTemplate.update("UPDATE PHIENLAMVIEC SET TrangThaiPhien = 'Đã kết thúc' WHERE MaKG = ?", testSpaceId);
                } catch (Exception e) {
                    System.err.println("[-] Khong the cap nhat trang thai phien: " + e.getMessage());
                }
                try {
                    jdbcTemplate.update("DELETE FROM HOADON WHERE MaPhien IN (SELECT MaPhien FROM PHIENLAMVIEC WHERE MaKG = ?)", testSpaceId);
                } catch (Exception e) {
                    System.err.println("[-] Khong the xoa hoa don: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[-] Kiem tra hoa don that bai: " + e.getMessage());
        }

        try {
            jdbcTemplate.update("DELETE FROM PHIENLAMVIEC WHERE MaKG = ?", testSpaceId);
        } catch (Exception e) {
            System.err.println("[-] Khong the xoa phien lam viec: " + e.getMessage());
        }

        try {
            jdbcTemplate.update("DELETE FROM KHACHHANG WHERE MaKH = ?", testCustomerId);
        } catch (Exception e) {
            System.err.println("[-] Khong the xoa khach hang: " + e.getMessage());
        }

        try {
            jdbcTemplate.update("DELETE FROM NGUOIDUNG WHERE MaND = ?", "TEST_ND_01");
        } catch (Exception e) {
            System.err.println("[-] Khong the xoa nguoi dung: " + e.getMessage());
        }

        try {
            jdbcTemplate.update("DELETE FROM KHONGGIAN WHERE MaKG = ?", testSpaceId);
        } catch (Exception e) {
            System.err.println("[-] Khong the xoa khong gian: " + e.getMessage());
        }

        try {
            jdbcTemplate.update("DELETE FROM CHINHANH WHERE MaCN = ?", testBranchId);
        } catch (Exception e) {
            System.err.println("[-] Khong the xoa chi nhanh: " + e.getMessage());
        }

        System.out.println("[Test Cleanup] Da hoan tat don dep du lieu kiem thu.");
    }

    @Test
    public void case1_NormalHoursAllowsEndAfterClose() {
        System.out.println("\n--- CASE 1: 07:00 -> 22:00, mở 20:00 dùng 4 giờ phải thành công ---");

        ZonedDateTime nowHcm = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        ZonedDateTime batDau = nowHcm.withHour(20).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime ketThuc = batDau.plusHours(4);

        String result = callStoredProcedure(testSpaceId, testCustomerId, batDau, ketThuc, "TEST_PH_01", null);
        System.out.println("[Result] " + result);
        assertTrue(result.contains("thanh cong"), "Mở phiên 20:00 -> 00:00 phải thành công.");
        assertFalse(result.toLowerCase().contains("vuot qua gio dong cua"),
                "Không được báo lỗi vượt quá giờ đóng cửa.");
    }

    @Test
    public void case2_NormalHoursBlocksStartAfterClose() {
        System.out.println("\n--- CASE 2: 07:00 -> 22:00, mở 23:00 phải bị chặn ---");
        
        ZonedDateTime nowHcm = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        ZonedDateTime batDau = nowHcm.withHour(23).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime ketThuc = batDau.plusHours(1);

        String result = callStoredProcedure(testSpaceId, testCustomerId, batDau, ketThuc, "TEST_PH_02", null);
        System.out.println("[Result] " + result);
        assertTrue(result.contains("Loi: Chi nhanh da qua gio hoat dong"), 
                "Mở phiên 23:00 phải bị chặn vì bắt đầu ngoài giờ hoạt động.");
    }

    @Test
    public void case3_OvernightAllowsStartAt23h() {
        System.out.println("\n--- CASE 3: 22:00 -> 06:00, mở 23:00 dùng 4 giờ phải thành công ---");
        jdbcTemplate.update("UPDATE CHINHANH SET ThoiGianMoCua = '22:00', ThoiGianDongCua = '06:00' WHERE MaCN = ?",
                testBranchId);

        ZonedDateTime nowHcm = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        ZonedDateTime batDau = nowHcm.withHour(23).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime ketThuc = batDau.plusHours(4);

        String result = callStoredProcedure(testSpaceId, testCustomerId, batDau, ketThuc, "TEST_PH_03", null);
        System.out.println("[Result] " + result);
        assertTrue(result.contains("thanh cong"), "Mở phiên 23:00 trong ca qua đêm phải thành công.");
    }

    @Test
    public void case4_OvernightAllowsStartAt01h() {
        System.out.println("\n--- CASE 4: 22:00 -> 06:00, mở 01:00 dùng 4 giờ phải thành công ---");
        jdbcTemplate.update("UPDATE CHINHANH SET ThoiGianMoCua = '22:00', ThoiGianDongCua = '06:00' WHERE MaCN = ?",
                testBranchId);

        ZonedDateTime nowHcm = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        ZonedDateTime batDau = nowHcm.withHour(1).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime ketThuc = batDau.plusHours(4);

        String result = callStoredProcedure(testSpaceId, testCustomerId, batDau, ketThuc, "TEST_PH_04", null);
        System.out.println("[Result] " + result);
        assertTrue(result.contains("thanh cong"), "Mở phiên 01:00 trong ca qua đêm phải thành công.");
    }

    @Test
    public void case5_OvernightBlocksMidday() {
        System.out.println("\n--- CASE 5: 22:00 -> 06:00, mở 12:00 phải bị chặn ---");
        jdbcTemplate.update("UPDATE CHINHANH SET ThoiGianMoCua = '22:00', ThoiGianDongCua = '06:00' WHERE MaCN = ?",
                testBranchId);

        ZonedDateTime nowHcm = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        ZonedDateTime batDau = nowHcm.withHour(12).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime ketThuc = batDau.plusHours(1);

        String result = callStoredProcedure(testSpaceId, testCustomerId, batDau, ketThuc, "TEST_PH_05", null);
        System.out.println("[Result] " + result);
        assertTrue(result.contains("Loi: Chi nhanh da qua gio hoat dong")
                        || result.contains("Loi: Chi nhanh chua den gio mo cua"),
                "Mở phiên lúc 12:00 phải bị chặn cho chi nhánh 22:00 -> 06:00.");
    }

    @Test
    public void case6_EqualMidnightMeansOpen24h() {
        System.out.println("\n--- CASE 6: 00:00 -> 00:00, mở 10:00 dùng 20 giờ phải thành công ---");
        jdbcTemplate.update("UPDATE CHINHANH SET ThoiGianMoCua = '00:00', ThoiGianDongCua = '00:00' WHERE MaCN = ?",
                testBranchId);

        ZonedDateTime nowHcm = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        ZonedDateTime batDau = nowHcm.withHour(10).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime ketThuc = batDau.plusHours(20);

        String result = callStoredProcedure(testSpaceId, testCustomerId, batDau, ketThuc, "TEST_PH_06", null);
        System.out.println("[Result] " + result);
        assertTrue(result.contains("thanh cong"), "00:00 -> 00:00 phải được hiểu là 24/24.");
    }

    @Test
    public void case7_MidnightTo24hMeansOpen24h() {
        System.out.println("\n--- CASE 7: 00:00 -> 24:00, mở 23:00 dùng 4 giờ phải thành công ---");
        assertTrue(BusinessHoursUtil.isOpen24h("00:00", "24:00"),
                "Helper phải hiểu 00:00 -> 24:00 là 24/24.");
        assertTrue(BusinessHoursUtil.isStartWithinBusinessHours(
                ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).withHour(23).withMinute(0).withSecond(0).withNano(0)
                        .toLocalDateTime(),
                BusinessHoursUtil.parseBranchTime("00:00", java.time.LocalTime.MIDNIGHT),
                BusinessHoursUtil.parseBranchTime("24:00", java.time.LocalTime.MIDNIGHT)));

        try {
            jdbcTemplate.update("UPDATE CHINHANH SET ThoiGianMoCua = '00:00', ThoiGianDongCua = '24:00' WHERE MaCN = ?",
                    testBranchId);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false,
                    "DB hiện tại có CHK_CN_DONGCUA không cho lưu 24:00; không ALTER TABLE theo yêu cầu.");
        }

        ZonedDateTime nowHcm = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        ZonedDateTime batDau = nowHcm.withHour(23).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime ketThuc = batDau.plusHours(4);

        String result = callStoredProcedure(testSpaceId, testCustomerId, batDau, ketThuc, "TEST_PH_07", null);
        System.out.println("[Result] " + result);
        assertTrue(result.contains("thanh cong"), "00:00 -> 24:00 phải được hiểu là 24/24.");
    }

    private String callStoredProcedure(String maKG, String maKH, ZonedDateTime batDau, ZonedDateTime ketThuc, String maPhien, String maDatCho) {
        String sql = "{call sp_MoPhienLamViecTrucTiep(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = dataSource.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, maKG);
            cstmt.setString(2, maKH);
            cstmt.setTimestamp(3, Timestamp.from(batDau.toInstant()));
            cstmt.setTimestamp(4, Timestamp.from(ketThuc.toInstant()));
            cstmt.setString(5, maPhien);
            cstmt.setString(6, maDatCho);
            cstmt.registerOutParameter(7, java.sql.Types.VARCHAR);

            cstmt.execute();
            return cstmt.getString(7);
        } catch (Exception e) {
            return "Lỗi thực thi SP: " + e.getMessage();
        }
    }
}
