package com.wms.web.controller;

import com.wms.config.DatabaseConnection;
import com.wms.web.form.YeuCauWebhookThanhToan;
import com.wms.web.service.CongThongTinService;
import com.wms.web.repository.CongThongTinWebRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class StandaloneWebhookVerifier {

    private static final String TEST_MA_DAT_CHO = "DC999999";
    private static String testMaKG = null;
    private static String testMaKH = null;
    private static String originalKhongGianStatus = null;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("[Verifier] BẮT ĐẦU CHẠY LIGHTWEIGHT STANDALONE WEBHOOK VERIFIER...");
        System.out.println("================================================================================");

        Connection conn = null;
        try {
            // 1. Lấy Connection từ pool có sẵn
            conn = DatabaseConnection.getInstance().getConnection();
            if (conn == null) {
                System.err.println("[Verifier] -> THẤT BẠI: Không thể lấy kết nối database!");
                return;
            }
            System.out.println("[Verifier] -> Lấy kết nối CSDL thành công.");

            // 2. Tạo Spring DataSource từ Connection duy nhất này
            SingleConnectionDataSource dataSource = new SingleConnectionDataSource(conn, true);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // 3. Khởi tạo Repository và Service
            CongThongTinWebRepository repository = new CongThongTinWebRepository(jdbcTemplate);
            CongThongTinService service = new CongThongTinService(repository);

            // 4. Thiết lập dữ liệu test ban đầu
            setUpTestData(jdbcTemplate);

            // ========================================================================
            // TEST CASE 1: Webhook hợp lệ chuyển DATCHO thành công
            // ========================================================================
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("[TEST CASE 1] Gửi webhook hợp lệ...");
            
            YeuCauWebhookThanhToan request1 = new YeuCauWebhookThanhToan();
            request1.setMaGiaoDich("TX_STANDALONE_111");
            request1.setSoTien(new BigDecimal("100000"));
            request1.setNoiDung("Chuyen khoan thanh toan cho ma dat cho " + TEST_MA_DAT_CHO);
            request1.setTrangThai("SUCCESS");
            request1.setThoiGianThanhToan(LocalDateTime.now());

            CongThongTinService.KetQuaWebhookThanhToan result1 = service.xuLyWebhookThanhToan(request1);
            System.out.println("[TEST CASE 1] Kết quả từ Service: " + result1.success() + " - " + result1.message());
            
            // Xác thực trong database
            String statusAfter1 = jdbcTemplate.queryForObject(
                    "SELECT TrangThaiDatTruoc FROM DATCHO WHERE MaDatCho = ?", String.class, TEST_MA_DAT_CHO);
            String kgStatusAfter1 = jdbcTemplate.queryForObject(
                    "SELECT TrangThaiKG FROM KHONGGIAN WHERE MaKG = ?", String.class, testMaKG);

            System.out.println("[TEST CASE 1] Trạng thái DATCHO trong DB: \"" + statusAfter1 + "\"");
            System.out.println("[TEST CASE 1] Trạng thái KHONGGIAN trong DB: \"" + kgStatusAfter1 + "\"");

            if (result1.success() && statusAfter1 != null && statusAfter1.contains("thành công") && kgStatusAfter1.contains("trước")) {
                System.out.println("[TEST CASE 1] -> THÀNH CÔNG: Đã cập nhật thanh toán và không gian!");
            } else {
                throw new RuntimeException("[TEST CASE 1] -> THẤT BẠI: Trạng thái cập nhật không đúng!");
            }

            // ========================================================================
            // TEST CASE 2: Gọi lại webhook cùng giao dịch -> không xử lý trùng (Idempotent)
            // ========================================================================
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("[TEST CASE 2] Gửi lại webhook trùng mã giao dịch (Idempotency)...");

            CongThongTinService.KetQuaWebhookThanhToan result2 = service.xuLyWebhookThanhToan(request1);
            System.out.println("[TEST CASE 2] Kết quả từ Service: " + result2.success() + " - " + result2.message());

            if (result2.success() && result2.message().contains("đã được xử lý trước đó")) {
                System.out.println("[TEST CASE 2] -> THÀNH CÔNG: Đã bỏ qua giao dịch trùng lặp một cách an toàn.");
            } else {
                throw new RuntimeException("[TEST CASE 2] -> THẤT BẠI: Không nhận diện hoặc xử lý sai giao dịch trùng!");
            }

            // ========================================================================
            // TEST CASE 3: Scheduler chạy sau khi DATCHO đã thành công -> không gửi email hủy
            // ========================================================================
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("[TEST CASE 3] Scheduler chạy sau khi đặt chỗ đã Đã thanh toán thành công...");

            // Cập nhật ThoiGianDat lùi về 15 phút trước để giả lập quá hạn thanh toán
            jdbcTemplate.update(
                    "UPDATE DATCHO SET ThoiGianDat = CURRENT_TIMESTAMP - INTERVAL '15' MINUTE WHERE MaDatCho = ?",
                    TEST_MA_DAT_CHO
            );

            // Kích hoạt Scheduler
            System.out.println("[TEST CASE 3] Chạy hetHanDatChoChoThanhToan()...");
            service.hetHanDatChoChoThanhToan();

            // Xác minh DATCHO vẫn ở trạng thái Đã thanh toán thành công
            String statusAfter3 = jdbcTemplate.queryForObject(
                    "SELECT TrangThaiDatTruoc FROM DATCHO WHERE MaDatCho = ?", String.class, TEST_MA_DAT_CHO);
            System.out.println("[TEST CASE 3] Trạng thái DATCHO trong DB hiện tại: \"" + statusAfter3 + "\"");

            if (statusAfter3 != null && statusAfter3.contains("thành công")) {
                System.out.println("[TEST CASE 3] -> THÀNH CÔNG: Scheduler bỏ qua đặt chỗ thành công một cách chính xác!");
            } else {
                throw new RuntimeException("[TEST CASE 3] -> THẤT BẠI: Scheduler đã hủy nhầm đặt chỗ đã thanh toán!");
            }

            // ========================================================================
            // BỔ SUNG: Kiểm tra Scheduler hoạt động chính xác khi hết hạn thực sự
            // ========================================================================
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("[TEST CASE BỔ SUNG] Reset trạng thái chờ thanh toán để kiểm tra Scheduler hủy...");
            
            String trangThaiChoThanhToan = getDbStatusValue(jdbcTemplate, "CHK_DC_TRANGTHAI", "cho thanh toan", "Đang chờ thanh toán");
            jdbcTemplate.update("UPDATE DATCHO SET TrangThaiDatTruoc = ? WHERE MaDatCho = ?", trangThaiChoThanhToan, TEST_MA_DAT_CHO);

            System.out.println("[TEST CASE BỔ SUNG] Chạy hetHanDatChoChoThanhToan()...");
            service.hetHanDatChoChoThanhToan();

            String statusAfter4 = jdbcTemplate.queryForObject(
                    "SELECT TrangThaiDatTruoc FROM DATCHO WHERE MaDatCho = ?", String.class, TEST_MA_DAT_CHO);
            System.out.println("[TEST CASE BỔ SUNG] Trạng thái DATCHO trong DB hiện tại: \"" + statusAfter4 + "\"");

            if (statusAfter4 != null && statusAfter4.contains("không thành công")) {
                System.out.println("[TEST CASE BỔ SUNG] -> THÀNH CÔNG: Scheduler đã hủy thành công đặt chỗ hết hạn thực sự.");
            } else {
                throw new RuntimeException("[TEST CASE BỔ SUNG] -> THẤT BẠI: Scheduler không hủy đặt chỗ hết hạn.");
            }

            System.out.println("\n================================================================================");
            System.out.println("[Verifier] >>> TẤT CẢ CÁC TEST CASES ĐÃ ĐẠT (PASS) 100% <<<");
            System.out.println("================================================================================");

        } catch (Exception e) {
            System.err.println("\n[Verifier] !!! CÓ LỖI XẢY RA TRONG QUÁ TRÌNH VERIFY !!!");
            e.printStackTrace();
        } finally {
            // Dọn dẹp dữ liệu test
            if (conn != null) {
                try {
                    SingleConnectionDataSource dataSource = new SingleConnectionDataSource(conn, true);
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                    cleanUpTestData(jdbcTemplate);
                } catch (Exception e) {
                    System.err.println("[Verifier] Lỗi dọn dẹp dữ liệu: " + e.getMessage());
                }
            }
            DatabaseConnection.getInstance().shutdown();
        }
    }

    private static void setUpTestData(JdbcTemplate jdbcTemplate) {
        // 1. Dọn sạch dữ liệu cũ
        cleanUpTestData(jdbcTemplate);

        // 2. Lấy thông tin MaKG và MaKH từ DB
        testMaKG = jdbcTemplate.queryForObject("SELECT MaKG FROM (SELECT MaKG FROM KHONGGIAN ORDER BY MaKG) WHERE ROWNUM = 1", String.class);
        testMaKH = jdbcTemplate.queryForObject("SELECT MaKH FROM (SELECT MaKH FROM KHACHHANG ORDER BY MaKH) WHERE ROWNUM = 1", String.class);

        if (testMaKG == null || testMaKH == null) {
            throw new RuntimeException("DB thiếu dữ liệu KHONGGIAN hoặc KHACHHANG!");
        }

        // Lưu trạng thái gốc của Không gian
        originalKhongGianStatus = jdbcTemplate.queryForObject("SELECT TrangThaiKG FROM KHONGGIAN WHERE MaKG = ?", String.class, testMaKG);

        // 3. Tạo một booking DATCHO ở trạng thái "Đang chờ thanh toán"
        String trangThaiChoThanhToan = getDbStatusValue(jdbcTemplate, "CHK_DC_TRANGTHAI", "cho thanh toan", "Đang chờ thanh toán");
        
        jdbcTemplate.update(
                """
                INSERT INTO DATCHO (MaDatCho, ThoiGianDat, ThoiGianDuKienToi, KhoangThoiGianSuDung,
                                    TrangThaiDatTruoc, ThanhTien, GhiChu, CapNhatLanCuoi, MaKH, MaKG)
                VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + NUMTODSINTERVAL(3, 'HOUR'), 2, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)
                """,
                TEST_MA_DAT_CHO,
                trangThaiChoThanhToan,
                new BigDecimal("100000"),
                "Giao dịch thử nghiệm webhook",
                testMaKH,
                testMaKG
        );

        // Chuyển trạng thái Không gian sang "Tạm khoá"
        String trangThaiTamKhoa = getDbStatusValue(jdbcTemplate, "CHK_KG_TRANGTHAI", "tam khoa", "Tạm khoá");
        jdbcTemplate.update("UPDATE KHONGGIAN SET TrangThaiKG = ? WHERE MaKG = ?", trangThaiTamKhoa, testMaKG);
        
        System.out.println("[setUp] Đã khởi tạo dữ liệu giả lập thành công.");
    }

    private static void cleanUpTestData(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("DELETE FROM HOADON WHERE MaPhien IN (SELECT MaPhien FROM PHIENLAMVIEC WHERE MaDatCho = ?)", TEST_MA_DAT_CHO);
        jdbcTemplate.update("DELETE FROM PHIENLAMVIEC WHERE MaDatCho = ?", TEST_MA_DAT_CHO);
        jdbcTemplate.update("DELETE FROM DATCHO WHERE MaDatCho = ?", TEST_MA_DAT_CHO);
        
        if (testMaKG != null && originalKhongGianStatus != null) {
            jdbcTemplate.update("UPDATE KHONGGIAN SET TrangThaiKG = ? WHERE MaKG = ?", originalKhongGianStatus, testMaKG);
        }
    }

    private static String getDbStatusValue(JdbcTemplate jdbcTemplate, String constraintName, String normalizedNeedle, String fallbackValue) {
        try {
            String condition = jdbcTemplate.queryForObject(
                    "SELECT search_condition_vc FROM user_constraints WHERE constraint_name = ?",
                    String.class,
                    constraintName
            );
            if (condition != null) {
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("'([^']*)'").matcher(condition);
                while (matcher.find()) {
                    String val = matcher.group(1);
                    String normalized = val.toLowerCase().replace('đ', 'd').replaceAll("[^a-z0-9 ]", " ").trim();
                    if (normalized.contains(normalizedNeedle)) {
                        return val;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return fallbackValue;
    }
}
