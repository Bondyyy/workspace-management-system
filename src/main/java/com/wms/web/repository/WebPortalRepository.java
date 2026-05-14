package com.wms.web.repository;

import com.wms.web.model.BookingView;
import com.wms.web.model.BranchView;
import com.wms.web.model.SessionUser;
import com.wms.web.model.SpaceView;
import com.wms.web.model.VoucherView;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
                "INSERT INTO KHACHHANG (MaKH, TongChiTieu, CapNhatLanCuoi, MaND) VALUES (?, 0, CURRENT_TIMESTAMP, ?)",
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
                SELECT kg.MaKG, kg.TenKG, kg.ViTri, kg.TrangThaiKG, cn.TenCN,
                       lkg.TenLoaiKG, NVL(lkg.DonGiaTheoGio, 0) AS DonGiaTheoGio
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
                rs.getBigDecimal("DonGiaTheoGio")
        ), params);
    }

    public SpaceView findSpaceById(String maKG) {
        String sql = """
                SELECT kg.MaKG, kg.TenKG, kg.ViTri, kg.TrangThaiKG, cn.TenCN,
                       lkg.TenLoaiKG, NVL(lkg.DonGiaTheoGio, 0) AS DonGiaTheoGio
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
                    rs.getBigDecimal("DonGiaTheoGio")
            ), maKG);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
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

    public void createBooking(String maDatCho, SessionUser user, String maKG,
                              LocalDateTime arrivalTime, Integer durationHours,
                              BigDecimal totalAmount, String note) {
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
                dbValue("CHK_DC_TRANGTHAI", normalize(status), 0, status),
                suffixNote,
                maDatCho
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
                rs.getString("TrangThaiDatTruoc"),
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
}
