package com.wms.util;

import com.wms.web.repository.CongThongTinWebRepository;
import com.wms.web.scheduler.LichKiemTraThanhToanDatCho;
import com.wms.web.service.CongThongTinService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class NoShowSchedulerTest {

    private JdbcTemplate getJdbcTemplate() throws Exception {
        Properties p = new Properties();
        try (FileInputStream fs = new FileInputStream("db.properties")) {
            p.load(fs);
        }
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setUrl(p.getProperty("url"));
        dataSource.setUsername(p.getProperty("user"));
        dataSource.setPassword(p.getProperty("pass"));
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("ALTER SESSION SET TIME_ZONE = '+07:00'");
        return jdbcTemplate;
    }

    @Test
    public void testNoShowScheduler() throws Exception {
        System.out.println("\n=== HỆ THỐNG KIỂM TRA ĐẶT CHỖ QUÁ HẠN (NO SHOW) ===");
        JdbcTemplate jdbcTemplate = getJdbcTemplate();

        // 0. Biên dịch trigger lên Database kiểm thử để đảm bảo đồng bộ
        try {
            String triggerSql = """
                CREATE OR REPLACE TRIGGER trg_VoHieuQRSauNhanCho
                BEFORE UPDATE OF TrangThaiDatTruoc ON DATCHO
                FOR EACH ROW
                WHEN (NEW.TrangThaiDatTruoc = 'Đã sử dụng' AND OLD.TrangThaiDatTruoc = 'Đã thanh toán thành công')
                BEGIN
                    :NEW.MaQR := NULL;
                    IF :NEW.GhiChu IS NULL OR INSTR(:NEW.GhiChu, '[SYSTEM_NO_SHOW]') = 0 THEN
                        :NEW.GhiChu := NVL(:NEW.GhiChu, '') || ' | Khách đã nhận chỗ. QR bị vô hiệu hóa.';
                    END IF;
                    :NEW.CapNhatLanCuoi := CURRENT_TIMESTAMP;
                END;
                """;
            jdbcTemplate.execute(triggerSql);
            System.out.println("[Test Setup] Đã biên dịch thành công TRG_VoHieuQRSauNhanCho lên Database kiểm thử.");
        } catch (Exception e) {
            System.err.println("[-] Không thể biên dịch trigger TRG_VoHieuQRSauNhanCho: " + e.getMessage());
            fail("Biên dịch trigger thất bại: " + e.getMessage());
        }

        CongThongTinWebRepository repository = new CongThongTinWebRepository(jdbcTemplate);
        CongThongTinService service = new CongThongTinService(repository);
        LichKiemTraThanhToanDatCho scheduler = new LichKiemTraThanhToanDatCho(service);

        // 1. Lấy động Khách Hàng và Không Gian có sẵn trong DB để test
        String maKH = null;
        String maKG = null;
        try {
            maKH = jdbcTemplate.queryForObject("SELECT MaKH FROM (SELECT MaKH FROM KHACHHANG ORDER BY MaKH) WHERE ROWNUM = 1", String.class);
            maKG = jdbcTemplate.queryForObject("SELECT MaKG FROM (SELECT MaKG FROM KHONGGIAN ORDER BY MaKG) WHERE ROWNUM = 1", String.class);
        } catch (Exception e) {
            System.err.println("[-] Không thể lấy Khách hàng/Không gian từ DB: " + e.getMessage());
            fail("DB trống hoặc chưa khởi tạo bảng KHACHHANG/KHONGGIAN");
        }

        System.out.println("[Test Setup] Sử dụng MaKH=" + maKH + ", MaKG=" + maKG + " để chạy thử nghiệm.");

        // 2. Tạo đặt chỗ mẫu quá hạn (Đã thanh toán thành công)
        jdbcTemplate.update(
            "INSERT INTO DATCHO (ThoiGianDat, ThoiGianDuKienToi, KhoangThoiGianSuDung, TrangThaiDatTruoc, ThanhTien, GhiChu, CapNhatLanCuoi, MaKH, MaKG, MaQR) " +
            "VALUES (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3' HOUR, 2, 'Đã thanh toán thành công', 100000, 'Test no show', CURRENT_TIMESTAMP, ?, ?, 'MOCK_QR_CODE')",
            maKH, maKG
        );

        // Tìm mã đặt chỗ vừa tạo
        String maDatCho = jdbcTemplate.queryForObject(
            "SELECT MaDatCho FROM (SELECT MaDatCho FROM DATCHO WHERE MaKH = ? ORDER BY ThoiGianDat DESC) WHERE ROWNUM = 1",
            String.class, maKH
        );

        System.out.println("[Test Setup] Đã tạo thành công đặt chỗ giả lập: MaDatCho=" + maDatCho);

        // Khóa không gian giả lập (trạng thái ban đầu: Đã đặt trước)
        jdbcTemplate.update("UPDATE KHONGGIAN SET TrangThaiKG = 'Đã đặt trước' WHERE MaKG = ?", maKG);

        String testCleanupMaDatCho = maDatCho;
        String testCleanupMaKG = maKG;

        try {
            // Kiểm tra thông tin TRƯỚC khi chạy Scheduler
            System.out.println("\n--- TRƯỚC KHI CHẠY SCHEDULER ---");
            printBookingAndSpaceInfo(jdbcTemplate, maDatCho);

            // 3. Chạy Scheduler
            System.out.println("\n--- CHẠY SCHEDULER ---");
            scheduler.kiemTraDatCho();

            // Kiểm tra thông tin SAU KHI chạy Scheduler
            System.out.println("\n--- SAU KHI CHẠY SCHEDULER ---");
            Map<String, Object> result = printBookingAndSpaceInfo(jdbcTemplate, maDatCho);

            // 4. Khẳng định kết quả (Assertions)
            assertNotNull(result, "Phải tìm thấy đặt chỗ trong cơ sở dữ liệu");
            assertEquals(com.wms.config.AppConstants.TRANG_THAI_DAT_CHO_QUA_HAN, result.get("TrangThaiDatTruoc"),
                    "Scheduler phải đặt trạng thái 'Quá hạn nhận chỗ'");
            assertNull(result.get("MaQR"), "Mã QR phải bị vô hiệu hóa (set NULL)");
            
            String ghiChu = (String) result.get("GhiChu");
            assertNotNull(ghiChu, "Ghi chú không được rỗng");
            assertTrue(ghiChu.contains("[SYSTEM_NO_SHOW]"), "Ghi chú phải chứa dấu hiệu '[SYSTEM_NO_SHOW]'");
            assertFalse(ghiChu.contains("Khách đã nhận chỗ"), "Ghi chú KHÔNG được chứa chuỗi 'Khách đã nhận chỗ' (chứng minh trigger TRG_VoHieuQRSauNhanCho hoạt động đúng)");

            String trangThaiKG = (String) result.get("TrangThaiKG");
            assertEquals("Trống", trangThaiKG, "Trạng thái không gian phải được tự động giải phóng về 'Trống'");
            
            System.out.println("\n[SUCCESS] Tất cả kiểm tra khẳng định (Assertions) đều đạt kết quả mong muốn!");
        } finally {
            // 5. Dọn dẹp dữ liệu test (Cleanup)
            System.out.println("\n[Test Cleanup] Đang xóa đặt chỗ giả lập " + testCleanupMaDatCho + "...");
            jdbcTemplate.update("DELETE FROM DATCHO WHERE MaDatCho = ?", testCleanupMaDatCho);
            jdbcTemplate.update("UPDATE KHONGGIAN SET TrangThaiKG = 'Trống' WHERE MaKG = ?", testCleanupMaKG);
            System.out.println("[Test Cleanup] Dọn dẹp dữ liệu hoàn tất.");
        }
    }

    private Map<String, Object> printBookingAndSpaceInfo(JdbcTemplate jdbcTemplate, String maDatCho) {
        String sql = """
            SELECT 
                dc.MaDatCho,
                dc.TrangThaiDatTruoc,
                dc.ThoiGianDuKienToi,
                dc.KhoangThoiGianSuDung,
                dc.MaQR,
                dc.GhiChu,
                dc.MaKG,
                kg.TrangThaiKG,
                SYSTIMESTAMP AS GioHeThong,
                dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR') AS GioKetThucDuKien,
                CASE 
                    WHEN SYSTIMESTAMP > dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR')
                    THEN 'DA_QUA_GIO'
                    ELSE 'CHUA_QUA_GIO'
                END AS TrangThaiQuaGio,
                (
                    SELECT COUNT(*)
                    FROM PHIENLAMVIEC plv
                    WHERE plv.MaDatCho = dc.MaDatCho
                ) AS SoPhien
            FROM DATCHO dc
            JOIN KHONGGIAN kg ON kg.MaKG = dc.MaKG
            WHERE dc.MaDatCho = ?
            """;
        
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, maDatCho);
        if (list.isEmpty()) {
            System.out.println("[-] Không tìm thấy đặt chỗ: " + maDatCho);
            return null;
        }
        
        Map<String, Object> row = list.get(0);
        System.out.println("Mã đặt chỗ          : " + row.get("MaDatCho"));
        System.out.println("Trạng thái đặt trước: " + row.get("TrangThaiDatTruoc"));
        System.out.println("Thời gian dự kiến tới: " + row.get("ThoiGianDuKienToi"));
        System.out.println("Khoảng thời gian SD : " + row.get("KhoangThoiGianSuDung"));
        System.out.println("Mã QR               : " + row.get("MaQR"));
        System.out.println("Ghi chú             : " + row.get("GhiChu"));
        System.out.println("Mã không gian (MaKG): " + row.get("MaKG"));
        System.out.println("Trạng thái không gian: " + row.get("TrangThaiKG"));
        System.out.println("Giờ hệ thống        : " + row.get("GioHeThong"));
        System.out.println("Giờ kết thúc dự kiến: " + row.get("GioKetThucDuKien"));
        System.out.println("Trạng thái quá giờ  : " + row.get("TrangThaiQuaGio"));
        System.out.println("Số phiên làm việc   : " + row.get("SoPhien"));
        return row;
    }
}
