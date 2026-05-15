package com.wms.web.repository;

import com.wms.web.model.BookingView;
import com.wms.web.model.BranchView;
import com.wms.web.model.MemberSessionView;
import com.wms.web.model.ServiceOptionView;
import com.wms.web.model.SessionServiceView;
import com.wms.web.model.SessionUser;
import com.wms.web.model.SpaceView;
import com.wms.web.model.VoucherView;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class WebPortalRepository {

    private final JdbcTemplate jdbcTemplate;

    public WebPortalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AuthRecord findAuthRecord(String identifier) {
        String sql = """
                SELECT n.MaND, n.HoTen, n.TenTaiKhoan, n.MatKhauMaHoa, n.TrangThaiND,
                       kh.MaKH, nv.MaNV
                FROM NGUOIDUNG n
                LEFT JOIN KHACHHANG kh ON kh.MaND = n.MaND
                LEFT JOIN NHANVIEN nv ON nv.MaND = n.MaND
                WHERE n.TenTaiKhoan = ? OR n.Email = ? OR n.SDT = ?
                """;
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new AuthRecord(
                    rs.getString("MaND"),
                    rs.getString("HoTen"),
                    rs.getString("TenTaiKhoan"),
                    rs.getString("MatKhauMaHoa"),
                    rs.getString("TrangThaiND"),
                    rs.getString("MaKH"),
                    rs.getString("MaNV")
            ), identifier, identifier, identifier);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public boolean usernameExists(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM NGUOIDUNG WHERE TenTaiKhoan = ?",
                Integer.class,
                username
        );
        return count != null && count > 0;
    }

    public boolean emailExists(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM NGUOIDUNG WHERE Email = ?",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }

    public void createMember(String fullName, String username, String email, String hashedPassword) {
        String maND = UUID.randomUUID().toString();
        String maKH = "KH_" + maND;

        jdbcTemplate.update(
                """
                INSERT INTO NGUOIDUNG
                    (MaND, HoTen, TenTaiKhoan, MatKhauMaHoa, Email, TrangThaiND, ThoiGianTao, CapNhatLanCuoi)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                maND,
                fullName,
                username,
                hashedPassword,
                email,
                dbValue("CHK_ND_TRANGTHAI", "dang hoat dong", 0, "Đang hoạt động")
        );

        jdbcTemplate.update(
                "INSERT INTO KHACHHANG (MaKH, MaHangThanhVien, TongChiTieu, CapNhatLanCuoi, MaND) VALUES (?, 'HTV01', 0, CURRENT_TIMESTAMP, ?)",
                maKH,
                maND
        );
    }

    public void updateLastLogin(String maND) {
        jdbcTemplate.update(
                "UPDATE NGUOIDUNG SET LanCuoiDangNhap = CURRENT_TIMESTAMP WHERE MaND = ?",
                maND
        );
    }

    public List<BranchView> findActiveBranches() {
        String sql = """
                SELECT MaCN, TenCN, DiaChi, ThoiGianMoCua, ThoiGianDongCua, DuongDayNong
                FROM CHINHANH
                ORDER BY TenCN
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new BranchView(
                rs.getString("MaCN"),
                rs.getString("TenCN"),
                rs.getString("DiaChi"),
                rs.getString("ThoiGianMoCua"),
                rs.getString("ThoiGianDongCua"),
                rs.getString("DuongDayNong")
        ));
    }

    public List<SpaceView> findSpaces(String branchId) {
        String baseSql = """
                SELECT kg.MaKG, kg.TenKG, kg.ViTri, kg.TrangThaiKG,
                       cn.TenCN, cn.ThoiGianMoCua, cn.ThoiGianDongCua,
                       lkg.TenLoaiKG, NVL(lkg.DonGiaTheoGio, 0) AS DonGiaTheoGio,
                       NVL(kg.ToaDoX, 0) AS ToaDoX, NVL(kg.ToaDoY, 0) AS ToaDoY,
                       NVL(kg.ChieuDai, 1) AS ChieuDai, NVL(kg.ChieuRong, 1) AS ChieuRong
                FROM KHONGGIAN kg
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                LEFT JOIN LOAIKHONGGIAN lkg ON lkg.MaLoaiKG = kg.MaLoaiKG
                """;
        String sql = baseSql + """
                WHERE kg.MaCN = ?
                ORDER BY cn.TenCN, kg.TenKG
                """;
        Object[] params = { branchId };
        if (branchId == null || branchId.isBlank()) {
            sql = baseSql + "ORDER BY cn.TenCN, kg.TenKG";
            params = new Object[0];
        }
        return jdbcTemplate.query(sql, (rs, rowNum) -> new SpaceView(
                rs.getString("MaKG"),
                rs.getString("TenKG"),
                rs.getString("TenLoaiKG"),
                rs.getString("ViTri"),
                rs.getString("TrangThaiKG"),
                rs.getString("TenCN"),
                rs.getBigDecimal("DonGiaTheoGio"),
                rs.getInt("ToaDoX"),
                rs.getInt("ToaDoY"),
                rs.getInt("ChieuDai"),
                rs.getInt("ChieuRong")
        ), params);
    }

    public List<SpaceView> findSpaces(String branchId, LocalDateTime selectedStart, LocalDateTime selectedEnd) {
        if (selectedStart == null || selectedEnd == null || !selectedEnd.isAfter(selectedStart)) {
            return findSpaces(branchId);
        }

        String baseSql = """
                SELECT kg.MaKG, kg.TenKG, kg.ViTri, kg.TrangThaiKG,
                       cn.TenCN, cn.ThoiGianMoCua, cn.ThoiGianDongCua,
                       lkg.TenLoaiKG, NVL(lkg.DonGiaTheoGio, 0) AS DonGiaTheoGio,
                       NVL(kg.ToaDoX, 0) AS ToaDoX, NVL(kg.ToaDoY, 0) AS ToaDoY,
                       NVL(kg.ChieuDai, 1) AS ChieuDai, NVL(kg.ChieuRong, 1) AS ChieuRong,
                       MAX(CASE
                           WHEN p.MaPhien IS NOT NULL THEN p.ThoiGianDuKienKetThuc
                           ELSE NULL
                       END) AS BusyUntil
                FROM KHONGGIAN kg
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                LEFT JOIN LOAIKHONGGIAN lkg ON lkg.MaLoaiKG = kg.MaLoaiKG
                LEFT JOIN PHIENLAMVIEC p ON p.MaKG = kg.MaKG
                    AND p.ThoiGianBatDau < ?
                    AND p.ThoiGianDuKienKetThuc > ?
                    AND (
                        p.ThoiGianKetThuc IS NULL
                        OR p.ThoiGianKetThuc > ?
                    )
                """;
        String sql = baseSql + """
                WHERE kg.MaCN = ?
                GROUP BY kg.MaKG, kg.TenKG, kg.ViTri, kg.TrangThaiKG,
                         cn.TenCN, cn.ThoiGianMoCua, cn.ThoiGianDongCua,
                         lkg.TenLoaiKG, NVL(lkg.DonGiaTheoGio, 0),
                         NVL(kg.ToaDoX, 0), NVL(kg.ToaDoY, 0),
                         NVL(kg.ChieuDai, 1), NVL(kg.ChieuRong, 1)
                ORDER BY cn.TenCN, kg.TenKG
                """;
        Object[] params = {
                Timestamp.valueOf(selectedEnd),
                Timestamp.valueOf(selectedStart),
                Timestamp.valueOf(selectedStart),
                branchId
        };
        if (branchId == null || branchId.isBlank()) {
            sql = baseSql + """
                    GROUP BY kg.MaKG, kg.TenKG, kg.ViTri, kg.TrangThaiKG,
                             cn.TenCN, cn.ThoiGianMoCua, cn.ThoiGianDongCua,
                             lkg.TenLoaiKG, NVL(lkg.DonGiaTheoGio, 0),
                             NVL(kg.ToaDoX, 0), NVL(kg.ToaDoY, 0),
                             NVL(kg.ChieuDai, 1), NVL(kg.ChieuRong, 1)
                    ORDER BY cn.TenCN, kg.TenKG
                    """;
            params = new Object[] {
                    Timestamp.valueOf(selectedEnd),
                    Timestamp.valueOf(selectedStart),
                    Timestamp.valueOf(selectedStart)
            };
        }

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Timestamp busyUntil = rs.getTimestamp("BusyUntil");
            return new SpaceView(
                    rs.getString("MaKG"),
                    rs.getString("TenKG"),
                    rs.getString("TenLoaiKG"),
                    rs.getString("ViTri"),
                    rs.getString("TrangThaiKG"),
                    rs.getString("TenCN"),
                    rs.getString("ThoiGianMoCua"),
                    rs.getString("ThoiGianDongCua"),
                    rs.getBigDecimal("DonGiaTheoGio"),
                    rs.getInt("ToaDoX"),
                    rs.getInt("ToaDoY"),
                    rs.getInt("ChieuDai"),
                    rs.getInt("ChieuRong"),
                    busyUntil == null,
                    busyUntil == null ? null : busyUntil.toLocalDateTime()
            );
        }, params);
    }

    public SpaceView findSpaceById(String maKG) {
        String sql = """
                SELECT kg.MaKG, kg.TenKG, kg.ViTri, kg.TrangThaiKG,
                       cn.TenCN, cn.ThoiGianMoCua, cn.ThoiGianDongCua,
                       lkg.TenLoaiKG, NVL(lkg.DonGiaTheoGio, 0) AS DonGiaTheoGio,
                       NVL(kg.ToaDoX, 0) AS ToaDoX, NVL(kg.ToaDoY, 0) AS ToaDoY,
                       NVL(kg.ChieuDai, 1) AS ChieuDai, NVL(kg.ChieuRong, 1) AS ChieuRong
                FROM KHONGGIAN kg
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                LEFT JOIN LOAIKHONGGIAN lkg ON lkg.MaLoaiKG = kg.MaLoaiKG
                WHERE kg.MaKG = ?
                """;
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new SpaceView(
                    rs.getString("MaKG"),
                    rs.getString("TenKG"),
                    rs.getString("TenLoaiKG"),
                    rs.getString("ViTri"),
                    rs.getString("TrangThaiKG"),
                    rs.getString("TenCN"),
                    rs.getString("ThoiGianMoCua"),
                    rs.getString("ThoiGianDongCua"),
                    rs.getBigDecimal("DonGiaTheoGio"),
                    rs.getInt("ToaDoX"),
                    rs.getInt("ToaDoY"),
                    rs.getInt("ChieuDai"),
                    rs.getInt("ChieuRong")
            ), maKG);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public boolean hasScheduleConflict(String maKG, LocalDateTime selectedStart, LocalDateTime selectedEnd) {
        if (maKG == null || selectedStart == null || selectedEnd == null || !selectedEnd.isAfter(selectedStart)) {
            return true;
        }
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM PHIENLAMVIEC
                WHERE MaKG = ?
                  AND ThoiGianBatDau < ?
                  AND ThoiGianDuKienKetThuc > ?
                  AND (
                      ThoiGianKetThuc IS NULL
                      OR ThoiGianKetThuc > ?
                  )
                """,
                Integer.class,
                maKG,
                Timestamp.valueOf(selectedEnd),
                Timestamp.valueOf(selectedStart),
                Timestamp.valueOf(selectedStart)
        );
        return count != null && count > 0;
    }

    public List<VoucherView> findActiveVouchers() {
        String sql = """
                SELECT MaPGG, MaChuSoPGG, GiaTriGiamGia, GiaTriApDungToiThieu,
                       NgayKetThucApDung, SLDaDung, SLToiDa
                FROM PHIEUGIAMGIA
                WHERE CAST(SYSTIMESTAMP AS TIMESTAMP) BETWEEN NgayBatDauApDung AND NgayKetThucApDung
                  AND NVL(SLDaDung, 0) < NVL(SLToiDa, 0)
                ORDER BY NgayKetThucApDung ASC
                """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapVoucher(rs));
        } catch (RuntimeException ex) {
            return List.of();
        }
    }

    public VoucherView findActiveVoucherByCode(String voucherCode) {
        if (voucherCode == null || voucherCode.isBlank()) {
            return null;
        }
        String sql = """
                SELECT MaPGG, MaChuSoPGG, GiaTriGiamGia, GiaTriApDungToiThieu,
                       NgayKetThucApDung, SLDaDung, SLToiDa
                FROM PHIEUGIAMGIA
                WHERE UPPER(MaChuSoPGG) = UPPER(?)
                  AND CAST(SYSTIMESTAMP AS TIMESTAMP) BETWEEN NgayBatDauApDung AND NgayKetThucApDung
                  AND NVL(SLDaDung, 0) < NVL(SLToiDa, 0)
                """;
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapVoucher(rs), voucherCode.trim());
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public String nextBookingId() {
        Integer maxNumber = jdbcTemplate.queryForObject(
                "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MaDatCho, '[0-9]+'))), 0) FROM DATCHO",
                Integer.class
        );
        int next = maxNumber == null ? 1 : maxNumber + 1;
        return String.format("DC%06d", next);
    }

    public String nextSessionId() {
        Integer maxNumber = jdbcTemplate.queryForObject(
                "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MaPhien, '[0-9]+'))), 0) FROM PHIENLAMVIEC",
                Integer.class
        );
        int next = maxNumber == null ? 1 : maxNumber + 1;
        return String.format("PH%04d", next);
    }

    public String nextInvoiceId() {
        Integer maxNumber = jdbcTemplate.queryForObject(
                "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MaHoaDon, '[0-9]+'))), 0) FROM HOADON",
                Integer.class
        );
        int next = maxNumber == null ? 1 : maxNumber + 1;
        return String.format("HD%06d", next);
    }

    public void createBooking(String maDatCho, String maPhien, String maHoaDon,
                              SessionUser user, String maKG, LocalDateTime arrivalTime,
                              Integer durationHours, BigDecimal totalAmount, String note) {
        jdbcTemplate.update(
                """
                INSERT INTO DATCHO
                    (MaDatCho, ThoiGianDat, ThoiGianDuKienToi, KhoangThoiGianSuDung,
                     TrangThaiDatTruoc, ThanhTien, GhiChu, CapNhatLanCuoi, MaKH, MaKG)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)
                """,
                maDatCho,
                Timestamp.valueOf(LocalDateTime.now()),
                Timestamp.valueOf(arrivalTime),
                durationHours,
                dbValue("CHK_DC_TRANGTHAI", "dang cho thanh toan", 0, "Đang chờ thanh toán"),
                totalAmount,
                note,
                user.getMaKH(),
                maKG
        );

        jdbcTemplate.update(
                """
                INSERT INTO PHIENLAMVIEC
                    (MaPhien, ThoiGianBatDau, ThoiGianDuKienKetThuc, TrangThaiPhien,
                     CapNhatLanCuoi, MaKG, MaKH, MaDatCho)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?)
                """,
                maPhien,
                Timestamp.valueOf(arrivalTime),
                Timestamp.valueOf(arrivalTime.plusHours(durationHours)),
                dbValue("CHK_PLV_TRANGTHAI", "da dat truoc", 1, "Đã đặt trước"),
                maKG,
                user.getMaKH(),
                maDatCho
        );

        jdbcTemplate.update(
                """
                INSERT INTO HOADON
                    (MaHoaDon, SoHD, TongTien, ThanhTien, NgayLapHoaDon, TrangThaiThanhToan, MaPhien)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)
                """,
                maHoaDon,
                maHoaDon,
                totalAmount,
                totalAmount,
                dbValue("CHK_HD_TRANGTHAI", "dang cho thanh toan", 0, "Đang chờ thanh toán"),
                maPhien
        );
    }

    public void createMissingSessionsForBookings() {
        String sql = """
                SELECT dc.MaDatCho, dc.MaKG, dc.MaKH, dc.ThoiGianDuKienToi,
                       dc.KhoangThoiGianSuDung, dc.ThanhTien
                FROM DATCHO dc
                WHERE NOT EXISTS (
                    SELECT 1 FROM PHIENLAMVIEC p WHERE p.MaDatCho = dc.MaDatCho
                )
                ORDER BY dc.ThoiGianDat ASC
                """;
        List<PendingSessionRow> pendingRows = jdbcTemplate.query(sql, (rs, rowNum) -> new PendingSessionRow(
                rs.getString("MaDatCho"),
                rs.getString("MaKG"),
                rs.getString("MaKH"),
                rs.getTimestamp("ThoiGianDuKienToi"),
                rs.getInt("KhoangThoiGianSuDung"),
                rs.getBigDecimal("ThanhTien")
        ));

        for (PendingSessionRow row : pendingRows) {
            int durationHours = Math.max(row.durationHours(), 1);
            LocalDateTime startTime = row.arrivalTime() == null
                    ? LocalDateTime.now()
                    : row.arrivalTime().toLocalDateTime();
            String maPhien = nextSessionId();
            String maHoaDon = nextInvoiceId();

            jdbcTemplate.update(
                    """
                    INSERT INTO PHIENLAMVIEC
                        (MaPhien, ThoiGianBatDau, ThoiGianDuKienKetThuc, TrangThaiPhien,
                         CapNhatLanCuoi, MaKG, MaKH, MaDatCho)
                    VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?)
                    """,
                    maPhien,
                    Timestamp.valueOf(startTime),
                    Timestamp.valueOf(startTime.plusHours(durationHours)),
                    dbValue("CHK_PLV_TRANGTHAI", "da dat truoc", 1, "Đã đặt trước"),
                    row.maKG(),
                    row.maKH(),
                    row.maDatCho()
            );

            jdbcTemplate.update(
                    """
                    INSERT INTO HOADON
                        (MaHoaDon, SoHD, TongTien, ThanhTien, NgayLapHoaDon, TrangThaiThanhToan, MaPhien)
                    VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)
                    """,
                    maHoaDon,
                    maHoaDon,
                    row.totalAmount() == null ? BigDecimal.ZERO : row.totalAmount(),
                    row.totalAmount() == null ? BigDecimal.ZERO : row.totalAmount(),
                    dbValue("CHK_HD_TRANGTHAI", "dang cho thanh toan", 0, "Đang chờ thanh toán"),
                    maPhien
            );
        }
    }

    public List<BookingView> findBookingsForMember(String maKH) {
        if (maKH == null || maKH.isBlank()) {
            return List.of();
        }
        String sql = """
                SELECT dc.MaDatCho, nd.HoTen, kg.TenKG, cn.TenCN, dc.ThoiGianDuKienToi,
                       dc.KhoangThoiGianSuDung, dc.TrangThaiDatTruoc, dc.ThanhTien, dc.GhiChu
                FROM DATCHO dc
                JOIN KHACHHANG kh ON kh.MaKH = dc.MaKH
                JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND
                JOIN KHONGGIAN kg ON kg.MaKG = dc.MaKG
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                WHERE dc.MaKH = ?
                ORDER BY dc.ThoiGianDat DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapBooking(rs), maKH);
    }

    public List<BookingView> findAllBookings() {
        String sql = """
                SELECT dc.MaDatCho, nd.HoTen, kg.TenKG, cn.TenCN, dc.ThoiGianDuKienToi,
                       dc.KhoangThoiGianSuDung, dc.TrangThaiDatTruoc, dc.ThanhTien, dc.GhiChu
                FROM DATCHO dc
                JOIN KHACHHANG kh ON kh.MaKH = dc.MaKH
                JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND
                JOIN KHONGGIAN kg ON kg.MaKG = dc.MaKG
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                ORDER BY dc.ThoiGianDat DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapBooking(rs));
    }

    public void updateBookingStatus(String maDatCho, String status, String suffixNote) {
        jdbcTemplate.update(
                "UPDATE DATCHO SET TrangThaiDatTruoc = ?, GhiChu = NVL(GhiChu, '') || ?, CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaDatCho = ?",
                dbBookingStatus(status),
                suffixNote,
                maDatCho
        );
    }

    public void updateInvoiceStatusByBooking(String maDatCho, String status) {
        jdbcTemplate.update(
                """
                UPDATE HOADON
                SET TrangThaiThanhToan = ?, NgayLapHoaDon = CURRENT_TIMESTAMP
                WHERE MaPhien IN (SELECT MaPhien FROM PHIENLAMVIEC WHERE MaDatCho = ?)
                """,
                dbInvoiceStatus(status),
                maDatCho
        );
    }

    public List<MemberSessionView> findMemberSessions(String maKH) {
        String sql = """
                SELECT p.MaPhien, p.MaKG, kg.TenKG, cn.TenCN, p.ThoiGianBatDau,
                       p.ThoiGianDuKienKetThuc, p.ThoiGianKetThuc, p.TrangThaiPhien,
                       cn.ThoiGianDongCua
                FROM PHIENLAMVIEC p
                JOIN KHONGGIAN kg ON kg.MaKG = p.MaKG
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                WHERE p.MaKH = ?
                ORDER BY p.ThoiGianBatDau DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapMemberSession(rs), maKH);
    }

    public MemberSessionView findMemberSession(String maKH, String maPhien) {
        String sql = """
                SELECT p.MaPhien, p.MaKG, kg.TenKG, cn.TenCN, p.ThoiGianBatDau,
                       p.ThoiGianDuKienKetThuc, p.ThoiGianKetThuc, p.TrangThaiPhien,
                       cn.ThoiGianDongCua
                FROM PHIENLAMVIEC p
                JOIN KHONGGIAN kg ON kg.MaKG = p.MaKG
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                WHERE p.MaKH = ? AND p.MaPhien = ?
                """;
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapMemberSession(rs), maKH, maPhien);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public List<ServiceOptionView> findServiceOptions() {
        String sql = """
                SELECT dv.MaDV, dv.TenDV, ldv.TenLoaiDV, NVL(dv.DonGia, 0) AS DonGia, dv.SoLuong
                FROM DICHVU dv
                LEFT JOIN LOAIDICHVU ldv ON ldv.MaLoaiDV = dv.MaLoaiDV
                ORDER BY ldv.TenLoaiDV, dv.TenDV
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ServiceOptionView(
                rs.getString("MaDV"),
                decodeMojibake(rs.getString("TenDV")),
                decodeMojibake(rs.getString("TenLoaiDV")),
                rs.getBigDecimal("DonGia"),
                rs.getObject("SoLuong") == null ? null : rs.getInt("SoLuong")
        ));
    }

    public ServiceOptionView findServiceOption(String maDV) {
        String sql = """
                SELECT dv.MaDV, dv.TenDV, ldv.TenLoaiDV, NVL(dv.DonGia, 0) AS DonGia, dv.SoLuong
                FROM DICHVU dv
                LEFT JOIN LOAIDICHVU ldv ON ldv.MaLoaiDV = dv.MaLoaiDV
                WHERE dv.MaDV = ?
                """;
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new ServiceOptionView(
                    rs.getString("MaDV"),
                    decodeMojibake(rs.getString("TenDV")),
                    decodeMojibake(rs.getString("TenLoaiDV")),
                    rs.getBigDecimal("DonGia"),
                    rs.getObject("SoLuong") == null ? null : rs.getInt("SoLuong")
            ), maDV);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public List<SessionServiceView> findServicesForMemberSession(String maKH, String maPhien) {
        String sql = """
                SELECT ct.MaDV, dv.TenDV, ldv.TenLoaiDV, ct.SoLuong,
                       NVL(dv.DonGia, 0) AS DonGia,
                       NVL(ct.SoLuong, 0) * NVL(dv.DonGia, 0) AS ThanhTien,
                       ct.GhiChu
                FROM CHITIETDICHVU ct
                JOIN DICHVU dv ON dv.MaDV = ct.MaDV
                LEFT JOIN LOAIDICHVU ldv ON ldv.MaLoaiDV = dv.MaLoaiDV
                JOIN PHIENLAMVIEC p ON p.MaPhien = ct.MaPhien
                WHERE p.MaKH = ? AND ct.MaPhien = ?
                ORDER BY ldv.TenLoaiDV, dv.TenDV
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new SessionServiceView(
                rs.getString("MaDV"),
                decodeMojibake(rs.getString("TenDV")),
                decodeMojibake(rs.getString("TenLoaiDV")),
                rs.getInt("SoLuong"),
                rs.getBigDecimal("DonGia"),
                rs.getBigDecimal("ThanhTien"),
                rs.getString("GhiChu")
        ), maKH, maPhien);
    }

    public void addPaidServiceToSession(String maPhien, ServiceOptionView service,
                                        int quantity, String note, BigDecimal totalAmount) {
        jdbcTemplate.update(
                """
                MERGE INTO CHITIETDICHVU dest
                USING (SELECT ? AS MaPhien, ? AS MaDV, ? AS SoLuong, ? AS GhiChu FROM DUAL) src
                ON (dest.MaPhien = src.MaPhien AND dest.MaDV = src.MaDV)
                WHEN MATCHED THEN
                    UPDATE SET dest.SoLuong = dest.SoLuong + src.SoLuong,
                               dest.GhiChu = NVL(src.GhiChu, dest.GhiChu)
                WHEN NOT MATCHED THEN
                    INSERT (MaPhien, MaDV, SoLuong, GhiChu)
                    VALUES (src.MaPhien, src.MaDV, src.SoLuong, src.GhiChu)
                """,
                maPhien,
                service.getMaDV(),
                quantity,
                note
        );

        if (isExtensionService(service)) {
            jdbcTemplate.update(
                    "UPDATE PHIENLAMVIEC SET ThoiGianDuKienKetThuc = ThoiGianDuKienKetThuc + INTERVAL '1' HOUR * ?, CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaPhien = ?",
                    quantity,
                    maPhien
            );
        }

        jdbcTemplate.update(
                """
                UPDATE HOADON
                SET TongTien = NVL(TongTien, 0) + ?,
                    ThanhTien = NVL(ThanhTien, 0) + ?,
                    PhuongThucThanhToan = NVL(PhuongThucThanhToan, ?),
                    TrangThaiThanhToan = ?,
                    NgayLapHoaDon = CURRENT_TIMESTAMP
                WHERE MaPhien = ?
                """,
                totalAmount,
                totalAmount,
                dbValue("CHK_HD_PTTT", "chuyen khoan", 0, "Chuyển khoản"),
                dbInvoiceStatus("Da thanh toan thanh cong"),
                maPhien
        );
    }

    private MemberSessionView mapMemberSession(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp start = rs.getTimestamp("ThoiGianBatDau");
        Timestamp expectedEnd = rs.getTimestamp("ThoiGianDuKienKetThuc");
        Timestamp actualEnd = rs.getTimestamp("ThoiGianKetThuc");
        return new MemberSessionView(
                rs.getString("MaPhien"),
                rs.getString("MaKG"),
                rs.getString("TenKG"),
                rs.getString("TenCN"),
                start == null ? null : start.toLocalDateTime(),
                expectedEnd == null ? null : expectedEnd.toLocalDateTime(),
                actualEnd == null ? null : actualEnd.toLocalDateTime(),
                displaySessionStatus(rs.getString("TrangThaiPhien")),
                rs.getString("ThoiGianDongCua")
        );
    }

    private BookingView mapBooking(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp arrivalTime = rs.getTimestamp("ThoiGianDuKienToi");
        int durationHours = rs.getInt("KhoangThoiGianSuDung");
        return new BookingView(
                rs.getString("MaDatCho"),
                rs.getString("HoTen"),
                rs.getString("TenKG"),
                rs.getString("TenCN"),
                arrivalTime == null ? null : arrivalTime.toLocalDateTime(),
                rs.wasNull() ? null : durationHours,
                displayStatus(rs.getString("TrangThaiDatTruoc")),
                rs.getBigDecimal("ThanhTien"),
                rs.getString("GhiChu")
        );
    }

    private VoucherView mapVoucher(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp endTime = rs.getTimestamp("NgayKetThucApDung");
        int used = rs.getInt("SLDaDung");
        Integer usedValue = rs.wasNull() ? null : used;
        int max = rs.getInt("SLToiDa");
        Integer maxValue = rs.wasNull() ? null : max;
        return new VoucherView(
                rs.getString("MaPGG"),
                rs.getString("MaChuSoPGG"),
                rs.getBigDecimal("GiaTriGiamGia"),
                rs.getBigDecimal("GiaTriApDungToiThieu"),
                endTime == null ? null : endTime.toLocalDateTime(),
                usedValue,
                maxValue
        );
    }

    private String dbValue(String constraintName, String normalizedNeedle, int fallbackIndex, String fallbackValue) {
        List<String> values = constraintValues(constraintName);
        for (String value : values) {
            if (normalize(value).contains(normalizedNeedle)) {
                return value;
            }
        }
        if (!values.isEmpty()) {
            int index = Math.max(0, Math.min(fallbackIndex, values.size() - 1));
            return values.get(index);
        }
        return fallbackValue;
    }

    private String dbBookingStatus(String status) {
        String normalized = normalize(status);
        if (normalized.contains("thanh cong")) {
            return dbValue("CHK_DC_TRANGTHAI", "thanh cong", 1, "Đã thanh toán thành công");
        }
        if (normalized.contains("khong thanh cong")) {
            return dbValue("CHK_DC_TRANGTHAI", "khong thanh cong", 2, "Thanh toán không thành công");
        }
        if (normalized.contains("su dung")) {
            return dbValue("CHK_DC_TRANGTHAI", "su dung", 3, "Đã sử dụng");
        }
        return dbValue("CHK_DC_TRANGTHAI", "cho thanh toan", 0, "Đang chờ thanh toán");
    }

    private String dbInvoiceStatus(String status) {
        String normalized = normalize(status);
        if (normalized.contains("thanh cong")) {
            return dbValue("CHK_HD_TRANGTHAI", "thanh cong", 1, "Đã thanh toán thành công");
        }
        if (normalized.contains("khong thanh cong")) {
            return dbValue("CHK_HD_TRANGTHAI", "khong thanh cong", 2, "Thanh toán không thành công");
        }
        return dbValue("CHK_HD_TRANGTHAI", "cho thanh toan", 0, "Đang chờ thanh toán");
    }

    private List<String> constraintValues(String constraintName) {
        List<String> values = new ArrayList<>();
        try {
            String condition = jdbcTemplate.queryForObject(
                    "SELECT search_condition_vc FROM user_constraints WHERE constraint_name = ?",
                    String.class,
                    constraintName
            );
            if (condition != null) {
                Matcher matcher = Pattern.compile("'([^']*)'").matcher(condition);
                while (matcher.find()) {
                    values.add(matcher.group(1));
                }
            }
        } catch (RuntimeException ignored) {
            // Fallback labels above keep the student demo usable even if metadata cannot be read.
        }
        return values;
    }

    private String normalize(String value) {
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

    private String displayStatus(String value) {
        if (value == null || value.isBlank()) {
            return "Chưa có trạng thái";
        }

        String decoded = decodeMojibake(value);
        String normalized = normalize(decoded);
        if (normalized.contains("cho thanh toan")) {
            return "Đang chờ thanh toán";
        }
        if (normalized.contains("thanh toan thanh cong")) {
            return "Đã thanh toán thành công";
        }
        if (normalized.contains("thanh toan khong thanh cong")) {
            return "Thanh toán không thành công";
        }
        if (normalized.contains("da su dung")) {
            return "Đã sử dụng";
        }
        return decoded;
    }

    private String displaySessionStatus(String value) {
        if (value == null || value.isBlank()) {
            return "Chua co trang thai";
        }
        String decoded = decodeMojibake(value);
        String normalized = normalize(decoded);
        if (normalized.contains("dang hoat dong")) {
            return "Dang hoat dong";
        }
        if (normalized.contains("dat tr")) {
            return "Da dat truoc";
        }
        if (normalized.contains("ket thuc")) {
            return "Da ket thuc";
        }
        return decoded;
    }

    private boolean isExtensionService(ServiceOptionView service) {
        if (service == null) {
            return false;
        }
        return "DV000".equalsIgnoreCase(service.getMaDV()) || normalize(service.getTenDV()).contains("gia han gio");
    }

    private String decodeMojibake(String value) {
        try {
            String decoded = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            return decoded.indexOf('\uFFFD') >= 0 ? value : decoded;
        } catch (RuntimeException ex) {
            return value;
        }
    }

    public record AuthRecord(
            String maND,
            String hoTen,
            String tenTaiKhoan,
            String matKhauMaHoa,
            String trangThaiND,
            String maKH,
            String maNV
    ) {
        public boolean isStaff() {
            return maNV != null && !maNV.isBlank();
        }
    }

    private record PendingSessionRow(
            String maDatCho,
            String maKG,
            String maKH,
            Timestamp arrivalTime,
            int durationHours,
            BigDecimal totalAmount
    ) {
    }
}
