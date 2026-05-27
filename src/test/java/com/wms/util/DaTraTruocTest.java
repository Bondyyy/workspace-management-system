package com.wms.util;

import com.wms.config.AppConstants;
import com.wms.dao.TrangChuQuanLy.QuanLyPhien.PhienLamViecDAO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.KetQuaNhanChoDTO;
import com.wms.web.repository.CongThongTinWebRepository;
import com.wms.web.scheduler.LichKiemTraThanhToanDatCho;
import com.wms.web.service.CongThongTinService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class DaTraTruocTest {

    private JdbcTemplate jdbcTemplate;
    private String maKH;
    private String maKG;
    private String testPrefix;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate = createJdbcTemplate();
        testPrefix = "T" + System.currentTimeMillis() % 100000;
        compileDbObjects();
        seedBaseData();
    }

    @AfterEach
    void tearDown() {
        if (jdbcTemplate == null || maKG == null) {
            return;
        }
        try {
            jdbcTemplate.update("DELETE FROM CHITIETDICHVU WHERE MaPhien IN (SELECT MaPhien FROM PHIENLAMVIEC WHERE MaKG = ?)", maKG);
            jdbcTemplate.update("UPDATE PHIENLAMVIEC SET TrangThaiPhien = 'Đã kết thúc' WHERE MaKG = ? AND TrangThaiPhien = 'Đang hoạt động'", maKG);
            jdbcTemplate.update("UPDATE HOADON SET TrangThaiThanhToan = 'Đang chờ thanh toán' WHERE MaPhien IN (SELECT MaPhien FROM PHIENLAMVIEC WHERE MaKG = ?)", maKG);
            jdbcTemplate.update("DELETE FROM HOADON WHERE MaPhien IN (SELECT MaPhien FROM PHIENLAMVIEC WHERE MaKG = ?)", maKG);
            jdbcTemplate.update("DELETE FROM PHIENLAMVIEC WHERE MaKG = ?", maKG);
            jdbcTemplate.update("DELETE FROM DATCHO WHERE MaKG = ?", maKG);
            jdbcTemplate.update("UPDATE KHONGGIAN SET TrangThaiKG = 'Trống' WHERE MaKG = ?", maKG);
        } catch (Exception ignored) {
        }
    }

    @Test
    void case1_DatTruocMoPhienTaoMotHoaDonDaTraTruoc() {
        String maDatCho = taoDatChoDaThanhToan(-1, 2, 150000, "QR_CASE1");
        KetQuaNhanChoDTO ketQua = new PhienLamViecDAO().moPhienTuQrDatCho(maDatCho, "QR_CASE1");
        assertTrue(ketQua.isThanhCong(), ketQua.getThongBao());

        List<Map<String, Object>> hoaDonList = jdbcTemplate.queryForList(
                """
                SELECT h.*, dc.ThanhTien AS TienDatCho
                FROM HOADON h
                JOIN PHIENLAMVIEC p ON p.MaPhien = h.MaPhien
                JOIN DATCHO dc ON dc.MaDatCho = p.MaDatCho
                WHERE p.MaDatCho = ?
                """,
                maDatCho);
        assertEquals(1, hoaDonList.size(), "Chỉ được có một hóa đơn cho phiên");
        Map<String, Object> hd = hoaDonList.get(0);
        assertEquals(150000.0, ((Number) hd.get("TienDatCho")).doubleValue(), 0.001);
        assertEquals("Đã trả trước", hd.get("TrangThaiThanhToan"));
    }

    @Test
    void case4_KhachKhongDenQuaGio() {
        String maDatCho = taoDatChoDaThanhToan(-3, 2, 120000, "QR_CASE4");
        CongThongTinService service = new CongThongTinService(new CongThongTinWebRepository(jdbcTemplate));
        new LichKiemTraThanhToanDatCho(service).kiemTraDatCho();

        Map<String, Object> dc = jdbcTemplate.queryForMap("SELECT * FROM DATCHO WHERE MaDatCho = ?", maDatCho);
        assertEquals(AppConstants.TRANG_THAI_DAT_CHO_QUA_HAN, dc.get("TrangThaiDatTruoc"));
        assertNull(dc.get("MaQR"));
    }

    @Test
    void case6_QuetQRDatChoQuaHan() {
        String maDatCho = taoDatChoDaThanhToan(-3, 2, 100000, "QR_CASE6");
        KetQuaNhanChoDTO ketQua = new PhienLamViecDAO().moPhienTuQrDatCho(maDatCho, "QR_CASE6");
        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("quá giờ nhận chỗ") || ketQua.getThongBao().contains("Quá hạn nhận chỗ"));
    }

    @Test
    void testCase1_PrepaidAutoEndNoExtraServices() {
        String maDatCho = taoDatChoDaThanhToan(-1, 2, 150000, "QR_CASE1_AUTO");
        KetQuaNhanChoDTO ketQua = new PhienLamViecDAO().moPhienTuQrDatCho(maDatCho, "QR_CASE1_AUTO");
        assertTrue(ketQua.isThanhCong(), ketQua.getThongBao());

        String maPhien = jdbcTemplate.queryForObject(
                "SELECT MaPhien FROM PHIENLAMVIEC WHERE MaDatCho = ?", String.class, maDatCho);

        jdbcTemplate.update(
                """
                UPDATE PHIENLAMVIEC
                SET ThoiGianBatDau = CURRENT_TIMESTAMP - NUMTODSINTERVAL(2, 'HOUR'),
                    ThoiGianDuKienKetThuc = CURRENT_TIMESTAMP - NUMTODSINTERVAL(10, 'MINUTE')
                WHERE MaPhien = ?
                """,
                maPhien);

        CongThongTinService service = new CongThongTinService(new CongThongTinWebRepository(jdbcTemplate));
        service.tuDongKetThucPhienQuaHanDatCho();

        Map<String, Object> phien = jdbcTemplate.queryForMap(
                "SELECT * FROM PHIENLAMVIEC WHERE MaPhien = ?", maPhien);
        assertEquals("Đã kết thúc", phien.get("TrangThaiPhien"));
        assertNotNull(phien.get("ThoiGianKetThuc"));

        Map<String, Object> hd = jdbcTemplate.queryForMap(
                "SELECT * FROM HOADON WHERE MaPhien = ?", maPhien);
        assertEquals("Đã thanh toán thành công", hd.get("TrangThaiThanhToan"));
        assertEquals(0.0, ((Number) hd.get("ThanhTien")).doubleValue(), 0.001);

        String trangThaiKG = jdbcTemplate.queryForObject(
                "SELECT TrangThaiKG FROM KHONGGIAN WHERE MaKG = ?", String.class, maKG);
        assertEquals("Trống", trangThaiKG);
    }

    @Test
    void testCase2_PrepaidAutoEndWithExtraServices() {
        String maDatCho = taoDatChoDaThanhToan(-1, 2, 150000, "QR_CASE2_AUTO");
        KetQuaNhanChoDTO ketQua = new PhienLamViecDAO().moPhienTuQrDatCho(maDatCho, "QR_CASE2_AUTO");
        assertTrue(ketQua.isThanhCong(), ketQua.getThongBao());

        String maPhien = jdbcTemplate.queryForObject(
                "SELECT MaPhien FROM PHIENLAMVIEC WHERE MaDatCho = ?", String.class, maDatCho);

        List<Map<String, Object>> dsDichVu = jdbcTemplate.queryForList("SELECT * FROM DICHVU");
        String maDV;
        double donGiaDV = 50000.0;
        if (dsDichVu.isEmpty()) {
            maDV = "DV_TEST_" + System.currentTimeMillis() % 10000;
            List<Map<String, Object>> dsLoaiDV = jdbcTemplate.queryForList("SELECT * FROM LOAIDICHVU");
            String maLoaiDV = dsLoaiDV.isEmpty() ? "LDP_TEST" : (String) dsLoaiDV.get(0).get("MaLoaiDV");
            if (dsLoaiDV.isEmpty()) {
                jdbcTemplate.update("INSERT INTO LOAIDICHVU (MaLoaiDV, TenLoaiDV, TrangThaiLDV) VALUES (?, ?, ?)",
                        maLoaiDV, "Loại test", "Hoạt động");
            }
            jdbcTemplate.update("INSERT INTO DICHVU (MaDV, TenDV, TrangThaiDV, DonGia, MaLoaiDV, SoLuong, GiaNhap) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    maDV, "Dịch vụ test", "Hoạt động", donGiaDV, maLoaiDV, 100, 20000);
        } else {
            Map<String, Object> dv = dsDichVu.get(0);
            maDV = (String) dv.get("MaDV");
            donGiaDV = ((Number) dv.get("DonGia")).doubleValue();
        }

        jdbcTemplate.update("INSERT INTO CHITIETDICHVU (MaDV, MaPhien, SoLuong, GhiChu) VALUES (?, ?, ?, ?)",
                maDV, maPhien, 1, "Khách dùng thêm");

        jdbcTemplate.update(
                """
                UPDATE PHIENLAMVIEC
                SET ThoiGianBatDau = CURRENT_TIMESTAMP - NUMTODSINTERVAL(2, 'HOUR'),
                    ThoiGianDuKienKetThuc = CURRENT_TIMESTAMP - NUMTODSINTERVAL(10, 'MINUTE')
                WHERE MaPhien = ?
                """,
                maPhien);

        CongThongTinService service = new CongThongTinService(new CongThongTinWebRepository(jdbcTemplate));
        service.tuDongKetThucPhienQuaHanDatCho();

        Map<String, Object> phien = jdbcTemplate.queryForMap(
                "SELECT * FROM PHIENLAMVIEC WHERE MaPhien = ?", maPhien);
        assertEquals("Đã kết thúc", phien.get("TrangThaiPhien"));

        Map<String, Object> hd = jdbcTemplate.queryForMap(
                "SELECT * FROM HOADON WHERE MaPhien = ?", maPhien);
        double thanhTienConLai = ((Number) hd.get("ThanhTien")).doubleValue();
        assertEquals(thanhTienConLai > 0 ? "Đang chờ thanh toán" : "Đã thanh toán thành công",
                hd.get("TrangThaiThanhToan"));
        assertEquals(150000.0, jdbcTemplate.queryForObject(
                "SELECT ThanhTien FROM DATCHO WHERE MaDatCho = ?", Number.class, maDatCho).doubleValue(), 0.001);
    }

    @Test
    void testCase4_WalkInSessionShouldNotBeEndedByPrebookedScheduler() {
        String maPhien = "P_WALK_" + System.currentTimeMillis() % 10000;
        jdbcTemplate.update("""
                INSERT INTO PHIENLAMVIEC (MaPhien, ThoiGianBatDau, ThoiGianDuKienKetThuc, TrangThaiPhien, MaKG, MaKH, MaDatCho)
                VALUES (?, CURRENT_TIMESTAMP - NUMTODSINTERVAL(2, 'HOUR'),
                        CURRENT_TIMESTAMP - NUMTODSINTERVAL(10, 'MINUTE'),
                        'Đang hoạt động', ?, ?, NULL)
                """,
                maPhien, maKG, maKH);

        List<Map<String, Object>> dsHd = jdbcTemplate.queryForList("SELECT * FROM HOADON WHERE MaPhien = ?", maPhien);
        String maHD = "HD_WALK_" + System.currentTimeMillis() % 10000;
        if (dsHd.isEmpty()) {
            jdbcTemplate.update("""
                    INSERT INTO HOADON (MaHoaDon, SoHD, TongTien, ThanhTien, NgayLapHoaDon, PhuongThucThanhToan, TrangThaiThanhToan, MaPhien)
                    VALUES (?, ?, 0, 0, CURRENT_TIMESTAMP, 'Tiền mặt', 'Đang chờ thanh toán', ?)
                    """,
                    maHD, maHD, maPhien);
        }

        CongThongTinService service = new CongThongTinService(new CongThongTinWebRepository(jdbcTemplate));
        service.tuDongKetThucPhienQuaHanDatCho();

        String trangThaiPhien = jdbcTemplate.queryForObject(
                "SELECT TrangThaiPhien FROM PHIENLAMVIEC WHERE MaPhien = ?", String.class, maPhien);
        assertEquals("Đang hoạt động", trangThaiPhien);

        jdbcTemplate.update("UPDATE PHIENLAMVIEC SET TrangThaiPhien = 'Đã kết thúc' WHERE MaPhien = ?", maPhien);
        jdbcTemplate.update("DELETE FROM HOADON WHERE MaPhien = ?", maPhien);
        jdbcTemplate.update("DELETE FROM PHIENLAMVIEC WHERE MaPhien = ?", maPhien);
    }

    private void seedBaseData() {
        maKH = jdbcTemplate.queryForObject(
                "SELECT MaKH FROM (SELECT MaKH FROM KHACHHANG ORDER BY MaKH) WHERE ROWNUM = 1", String.class);
        maKG = jdbcTemplate.queryForObject(
                "SELECT MaKG FROM (SELECT MaKG FROM KHONGGIAN ORDER BY MaKG) WHERE ROWNUM = 1", String.class);
        jdbcTemplate.update("UPDATE KHONGGIAN SET TrangThaiKG = 'Trống' WHERE MaKG = ?", maKG);
    }

    private String taoDatChoDaThanhToan(int hoursOffset, int duration, double tien, String qr) {
        jdbcTemplate.update(
                """
                INSERT INTO DATCHO (ThoiGianDat, ThoiGianDuKienToi, KhoangThoiGianSuDung, TrangThaiDatTruoc,
                                    ThanhTien, GhiChu, CapNhatLanCuoi, MaKH, MaKG, MaQR)
                VALUES (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + NUMTODSINTERVAL(1, 'HOUR'), ?, 'Đã thanh toán thành công',
                        ?, ?, CURRENT_TIMESTAMP, ?, ?, ?)
                """,
                duration, tien, "Test " + testPrefix, maKH, maKG, qr);
        String maDatCho = jdbcTemplate.queryForObject(
                "SELECT MaDatCho FROM DATCHO WHERE MaQR = ?",
                String.class, qr);
        jdbcTemplate.update(
                """
                UPDATE DATCHO
                SET ThoiGianDat = CURRENT_TIMESTAMP + NUMTODSINTERVAL(?, 'HOUR'),
                    ThoiGianDuKienToi = CURRENT_TIMESTAMP + NUMTODSINTERVAL(?, 'HOUR')
                WHERE MaDatCho = ?
                """,
                hoursOffset - 1, hoursOffset, maDatCho);
        return jdbcTemplate.queryForObject(
                "SELECT MaDatCho FROM DATCHO WHERE MaDatCho = ?",
                String.class, maDatCho);
    }

    private void compileDbObjects() throws Exception {
        ensureVoucherRelationTable();
        executeSqlFile("Database/03_function/FN_TinhTienKhongGian.sql");
        executeSqlFile("Database/03_function/FN_TinhTienDichVu.sql");
        executeSqlFile("Database/03_function/FN_TinhTongTien.sql");
        executeSqlFile("Database/03_function/FN_TinhThanhTien.sql");
        executeSqlFile("Database/05_triggers/ThanhToanTrucTiep/TRG_TaoMaHoaDon.sql");
        executeSqlFile("Database/05_triggers/ThanhToanTrucTiep/TRG_TaoHoaDonKhiMoPhien.sql");
        executeSqlFile("Database/05_triggers/ThanhToanTrucTiep/TRG_TinhToanHoaDon.sql");
        executeSqlFile("Database/05_triggers/ThanhToanTrucTiep/TRG_KiemTraPhuongThucThanhToan.sql");
        executeSqlFile("Database/05_triggers/DatCho/TRG_VoHieuQRSauNhanCho.sql");
        executeSqlFile("Database/04_procedures/QuanLyPhien/SP_KetThucPhien.sql");
        executeSqlFile("Database/04_procedures/QuanLyPGG/sp_ThanhToanVoiPGG.sql");
    }

    private void ensureVoucherRelationTable() {
        jdbcTemplate.execute("""
                BEGIN
                    EXECUTE IMMEDIATE '
                        CREATE TABLE CHITIETAPDUNGPGG (
                            MaApDung VARCHAR2(50) PRIMARY KEY,
                            MaPGG VARCHAR2(50),
                            MaDatCho VARCHAR2(50),
                            MaHoaDon VARCHAR2(50),
                            NguonApDung VARCHAR2(50),
                            SoTienGiam NUMBER(18, 2) DEFAULT 0,
                            ThoiGianApDung TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )';
                EXCEPTION
                    WHEN OTHERS THEN
                        IF SQLCODE != -955 THEN
                            RAISE;
                        END IF;
                END;
                """);
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
}
