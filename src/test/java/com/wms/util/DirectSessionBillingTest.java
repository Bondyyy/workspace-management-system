package com.wms.util;

import com.wms.dao.TrangChuQuanLy.QuanLyHoaDon.HoaDonDAO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.InvoiceLine;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DirectSessionBillingTest {

    private JdbcTemplate jdbcTemplate;
    private String testPrefix;
    private String maKH;
    private String maKG;
    private double donGiaTheoGio;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate = createJdbcTemplate();
        testPrefix = "B" + Long.toString(System.nanoTime(), 36).toUpperCase();
        compileBillingFunctions();
        seedBaseData();
    }

    @AfterEach
    void tearDown() {
        if (jdbcTemplate == null || testPrefix == null) {
            return;
        }
        try {
            jdbcTemplate.update("""
                    DELETE FROM CHITIETDICHVU
                    WHERE MaPhien IN (SELECT MaPhien FROM PHIENLAMVIEC WHERE MaPhien LIKE ?)
                    """, "P" + testPrefix + "%");
            jdbcTemplate.update("DELETE FROM CHITIETAPDUNGPGG WHERE MaHoaDon LIKE ?", "HD" + testPrefix + "%");
            jdbcTemplate.update("DELETE FROM HOADON WHERE MaHoaDon LIKE ?", "HD" + testPrefix + "%");
            jdbcTemplate.update("DELETE FROM PHIENLAMVIEC WHERE MaPhien LIKE ?", "P" + testPrefix + "%");
            jdbcTemplate.update("DELETE FROM DICHVU WHERE MaDV LIKE ?", "DV" + testPrefix + "%");
            jdbcTemplate.update("DELETE FROM LOAIDICHVU WHERE MaLoaiDV LIKE ?", "LDV" + testPrefix + "%");
        } catch (Exception ignored) {
        }
    }

    @ParameterizedTest(name = "{0} minutes bills {1} hour(s)")
    @CsvSource({
            "10, 0",
            "15, 0",
            "16, 1",
            "75, 1",
            "76, 2"
    })
    void directSessionUsesDatabaseSpaceBillingFormula(int minutes, int expectedHours) {
        TestInvoice invoice = createDirectInvoice(minutes, 0, 0);
        double expectedSpaceFee = expectedHours * donGiaTheoGio;

        assertMoney(expectedSpaceFee, money("SELECT FN_TinhTienKhongGian(?) FROM DUAL", invoice.maPhien));
        assertMoney(expectedSpaceFee, money("SELECT FN_TinhTongTien(?) FROM DUAL", invoice.maPhien));
        assertMoney(expectedSpaceFee, money("SELECT TongTien FROM HOADON WHERE MaHoaDon = ?", invoice.maHoaDon));

        ThongTinHoaDonDTO detail = new HoaDonDAO().layThongTinChiTietHoaDon(invoice.maHoaDon);
        assertNotNull(detail);
        assertEquals(expectedHours, detail.getTongSoGio(), 0.001);
        assertMoney(expectedSpaceFee, detail.getTongTien());

        InvoiceLine spaceLine = spaceLine(detail);
        assertEquals(expectedHours, spaceLine.getSoLuong());
        assertMoney(expectedSpaceFee, spaceLine.getThanhTien());
    }

    @Test
    void directSessionUnderFifteenMinutesWithServicesOnlyChargesServices() {
        int serviceQuantity = 2;
        double serviceUnitPrice = 25_000;
        TestInvoice invoice = createDirectInvoice(10, serviceQuantity, serviceUnitPrice);
        double expectedServiceFee = serviceQuantity * serviceUnitPrice;

        assertMoney(0, money("SELECT FN_TinhTienKhongGian(?) FROM DUAL", invoice.maPhien));
        assertMoney(expectedServiceFee, money("SELECT FN_TinhTongTien(?) FROM DUAL", invoice.maPhien));
        assertMoney(expectedServiceFee, money("SELECT TongTien FROM HOADON WHERE MaHoaDon = ?", invoice.maHoaDon));

        ThongTinHoaDonDTO detail = new HoaDonDAO().layThongTinChiTietHoaDon(invoice.maHoaDon);
        assertNotNull(detail);
        assertEquals(0, detail.getTongSoGio(), 0.001);
        assertMoney(expectedServiceFee, detail.getTongTien());

        InvoiceLine spaceLine = spaceLine(detail);
        assertEquals(0, spaceLine.getSoLuong());
        assertMoney(0, spaceLine.getThanhTien());

        InvoiceLine serviceLine = detail.getDongChiPhi().stream()
                .filter(line -> line.getNoiDung() != null && line.getNoiDung().startsWith("Service test"))
                .findFirst()
                .orElseThrow();
        assertEquals(serviceQuantity, serviceLine.getSoLuong());
        assertMoney(expectedServiceFee, serviceLine.getThanhTien());
    }

    private TestInvoice createDirectInvoice(int minutes, int serviceQuantity, double serviceUnitPrice) {
        String suffix = minutes + "_" + serviceQuantity;
        String maPhien = "P" + testPrefix + "_" + suffix;
        String maHoaDon = "HD" + testPrefix + "_" + suffix;
        LocalDateTime start = LocalDateTime.of(2026, 5, 28, 9, 0);

        jdbcTemplate.update("""
                INSERT INTO PHIENLAMVIEC (
                    MaPhien, ThoiGianBatDau, ThoiGianDuKienKetThuc,
                    TrangThaiPhien, ThoiGianKetThuc, CapNhatLanCuoi,
                    MaKG, MaKH, MaDatCho
                ) VALUES (?, ?, ?, 'Đã kết thúc', ?, CURRENT_TIMESTAMP, ?, ?, NULL)
                """,
                maPhien,
                Timestamp.valueOf(start),
                Timestamp.valueOf(start.plusHours(3)),
                Timestamp.valueOf(start.plusMinutes(minutes)),
                maKG,
                maKH);

        if (serviceQuantity > 0) {
            String maLoaiDV = "LDV" + testPrefix;
            String maDV = "DV" + testPrefix;
            jdbcTemplate.update(
                    "INSERT INTO LOAIDICHVU (MaLoaiDV, TenLoaiDV, TrangThaiLDV) VALUES (?, ?, 'Đang hoạt động')",
                    maLoaiDV, "Loai dich vu test " + testPrefix);
            jdbcTemplate.update("""
                    INSERT INTO DICHVU (MaDV, TenDV, TrangThaiDV, DonGia, MaLoaiDV, SoLuong, GiaNhap)
                    VALUES (?, ?, 'Hoạt động', ?, ?, 100, 0)
                    """,
                    maDV, "Service test " + testPrefix, serviceUnitPrice, maLoaiDV);
            jdbcTemplate.update("UPDATE PHIENLAMVIEC SET TrangThaiPhien = 'Đang hoạt động' WHERE MaPhien = ?",
                    maPhien);
            jdbcTemplate.update("INSERT INTO CHITIETDICHVU (MaDV, MaPhien, SoLuong, GhiChu) VALUES (?, ?, ?, ?)",
                    maDV, maPhien, serviceQuantity, "Direct session billing test");
            jdbcTemplate.update("UPDATE PHIENLAMVIEC SET TrangThaiPhien = 'Đã kết thúc' WHERE MaPhien = ?",
                    maPhien);
        }

        double tongTien = money("SELECT FN_TinhTongTien(?) FROM DUAL", maPhien);
        insertHoaDonTest(maHoaDon, maPhien, tongTien);
        return new TestInvoice(maPhien, maHoaDon);
    }

    private void insertHoaDonTest(String maHoaDon, String maPhien, double tongTien) {
        boolean restoreTrigger = isTriggerEnabled("TRG_TaoMaHoaDon");
        if (restoreTrigger) {
            setTriggerEnabled("TRG_TaoMaHoaDon", false);
        }
        try {
            jdbcTemplate.update("""
                    INSERT INTO HOADON (
                        MaHoaDon, SoHD, TongTien, ThanhTien,
                        NgayLapHoaDon, TrangThaiThanhToan, MaPhien
                    ) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)
                    """,
                    maHoaDon, maHoaDon, tongTien, tongTien, "Đang chờ thanh toán", maPhien);
        } finally {
            if (restoreTrigger) {
                setTriggerEnabled("TRG_TaoMaHoaDon", true);
            }
        }
    }

    private void seedBaseData() {
        maKH = jdbcTemplate.queryForObject(
                "SELECT MaKH FROM (SELECT MaKH FROM KHACHHANG ORDER BY MaKH) WHERE ROWNUM = 1",
                String.class);
        var row = jdbcTemplate.queryForMap("""
                SELECT kg.MaKG, lkg.DonGiaTheoGio
                FROM KHONGGIAN kg
                JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG
                WHERE kg.TenKG IS NOT NULL
                  AND NVL(lkg.DonGiaTheoGio, 0) > 0
                ORDER BY kg.MaKG
                FETCH FIRST 1 ROWS ONLY
                """);
        maKG = (String) row.get("MaKG");
        donGiaTheoGio = ((Number) row.get("DonGiaTheoGio")).doubleValue();
    }

    private InvoiceLine spaceLine(ThongTinHoaDonDTO detail) {
        return detail.getDongChiPhi().stream()
                .filter(line -> line.getNoiDung() != null && line.getNoiDung().startsWith("Thuê"))
                .findFirst()
                .orElseThrow();
    }

    private double money(String sql, Object... args) {
        Number number = jdbcTemplate.queryForObject(sql, Number.class, args);
        return number == null ? 0 : number.doubleValue();
    }

    private void assertMoney(double expected, double actual) {
        assertEquals(expected, actual, 0.001);
    }

    private boolean isTriggerEnabled(String triggerName) {
        List<String> statuses = jdbcTemplate.queryForList(
                "SELECT STATUS FROM USER_TRIGGERS WHERE TRIGGER_NAME = ?",
                String.class,
                triggerName.toUpperCase(Locale.ROOT));
        return !statuses.isEmpty() && "ENABLED".equalsIgnoreCase(statuses.get(0));
    }

    private void setTriggerEnabled(String triggerName, boolean enabled) {
        jdbcTemplate.execute("ALTER TRIGGER " + triggerName + (enabled ? " ENABLE" : " DISABLE"));
    }

    private void compileBillingFunctions() throws Exception {
        executeSqlFile("Database/03_function/FN_TinhTienKhongGian.sql");
        executeSqlFile("Database/03_function/FN_TinhTienDichVu.sql");
        executeSqlFile("Database/03_function/FN_TinhTongTien.sql");
    }

    private void executeSqlFile(String path) throws Exception {
        String sql = Files.readString(Path.of(path)).trim();
        if (sql.endsWith("/")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        jdbcTemplate.execute(sql);
    }

    private JdbcTemplate createJdbcTemplate() throws Exception {
        Properties p = new Properties();
        try (FileInputStream fs = new FileInputStream("db.properties")) {
            p.load(fs);
        }
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setUrl(p.getProperty("url"));
        ds.setUsername(p.getProperty("user"));
        ds.setPassword(p.getProperty("pass"));
        JdbcTemplate jt = new JdbcTemplate(ds);
        jt.execute("ALTER SESSION SET TIME_ZONE = '+07:00'");
        return jt;
    }

    private record TestInvoice(String maPhien, String maHoaDon) {
    }
}
