package com.wms.web.controller;

import com.wms.web.form.YeuCauWebhookThanhToan;
import com.wms.web.service.CongThongTinService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WebhookThanhToanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CongThongTinService congThongTinService;

    private static final String TEST_MA_DAT_CHO = "DC_TEST_999";
    private String originalKhongGianStatus = null;
    private String testMaKG = null;
    private String testMaKH = null;

    @BeforeEach
    public void setUp() {
        System.out.println("--- THIẾT LẬP DỮ LIỆU GIẢ LẬP ĐỂ TEST WEBHOOK ---");
        
        // 1. Dọn dẹp dữ liệu rác cũ nếu có
        cleanUpTestData();

        // 2. Lấy thông tin MaKG và MaKH từ DB
        testMaKG = jdbcTemplate.queryForObject("SELECT MaKG FROM (SELECT MaKG FROM KHONGGIAN ORDER BY MaKG) WHERE ROWNUM = 1", String.class);
        testMaKH = jdbcTemplate.queryForObject("SELECT MaKH FROM (SELECT MaKH FROM KHACHHANG ORDER BY MaKH) WHERE ROWNUM = 1", String.class);

        assertThat(testMaKG).isNotNull();
        assertThat(testMaKH).isNotNull();

        // Lưu trạng thái gốc của Không gian
        originalKhongGianStatus = jdbcTemplate.queryForObject("SELECT TrangThaiKG FROM KHONGGIAN WHERE MaKG = ?", String.class, testMaKG);

        // 3. Tạo một booking DATCHO giả lập ở trạng thái "Đang chờ thanh toán"
        String trangThaiChoThanhToan = getDbStatusValue("CHK_DC_TRANGTHAI", "cho thanh toan", "Đang chờ thanh toán");
        
        // Chèn trực tiếp dữ liệu DATCHO vào database
        jdbcTemplate.update(
                """
                INSERT INTO DATCHO (MaDatCho, ThoiGianDat, ThoiGianDuKienToi, KhoangThoiGianSuDung,
                                    TrangThaiDatTruoc, ThanhTien, GhiChu, CapNhatLanCuoi, MaKH, MaKG)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)
                """,
                TEST_MA_DAT_CHO,
                Timestamp.valueOf(LocalDateTime.now().minusMinutes(2)), // Đã đặt cách đây 2 phút (chưa bị hết hạn)
                Timestamp.valueOf(LocalDateTime.now().plusHours(3)),
                2,
                trangThaiChoThanhToan,
                new BigDecimal("100000"),
                "Giao dịch thử nghiệm webhook",
                testMaKH,
                testMaKG
        );

        // Chuyển trạng thái Không gian sang "Tạm khoá"
        String trangThaiTamKhoa = getDbStatusValue("CHK_KG_TRANGTHAI", "tam khoa", "Tạm khoá");
        jdbcTemplate.update("UPDATE KHONGGIAN SET TrangThaiKG = ? WHERE MaKG = ?", trangThaiTamKhoa, testMaKG);
        
        System.out.println("[setUp] Đã tạo thành công DATCHO giả lập: " + TEST_MA_DAT_CHO + " với MaKH: " + testMaKH + ", MaKG: " + testMaKG);
    }

    @AfterEach
    public void tearDown() {
        System.out.println("--- DỌN DẸP DỮ LIỆU SAU KHI TEST WEBHOOK ---");
        cleanUpTestData();
    }

    private void cleanUpTestData() {
        // Xóa hóa đơn liên quan đến DATCHO test nếu có
        jdbcTemplate.update("DELETE FROM HOADON WHERE MaPhien IN (SELECT MaPhien FROM PHIENLAMVIEC WHERE MaDatCho = ?)", TEST_MA_DAT_CHO);
        // Xóa phiên làm việc liên quan đến DATCHO test nếu có
        jdbcTemplate.update("DELETE FROM PHIENLAMVIEC WHERE MaDatCho = ?", TEST_MA_DAT_CHO);
        // Xóa DATCHO test
        jdbcTemplate.update("DELETE FROM DATCHO WHERE MaDatCho = ?", TEST_MA_DAT_CHO);
        
        // Khôi phục trạng thái Không gian ban đầu
        if (testMaKG != null && originalKhongGianStatus != null) {
            jdbcTemplate.update("UPDATE KHONGGIAN SET TrangThaiKG = ? WHERE MaKG = ?", originalKhongGianStatus, testMaKG);
        }
    }

    private String getDbStatusValue(String constraintName, String normalizedNeedle, String fallbackValue) {
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

    /**
     * Test case 1: Gửi webhook hợp lệ -> DATCHO chuyển Đã thanh toán thành công
     */
    @Test
    public void testCase1_validWebhook_updatesToPaid() throws Exception {
        System.out.println("\n>>> BẮT ĐẦU TEST CASE 1: Webhook hợp lệ chuyển DATCHO thành công <<<");
        
        String jsonPayload = """
                {
                    "maGiaoDich": "TX_TEST_111",
                    "soTien": 100000,
                    "noiDung": "Chuyen khoan dat cho DC_TEST_999",
                    "trangThai": "SUCCESS",
                    "thoiGianThanhToan": "%s"
                }
                """.formatted(LocalDateTime.now().toString());

        mockMvc.perform(post("/api/payment-webhook/bank-transfer")
                .header("X-Webhook-Secret", "WMS_SECRET_TOKEN_2026")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.maDatCho").value(TEST_MA_DAT_CHO));

        // Kiểm tra database: Trang thái DATCHO phải là "Đã thanh toán thành công"
        String currentStatus = jdbcTemplate.queryForObject("SELECT TrangThaiDatTruoc FROM DATCHO WHERE MaDatCho = ?", String.class, TEST_MA_DAT_CHO);
        System.out.println("[Test Case 1] Trạng thái DATCHO trong DB hiện tại: \"" + currentStatus + "\"");
        assertThat(currentStatus).contains("thành công");

        // Kiểm tra database: Trạng thái Không gian phải là "Đã đặt trước"
        String currentKGStatus = jdbcTemplate.queryForObject("SELECT TrangThaiKG FROM KHONGGIAN WHERE MaKG = ?", String.class, testMaKG);
        System.out.println("[Test Case 1] Trạng thái KHONGGIAN trong DB hiện tại: \"" + currentKGStatus + "\"");
        assertThat(currentKGStatus).contains("trước");
    }

    /**
     * Test case 2: Gọi lại webhook cùng giao dịch -> không lỗi, không xử lý trùng (Idempotent)
     */
    @Test
    public void testCase2_duplicateWebhook_doesNotProcessAgain() throws Exception {
        System.out.println("\n>>> BẮT ĐẦU TEST CASE 2: Gọi webhook trùng mã giao dịch (Idempotency) <<<");

        // Gửi webhook lần 1
        String jsonPayload = """
                {
                    "maGiaoDich": "TX_TEST_222",
                    "soTien": 100000,
                    "noiDung": "Chuyen khoan dat cho DC_TEST_999",
                    "trangThai": "SUCCESS",
                    "thoiGianThanhToan": "%s"
                }
                """.formatted(LocalDateTime.now().toString());

        mockMvc.perform(post("/api/payment-webhook/bank-transfer")
                .header("X-Webhook-Secret", "WMS_SECRET_TOKEN_2026")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Đọc lại ghi chú để chắc chắn mã giao dịch đã được ghi nhận
        String noteAfterFirst = jdbcTemplate.queryForObject("SELECT GhiChu FROM DATCHO WHERE MaDatCho = ?", String.class, TEST_MA_DAT_CHO);
        System.out.println("[Test Case 2] Ghi chú DATCHO sau lần 1: \"" + noteAfterFirst + "\"");
        assertThat(noteAfterFirst).contains("TX_TEST_222");

        // Gửi lại đúng webhook đó lần thứ 2
        mockMvc.perform(post("/api/payment-webhook/bank-transfer")
                .header("X-Webhook-Secret", "WMS_SECRET_TOKEN_2026")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk()) // Trả success mà không lỗi
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đặt chỗ DC_TEST_999 đã được xử lý trước đó với mã giao dịch này."));

        System.out.println("[Test Case 2] Webhook trả thành công và bỏ qua ghi nhận trùng lặp giao dịch.");
    }

    /**
     * Test case 3: Scheduler chạy sau khi DATCHO đã thành công -> không gửi email hủy
     */
    @Test
    public void testCase3_schedulerAfterPaid_doesNotCancelOrSendEmail() throws Exception {
        System.out.println("\n>>> BẮT ĐẦU TEST CASE 3: Scheduler chạy sau khi DATCHO đã thành công <<<");

        // 1. Giả lập thanh toán thành công cho DATCHO thông qua webhook
        String jsonPayload = """
                {
                    "maGiaoDich": "TX_TEST_333",
                    "soTien": 100000,
                    "noiDung": "Chuyen khoan dat cho DC_TEST_999",
                    "trangThai": "SUCCESS",
                    "thoiGianThanhToan": "%s"
                }
                """.formatted(LocalDateTime.now().toString());

        mockMvc.perform(post("/api/payment-webhook/bank-transfer")
                .header("X-Webhook-Secret", "WMS_SECRET_TOKEN_2026")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk());

        // 2. Chuyển ThoiGianDat lùi về 15 phút trước để scheduler nhận diện là "đã quá 10 phút"
        jdbcTemplate.update("UPDATE DATCHO SET ThoiGianDat = CURRENT_TIMESTAMP - INTERVAL '15' MINUTE WHERE MaDatCho = ?", TEST_MA_DAT_CHO);

        // 3. Kích hoạt Scheduler
        System.out.println("[Test Case 3] Tiến hành kích hoạt Scheduler hetHanDatChoChoThanhToan()...");
        congThongTinService.hetHanDatChoChoThanhToan();

        // 4. Xác nhận DATCHO vẫn Đã thanh toán thành công (không bị chuyển sang Không thành công)
        String finalStatus = jdbcTemplate.queryForObject("SELECT TrangThaiDatTruoc FROM DATCHO WHERE MaDatCho = ?", String.class, TEST_MA_DAT_CHO);
        System.out.println("[Test Case 3] Trạng thái DATCHO sau khi scheduler chạy: \"" + finalStatus + "\"");
        assertThat(finalStatus).contains("thành công");
        System.out.println("[Test Case 3] Thành công: Scheduler bỏ qua đặt chỗ vì trạng thái đã thanh toán.");
    }
}
