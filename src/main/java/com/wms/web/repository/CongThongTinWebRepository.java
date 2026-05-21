package com.wms.web.repository;

import com.wms.web.model.DatChoView;
import com.wms.web.model.ThongTinTaiKhoanView;
import com.wms.web.model.LichSuDatChoView;
import com.wms.web.model.ThanhToanDatChoView;
import com.wms.web.model.ChiNhanhView;
import com.wms.web.model.PhienHoiVienView;
import com.wms.web.model.TuyChonDichVuView;
import com.wms.web.model.DichVuTrongPhienView;
import com.wms.web.model.NguoiDungPhien;
import com.wms.web.model.KhongGianView;
import com.wms.web.model.PhieuGiamGiaView;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.ThongTinXacNhanDatChoDTO;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class CongThongTinWebRepository {

    private final JdbcTemplate mauJdbc;

    public CongThongTinWebRepository(JdbcTemplate mauJdbc) {
        this.mauJdbc = mauJdbc;
    }

    public BanGhiXacThuc timThongTinXacThuc(String identifier) {
        String sql = """
                SELECT n.MaND, n.HoTen, n.TenTaiKhoan, n.MatKhauMaHoa, n.TrangThaiND,
                       kh.MaKH, nv.MaNV
                FROM NGUOIDUNG n
                LEFT JOIN KHACHHANG kh ON kh.MaND = n.MaND
                LEFT JOIN NHANVIEN nv ON nv.MaND = n.MaND
                WHERE n.TenTaiKhoan = ? OR n.Email = ? OR n.SDT = ?
                """;
        try {
            return mauJdbc.queryForObject(sql, (rs, rowNum) -> new BanGhiXacThuc(
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

    public boolean tonTaiTenTaiKhoan(String username) {
        Integer count = mauJdbc.queryForObject(
                "SELECT COUNT(*) FROM NGUOIDUNG WHERE TenTaiKhoan = ?",
                Integer.class,
                username
        );
        return count != null && count > 0;
    }

    public boolean tonTaiEmail(String email) {
        Integer count = mauJdbc.queryForObject(
                "SELECT COUNT(*) FROM NGUOIDUNG WHERE Email = ?",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }

    public void taoHoiVien(String fullName, String username, String email, String hashedPassword) {
        String maND = UUID.randomUUID().toString();
        String maKH = "KH_" + maND;

        mauJdbc.update(
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
                giaTriDb("CHK_ND_TRANGTHAI", "dang hoat dong", 0, "Đang hoạt động")
        );

        mauJdbc.update(
                "INSERT INTO KHACHHANG (MaKH, MaHangThanhVien, TongChiTieu, CapNhatLanCuoi, MaND) VALUES (?, 'HTV01', 0, CURRENT_TIMESTAMP, ?)",
                maKH,
                maND
        );
    }

    public void capNhatLanDangNhapCuoi(String maND) {
        mauJdbc.update(
                "UPDATE NGUOIDUNG SET LanCuoiDangNhap = CURRENT_TIMESTAMP WHERE MaND = ?",
                maND
        );
    }

    public ThongTinTaiKhoanView timThongTinTaiKhoan(String maND) {
        if (maND == null || maND.isBlank()) {
            return null;
        }
        String sql = """
                SELECT n.MaND, n.HoTen, n.TenTaiKhoan, n.Email, n.SDT, n.NgaySinh, n.GioiTinh,
                       NVL(htv.TenHangThanhVien, 'Không có') AS TenHangThanhVien
                FROM NGUOIDUNG n
                LEFT JOIN KHACHHANG kh ON kh.MaND = n.MaND
                LEFT JOIN HANGTHANHVIEN htv ON htv.MaHangThanhVien = kh.MaHangThanhVien
                WHERE n.MaND = ?
                """;
        try {
            return mauJdbc.queryForObject(sql, (rs, rowNum) -> {
                java.sql.Date birthDate = rs.getDate("NgaySinh");
                return new ThongTinTaiKhoanView(
                        rs.getString("MaND"),
                        rs.getString("HoTen"),
                        rs.getString("TenTaiKhoan"),
                        rs.getString("Email"),
                        rs.getString("SDT"),
                        birthDate == null ? null : birthDate.toLocalDate(),
                        rs.getString("GioiTinh"),
                        rs.getString("TenHangThanhVien")
                );
            }, maND);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public void capNhatThongTinTaiKhoan(String maND, String hoTen, String email, String soDienThoai,
                                     LocalDate ngaySinh, String gioiTinh) {
        mauJdbc.update(
                """
                UPDATE NGUOIDUNG
                SET HoTen = ?,
                    Email = ?,
                    SDT = ?,
                    NgaySinh = ?,
                    GioiTinh = ?,
                    CapNhatLanCuoi = CURRENT_TIMESTAMP
                WHERE MaND = ?
                """,
                hoTen,
                rongThanhNull(email),
                rongThanhNull(soDienThoai),
                ngaySinh == null ? null : Date.valueOf(ngaySinh),
                rongThanhNull(gioiTinh),
                maND
        );
    }

    public List<ChiNhanhView> timChiNhanhHoatDong() {
        String sql = """
                SELECT MaCN, TenCN, DiaChi, ThoiGianMoCua, ThoiGianDongCua, DuongDayNong
                FROM CHINHANH
                ORDER BY TenCN
                """;
        return mauJdbc.query(sql, (rs, rowNum) -> new ChiNhanhView(
                rs.getString("MaCN"),
                rs.getString("TenCN"),
                rs.getString("DiaChi"),
                rs.getString("ThoiGianMoCua"),
                rs.getString("ThoiGianDongCua"),
                rs.getString("DuongDayNong")
        ));
    }

    public List<KhongGianView> timKhongGian(String branchId) {
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
        return mauJdbc.query(sql, (rs, rowNum) -> new KhongGianView(
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

    public List<KhongGianView> timKhongGian(String branchId, LocalDateTime selectedStart, LocalDateTime selectedEnd) {
        if (selectedStart == null || selectedEnd == null || !selectedEnd.isAfter(selectedStart)) {
            return timKhongGian(branchId);
        }

        String baseSql = """
                SELECT kg.MaKG, kg.TenKG, kg.ViTri, kg.TrangThaiKG,
                       cn.TenCN, cn.ThoiGianMoCua, cn.ThoiGianDongCua,
                       lkg.TenLoaiKG, NVL(lkg.DonGiaTheoGio, 0) AS DonGiaTheoGio,
                       NVL(kg.ToaDoX, 0) AS ToaDoX, NVL(kg.ToaDoY, 0) AS ToaDoY,
                       NVL(kg.ChieuDai, 1) AS ChieuDai, NVL(kg.ChieuRong, 1) AS ChieuRong,
                       MAX(CASE
                           WHEN p.MaPhien IS NOT NULL THEN p.ThoiGianDuKienKetThuc
                           WHEN dc.MaDatCho IS NOT NULL
                               THEN dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR')
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
                LEFT JOIN DATCHO dc ON dc.MaKG = kg.MaKG
                    AND dc.ThoiGianDuKienToi < ?
                    AND (dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR')) > ?
                    AND (
                        dc.TrangThaiDatTruoc = ?
                        OR (
                            dc.TrangThaiDatTruoc = ?
                            AND dc.ThoiGianDat >= SYSTIMESTAMP - INTERVAL '10' MINUTE
                        )
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
                Timestamp.valueOf(selectedEnd),
                Timestamp.valueOf(selectedStart),
                trangThaiDatChoDb("Da thanh toan thanh cong"),
                trangThaiDatChoDb("Dang cho thanh toan"),
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
                    Timestamp.valueOf(selectedStart),
                    Timestamp.valueOf(selectedEnd),
                    Timestamp.valueOf(selectedStart),
                    trangThaiDatChoDb("Da thanh toan thanh cong"),
                    trangThaiDatChoDb("Dang cho thanh toan")
            };
        }

        return mauJdbc.query(sql, (rs, rowNum) -> {
            Timestamp busyUntil = rs.getTimestamp("BusyUntil");
            return new KhongGianView(
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

    public KhongGianView timKhongGianTheoMa(String maKG) {
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
            return mauJdbc.queryForObject(sql, (rs, rowNum) -> new KhongGianView(
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

    public boolean coTrungLich(String maKG, LocalDateTime selectedStart, LocalDateTime selectedEnd) {
        if (maKG == null || selectedStart == null || selectedEnd == null || !selectedEnd.isAfter(selectedStart)) {
            return true;
        }
        Integer count = mauJdbc.queryForObject(
                """
                SELECT
                    (SELECT COUNT(*)
                     FROM PHIENLAMVIEC
                     WHERE MaKG = ?
                       AND ThoiGianBatDau < ?
                       AND ThoiGianDuKienKetThuc > ?
                       AND (
                           ThoiGianKetThuc IS NULL
                           OR ThoiGianKetThuc > ?
                       ))
                    +
                    (SELECT COUNT(*)
                     FROM DATCHO
                     WHERE MaKG = ?
                       AND ThoiGianDuKienToi < ?
                       AND (ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(KhoangThoiGianSuDung, 1), 'HOUR')) > ?
                       AND (
                           TrangThaiDatTruoc = ?
                           OR (
                               TrangThaiDatTruoc = ?
                               AND ThoiGianDat >= SYSTIMESTAMP - INTERVAL '10' MINUTE
                           )
                       ))
                FROM DUAL
                """,
                Integer.class,
                maKG,
                Timestamp.valueOf(selectedEnd),
                Timestamp.valueOf(selectedStart),
                Timestamp.valueOf(selectedStart),
                maKG,
                Timestamp.valueOf(selectedEnd),
                Timestamp.valueOf(selectedStart),
                trangThaiDatChoDb("Da thanh toan thanh cong"),
                trangThaiDatChoDb("Dang cho thanh toan")
        );
        return count != null && count > 0;
    }

    public List<PhieuGiamGiaView> timPhieuGiamGiaHieuLuc() {
        String sql = """
                SELECT MaPGG, MaChuSoPGG, GiaTriGiamGia, GiaTriApDungToiThieu,
                       NgayKetThucApDung, SLDaDung, SLToiDa
                FROM PHIEUGIAMGIA
                WHERE CAST(SYSTIMESTAMP AS TIMESTAMP) BETWEEN NgayBatDauApDung AND NgayKetThucApDung
                  AND NVL(SLDaDung, 0) < NVL(SLToiDa, 0)
                ORDER BY NgayKetThucApDung ASC
                """;
        try {
            return mauJdbc.query(sql, (rs, rowNum) -> anhXaPhieuGiamGia(rs));
        } catch (RuntimeException ex) {
            return List.of();
        }
    }

    public PhieuGiamGiaView timPhieuGiamGiaHieuLucTheoMa(String voucherCode) {
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
            return mauJdbc.queryForObject(sql, (rs, rowNum) -> anhXaPhieuGiamGia(rs), voucherCode.trim());
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public String taoMaDatChoTiepTheo() {
        Integer maxNumber = mauJdbc.queryForObject(
                "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MaDatCho, '[0-9]+'))), 0) FROM DATCHO",
                Integer.class
        );
        int next = maxNumber == null ? 1 : maxNumber + 1;
        return String.format("DC%06d", next);
    }

    public String taoMaPhienTiepTheo() {
        Integer maxNumber = mauJdbc.queryForObject(
                "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MaPhien, '[0-9]+'))), 0) FROM PHIENLAMVIEC",
                Integer.class
        );
        int next = maxNumber == null ? 1 : maxNumber + 1;
        return String.format("PH%04d", next);
    }

    public String taoMaHoaDonTiepTheo() {
        Integer maxNumber = mauJdbc.queryForObject(
                "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MaHoaDon, '[0-9]+'))), 0) FROM HOADON",
                Integer.class
        );
        int next = maxNumber == null ? 1 : maxNumber + 1;
        return String.format("HD%06d", next);
    }

    public void taoDatCho(String maDatCho, NguoiDungPhien user, String maKG, LocalDateTime arrivalTime,
                              Integer durationHours, BigDecimal totalAmount, String note) {
        mauJdbc.update(
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
                giaTriDb("CHK_DC_TRANGTHAI", "dang cho thanh toan", 0, "Đang chờ thanh toán"),
                totalAmount,
                note,
                user.getMaKH(),
                maKG
        );

        mauJdbc.update(
                "UPDATE KHONGGIAN SET TrangThaiKG = ? WHERE MaKG = ?",
                trangThaiKhongGianDb("Tam khoa"),
                maKG
        );
    }

    public void taoPhienConThieuChoDatCho() {
        // New pre-booking flow creates sessions only when staff check in a paid QR.
    }

    public List<DatChoView> timDatChoTheoHoiVien(String maKH) {
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
        return mauJdbc.query(sql, (rs, rowNum) -> anhXaDatCho(rs), maKH);
    }

    public List<DatChoView> timTatCaDatCho() {
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
        return mauJdbc.query(sql, (rs, rowNum) -> anhXaDatCho(rs));
    }

    public ThanhToanDatChoView timThanhToanDatCho(String maDatCho, String maKH) {
        if (maDatCho == null || maDatCho.isBlank()) {
            return null;
        }
        String sql = """
                SELECT dc.MaDatCho, nd.HoTen, kg.TenKG, cn.TenCN, dc.ThoiGianDat,
                       dc.ThoiGianDuKienToi, dc.KhoangThoiGianSuDung,
                       dc.TrangThaiDatTruoc, dc.ThanhTien
                FROM DATCHO dc
                JOIN KHACHHANG kh ON kh.MaKH = dc.MaKH
                JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND
                JOIN KHONGGIAN kg ON kg.MaKG = dc.MaKG
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                WHERE dc.MaDatCho = ?
                  AND (? IS NULL OR dc.MaKH = ?)
                """;
        try {
            return mauJdbc.queryForObject(sql, (rs, rowNum) -> {
                Timestamp bookingTime = rs.getTimestamp("ThoiGianDat");
                Timestamp arrivalTime = rs.getTimestamp("ThoiGianDuKienToi");
                int durationHours = rs.getInt("KhoangThoiGianSuDung");
                boolean durationNull = rs.wasNull();
                String bookingId = rs.getString("MaDatCho");
                return new ThanhToanDatChoView(
                        bookingId,
                        rs.getString("HoTen"),
                        rs.getString("TenKG"),
                        rs.getString("TenCN"),
                        arrivalTime == null ? null : arrivalTime.toLocalDateTime(),
                        durationNull ? null : durationHours,
                        hienThiTrangThai(rs.getString("TrangThaiDatTruoc")),
                        rs.getBigDecimal("ThanhTien"),
                        transferContent(bookingId),
                        bookingTime == null ? null : bookingTime.toLocalDateTime().plusMinutes(10)
                );
            }, maDatCho, maKH, maKH);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public boolean xacNhanDatChoDaTraTien(String maDatCho, String maQR, String note) {
        int updated = mauJdbc.update(
                """
                UPDATE DATCHO
                SET TrangThaiDatTruoc = ?,
                    MaQR = ?,
                    GhiChu = NVL(GhiChu, '') || ?,
                    CapNhatLanCuoi = CURRENT_TIMESTAMP
                WHERE MaDatCho = ?
                  AND TrangThaiDatTruoc = ?
                """,
                trangThaiDatChoDb("Da thanh toan thanh cong"),
                maQR,
                note == null ? "" : note,
                maDatCho,
                trangThaiDatChoDb("Dang cho thanh toan")
        );
        if (updated == 0) {
            return false;
        }

        mauJdbc.update(
                """
                UPDATE KHONGGIAN
                SET TrangThaiKG = ?
                WHERE MaKG = (SELECT MaKG FROM DATCHO WHERE MaDatCho = ?)
                """,
                trangThaiKhongGianDb("Da dat truoc"),
                maDatCho
        );
        return true;
    }

    public boolean ghiNhanThanhToanDatChoThatBai(String maDatCho, String reason) {
        int updated = mauJdbc.update(
                """
                UPDATE DATCHO
                SET TrangThaiDatTruoc = ?,
                    MaQR = NULL,
                    GhiChu = NVL(GhiChu, '') || ?,
                    CapNhatLanCuoi = CURRENT_TIMESTAMP
                WHERE MaDatCho = ?
                  AND TrangThaiDatTruoc = ?
                """,
                trangThaiDatChoDb("Thanh toan khong thanh cong"),
                " | " + (reason == null || reason.isBlank() ? "Thanh toán không thành công." : reason.trim()),
                maDatCho,
                trangThaiDatChoDb("Dang cho thanh toan")
        );
        if (updated == 0) {
            return false;
        }

        mauJdbc.update(
                """
                UPDATE KHONGGIAN
                SET TrangThaiKG = ?
                WHERE MaKG = (SELECT MaKG FROM DATCHO WHERE MaDatCho = ?)
                """,
                trangThaiKhongGianDb("Trong"),
                maDatCho
        );
        return true;
    }

    public List<ThongTinXacNhanDatChoDTO> timDatChoChoThanhToanDaHetHan() {
        String sql = """
                SELECT CAST(NULL AS VARCHAR2(50)) AS MaPhien,
                       dc.MaDatCho, dc.MaQR, nd.HoTen, nd.Email,
                       kg.TenKG, cn.TenCN, dc.ThoiGianDuKienToi AS ThoiGianBatDau,
                       dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR') AS ThoiGianDuKienKetThuc,
                       dc.ThanhTien
                FROM DATCHO dc
                JOIN KHACHHANG kh ON kh.MaKH = dc.MaKH
                JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND
                JOIN KHONGGIAN kg ON kg.MaKG = dc.MaKG
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                WHERE dc.TrangThaiDatTruoc = ?
                  AND dc.ThoiGianDat < SYSTIMESTAMP - INTERVAL '10' MINUTE
                ORDER BY dc.ThoiGianDat ASC
                """;
        return mauJdbc.query(sql, (rs, rowNum) -> mapThongTinXacNhanDatCho(rs),
                trangThaiDatChoDb("Dang cho thanh toan"));
    }

    public boolean taoPhienChoDatChoDaCheckIn(String maDatCho, String maPhien, String maHoaDon) {
        Integer inserted = mauJdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM DATCHO dc
                WHERE MaDatCho = ?
                  AND TrangThaiDatTruoc = ?
                  AND NOT EXISTS (SELECT 1 FROM PHIENLAMVIEC p WHERE p.MaDatCho = dc.MaDatCho)
                """,
                Integer.class,
                maDatCho,
                trangThaiDatChoDb("Da thanh toan thanh cong")
        );
        if (inserted == null || inserted == 0) {
            return false;
        }

        mauJdbc.update(
                """
                INSERT INTO PHIENLAMVIEC
                    (MaPhien, ThoiGianBatDau, ThoiGianDuKienKetThuc, TrangThaiPhien,
                     CapNhatLanCuoi, MaKG, MaKH, MaDatCho)
                SELECT ?, CURRENT_TIMESTAMP,
                       CURRENT_TIMESTAMP + NUMTODSINTERVAL(NVL(KhoangThoiGianSuDung, 1), 'HOUR'),
                       ?, CURRENT_TIMESTAMP, MaKG, MaKH, MaDatCho
                FROM DATCHO
                WHERE MaDatCho = ?
                """,
                maPhien,
                giaTriDb("CHK_PLV_TRANGTHAI", "dang hoat dong", 0, "Đang hoạt động"),
                maDatCho
        );

        mauJdbc.update(
                """
                INSERT INTO HOADON
                    (MaHoaDon, SoHD, TongTien, ThanhTien, NgayLapHoaDon,
                     PhuongThucThanhToan, TrangThaiThanhToan, MaPhien)
                SELECT ?, ?, NVL(ThanhTien, 0), NVL(ThanhTien, 0), CURRENT_TIMESTAMP,
                       ?, ?, ?
                FROM DATCHO
                WHERE MaDatCho = ?
                """,
                maHoaDon,
                maHoaDon,
                giaTriDb("CHK_HD_PTTT", "chuyen khoan", 0, "Chuyển khoản"),
                trangThaiHoaDonDb("Da thanh toan thanh cong"),
                maPhien,
                maDatCho
        );

        mauJdbc.update(
                """
                UPDATE DATCHO
                SET TrangThaiDatTruoc = ?, CapNhatLanCuoi = CURRENT_TIMESTAMP
                WHERE MaDatCho = ?
                """,
                trangThaiDatChoDb("Da su dung"),
                maDatCho
        );

        mauJdbc.update(
                """
                UPDATE KHONGGIAN
                SET TrangThaiKG = ?
                WHERE MaKG = (SELECT MaKG FROM DATCHO WHERE MaDatCho = ?)
                """,
                trangThaiKhongGianDb("Dang hoat dong"),
                maDatCho
        );
        return true;
    }

    public void capNhatTrangThaiDatCho(String maDatCho, String status, String suffixNote) {
        mauJdbc.update(
                "UPDATE DATCHO SET TrangThaiDatTruoc = ?, GhiChu = NVL(GhiChu, '') || ?, CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaDatCho = ?",
                trangThaiDatChoDb(status),
                suffixNote,
                maDatCho
        );
    }

    public void capNhatTrangThaiHoaDonTheoDatCho(String maDatCho, String status) {
        mauJdbc.update(
                """
                UPDATE HOADON
                SET TrangThaiThanhToan = ?, NgayLapHoaDon = CURRENT_TIMESTAMP
                WHERE MaPhien IN (SELECT MaPhien FROM PHIENLAMVIEC WHERE MaDatCho = ?)
                """,
                trangThaiHoaDonDb(status),
                maDatCho
        );
    }

    public void capNhatQrDatCho(String maDatCho, String maQR) {
        mauJdbc.update(
                "UPDATE DATCHO SET MaQR = ?, CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaDatCho = ?",
                maQR,
                maDatCho
        );
    }

    public ThongTinXacNhanDatChoDTO timThongTinXacNhanTheoDatCho(String maDatCho) {
        String sql = """
                SELECT CAST(NULL AS VARCHAR2(50)) AS MaPhien,
                       dc.MaDatCho, dc.MaQR, nd.HoTen, nd.Email,
                       kg.TenKG, cn.TenCN, dc.ThoiGianDuKienToi AS ThoiGianBatDau,
                       dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR') AS ThoiGianDuKienKetThuc,
                       dc.ThanhTien
                FROM DATCHO dc
                JOIN KHACHHANG kh ON kh.MaKH = dc.MaKH
                JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND
                JOIN KHONGGIAN kg ON kg.MaKG = dc.MaKG
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                WHERE dc.MaDatCho = ?
                """;
        try {
            return mauJdbc.queryForObject(sql, (rs, rowNum) -> mapThongTinXacNhanDatCho(rs), maDatCho);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public ThongTinXacNhanDatChoDTO timThongTinXacNhanTheoQr(String maQR) {
        String sql = """
                SELECT p.MaPhien, dc.MaDatCho, dc.MaQR, nd.HoTen, nd.Email,
                       kg.TenKG, cn.TenCN,
                       NVL(p.ThoiGianBatDau, dc.ThoiGianDuKienToi) AS ThoiGianBatDau,
                       NVL(p.ThoiGianDuKienKetThuc, dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR')) AS ThoiGianDuKienKetThuc,
                       NVL(h.ThanhTien, dc.ThanhTien) AS ThanhTien
                FROM DATCHO dc
                LEFT JOIN PHIENLAMVIEC p ON p.MaDatCho = dc.MaDatCho
                JOIN KHACHHANG kh ON kh.MaKH = dc.MaKH
                JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND
                JOIN KHONGGIAN kg ON kg.MaKG = dc.MaKG
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                LEFT JOIN HOADON h ON h.MaPhien = p.MaPhien
                WHERE dc.MaQR = ?
                """;
        try {
            return mauJdbc.queryForObject(sql, (rs, rowNum) -> mapThongTinXacNhanDatCho(rs), maQR);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public String timQrDatChoCuaHoiVien(String maKH, String maDatCho) {
        if (maKH == null || maKH.isBlank() || maDatCho == null || maDatCho.isBlank()) {
            return null;
        }
        String sql = """
                SELECT dc.MaQR
                FROM DATCHO dc
                WHERE dc.MaKH = ? AND dc.MaDatCho = ?
                """;
        try {
            return mauJdbc.queryForObject(sql, String.class, maKH, maDatCho);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public List<PhienHoiVienView> timPhienCuaHoiVien(String maKH) {
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
        return mauJdbc.query(sql, (rs, rowNum) -> anhXaPhienHoiVien(rs), maKH);
    }

    public List<LichSuDatChoView> timLichSuDatChoCuaHoiVien(String maKH) {
        if (maKH == null || maKH.isBlank()) {
            return List.of();
        }
        String sql = """
                SELECT p.MaPhien, dc.MaDatCho, dc.MaQR, kg.TenKG, cn.TenCN,
                       NVL(p.ThoiGianBatDau, dc.ThoiGianDuKienToi) AS ThoiGianBatDau,
                       NVL(p.ThoiGianDuKienKetThuc, dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR')) AS ThoiGianDuKienKetThuc,
                       p.ThoiGianKetThuc,
                       NVL(p.TrangThaiPhien, dc.TrangThaiDatTruoc) AS TrangThaiPhien,
                       h.MaHoaDon,
                       NVL(h.TongTien, dc.ThanhTien) AS TongTien,
                       NVL(h.ThanhTien, dc.ThanhTien) AS ThanhTien,
                       NVL(h.TrangThaiThanhToan, dc.TrangThaiDatTruoc) AS TrangThaiThanhToan,
                       h.NgayLapHoaDon
                FROM DATCHO dc
                JOIN KHONGGIAN kg ON kg.MaKG = dc.MaKG
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                LEFT JOIN PHIENLAMVIEC p ON p.MaDatCho = dc.MaDatCho
                LEFT JOIN HOADON h ON h.MaPhien = p.MaPhien
                WHERE dc.MaKH = ?
                ORDER BY dc.ThoiGianDat DESC
                """;
        return mauJdbc.query(sql, (rs, rowNum) -> {
            Timestamp start = rs.getTimestamp("ThoiGianBatDau");
            Timestamp expectedEnd = rs.getTimestamp("ThoiGianDuKienKetThuc");
            Timestamp actualEnd = rs.getTimestamp("ThoiGianKetThuc");
            Timestamp invoiceDate = rs.getTimestamp("NgayLapHoaDon");
            String maPhien = rs.getString("MaPhien");
            return new LichSuDatChoView(
                    maPhien,
                    rs.getString("MaDatCho"),
                    rs.getString("MaHoaDon"),
                    rs.getString("MaQR"),
                    rs.getString("TenKG"),
                    rs.getString("TenCN"),
                    start == null ? null : start.toLocalDateTime(),
                    expectedEnd == null ? null : expectedEnd.toLocalDateTime(),
                    actualEnd == null ? null : actualEnd.toLocalDateTime(),
                    maPhien == null
                            ? hienThiTrangThai(rs.getString("TrangThaiPhien"))
                            : hienThiTrangThaiPhien(rs.getString("TrangThaiPhien")),
                    hienThiTrangThaiHoaDon(rs.getString("TrangThaiThanhToan")),
                    rs.getBigDecimal("TongTien"),
                    rs.getBigDecimal("ThanhTien"),
                    invoiceDate == null ? null : invoiceDate.toLocalDateTime(),
                    maPhien == null ? List.of() : timDichVuTrongPhienHoiVien(maKH, maPhien)
            );
        }, maKH);
    }

    public PhienHoiVienView timPhienHoiVien(String maKH, String maPhien) {
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
            return mauJdbc.queryForObject(sql, (rs, rowNum) -> anhXaPhienHoiVien(rs), maKH, maPhien);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public List<TuyChonDichVuView> timLuaChonDichVu() {
        String sql = """
                SELECT dv.MaDV, dv.TenDV, ldv.TenLoaiDV, NVL(dv.DonGia, 0) AS DonGia, dv.SoLuong
                FROM DICHVU dv
                LEFT JOIN LOAIDICHVU ldv ON ldv.MaLoaiDV = dv.MaLoaiDV
                ORDER BY ldv.TenLoaiDV, dv.TenDV
                """;
        return mauJdbc.query(sql, (rs, rowNum) -> new TuyChonDichVuView(
                rs.getString("MaDV"),
                giaiMaLoiFont(rs.getString("TenDV")),
                giaiMaLoiFont(rs.getString("TenLoaiDV")),
                rs.getBigDecimal("DonGia"),
                rs.getObject("SoLuong") == null ? null : rs.getInt("SoLuong")
        ));
    }

    public TuyChonDichVuView timLuaChonDichVuTheoMa(String maDV) {
        String sql = """
                SELECT dv.MaDV, dv.TenDV, ldv.TenLoaiDV, NVL(dv.DonGia, 0) AS DonGia, dv.SoLuong
                FROM DICHVU dv
                LEFT JOIN LOAIDICHVU ldv ON ldv.MaLoaiDV = dv.MaLoaiDV
                WHERE dv.MaDV = ?
                """;
        try {
            return mauJdbc.queryForObject(sql, (rs, rowNum) -> new TuyChonDichVuView(
                    rs.getString("MaDV"),
                    giaiMaLoiFont(rs.getString("TenDV")),
                    giaiMaLoiFont(rs.getString("TenLoaiDV")),
                    rs.getBigDecimal("DonGia"),
                    rs.getObject("SoLuong") == null ? null : rs.getInt("SoLuong")
            ), maDV);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public List<DichVuTrongPhienView> timDichVuTrongPhienHoiVien(String maKH, String maPhien) {
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
        return mauJdbc.query(sql, (rs, rowNum) -> new DichVuTrongPhienView(
                rs.getString("MaDV"),
                giaiMaLoiFont(rs.getString("TenDV")),
                giaiMaLoiFont(rs.getString("TenLoaiDV")),
                rs.getInt("SoLuong"),
                rs.getBigDecimal("DonGia"),
                rs.getBigDecimal("ThanhTien"),
                rs.getString("GhiChu")
        ), maKH, maPhien);
    }

    public void themDichVuDaThanhToanVaoPhien(String maPhien, TuyChonDichVuView service,
                                        int quantity, String note, BigDecimal totalAmount) {
        mauJdbc.update(
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

        if (laDichVuGiaHan(service)) {
            mauJdbc.update(
                    "UPDATE PHIENLAMVIEC SET ThoiGianDuKienKetThuc = ThoiGianDuKienKetThuc + NUMTODSINTERVAL(?, 'HOUR'), CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaPhien = ?",
                    quantity,
                    maPhien
            );
        }

        mauJdbc.update(
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
                giaTriDb("CHK_HD_PTTT", "chuyen khoan", 0, "Chuyển khoản"),
                trangThaiHoaDonDb("Da thanh toan thanh cong"),
                maPhien
        );
    }

    private PhienHoiVienView anhXaPhienHoiVien(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp start = rs.getTimestamp("ThoiGianBatDau");
        Timestamp expectedEnd = rs.getTimestamp("ThoiGianDuKienKetThuc");
        Timestamp actualEnd = rs.getTimestamp("ThoiGianKetThuc");
        return new PhienHoiVienView(
                rs.getString("MaPhien"),
                rs.getString("MaKG"),
                rs.getString("TenKG"),
                rs.getString("TenCN"),
                start == null ? null : start.toLocalDateTime(),
                expectedEnd == null ? null : expectedEnd.toLocalDateTime(),
                actualEnd == null ? null : actualEnd.toLocalDateTime(),
                hienThiTrangThaiPhien(rs.getString("TrangThaiPhien")),
                rs.getString("ThoiGianDongCua")
        );
    }

    private DatChoView anhXaDatCho(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp arrivalTime = rs.getTimestamp("ThoiGianDuKienToi");
        int durationHours = rs.getInt("KhoangThoiGianSuDung");
        boolean durationNull = rs.wasNull();
        return new DatChoView(
                rs.getString("MaDatCho"),
                rs.getString("HoTen"),
                rs.getString("TenKG"),
                rs.getString("TenCN"),
                arrivalTime == null ? null : arrivalTime.toLocalDateTime(),
                durationNull ? null : durationHours,
                hienThiTrangThai(rs.getString("TrangThaiDatTruoc")),
                rs.getBigDecimal("ThanhTien"),
                rs.getString("GhiChu")
        );
    }

    private ThongTinXacNhanDatChoDTO mapThongTinXacNhanDatCho(java.sql.ResultSet rs) throws java.sql.SQLException {
        ThongTinXacNhanDatChoDTO dto = new ThongTinXacNhanDatChoDTO();
        dto.setMaPhien(rs.getString("MaPhien"));
        dto.setMaDatCho(rs.getString("MaDatCho"));
        dto.setMaQR(rs.getString("MaQR"));
        dto.setHoTen(rs.getString("HoTen"));
        dto.setEmail(rs.getString("Email"));
        dto.setTenKhongGian(rs.getString("TenKG"));
        dto.setTenChiNhanh(rs.getString("TenCN"));
        dto.setThoiGianBatDau(rs.getTimestamp("ThoiGianBatDau"));
        dto.setThoiGianDuKienKetThuc(rs.getTimestamp("ThoiGianDuKienKetThuc"));
        dto.setThanhTien(rs.getBigDecimal("ThanhTien"));
        return dto;
    }

    private PhieuGiamGiaView anhXaPhieuGiamGia(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp endTime = rs.getTimestamp("NgayKetThucApDung");
        int danhDauDaSuDung = rs.getInt("SLDaDung");
        Integer usedValue = rs.wasNull() ? null : danhDauDaSuDung;
        int max = rs.getInt("SLToiDa");
        Integer maxValue = rs.wasNull() ? null : max;
        return new PhieuGiamGiaView(
                rs.getString("MaPGG"),
                rs.getString("MaChuSoPGG"),
                rs.getBigDecimal("GiaTriGiamGia"),
                rs.getBigDecimal("GiaTriApDungToiThieu"),
                endTime == null ? null : endTime.toLocalDateTime(),
                usedValue,
                maxValue
        );
    }

    private String giaTriDb(String constraintName, String normalizedNeedle, int fallbackIndex, String fallbackValue) {
        List<String> values = layGiaTriRangBuoc(constraintName);
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

    private String trangThaiDatChoDb(String status) {
        String normalized = chuanHoa(status);
        if (normalized.contains("khong thanh cong")) {
            return giaTriDb("CHK_DC_TRANGTHAI", "khong thanh cong", 2, "Thanh toán không thành công");
        }
        if (normalized.contains("thanh cong")) {
            return giaTriDb("CHK_DC_TRANGTHAI", "thanh cong", 1, "Đã thanh toán thành công");
        }
        if (normalized.contains("su dung")) {
            return giaTriDb("CHK_DC_TRANGTHAI", "su dung", 3, "Đã sử dụng");
        }
        return giaTriDb("CHK_DC_TRANGTHAI", "cho thanh toan", 0, "Đang chờ thanh toán");
    }

    private String trangThaiHoaDonDb(String status) {
        String normalized = chuanHoa(status);
        if (normalized.contains("khong thanh cong")) {
            return giaTriDb("CHK_HD_TRANGTHAI", "khong thanh cong", 2, "Thanh toán không thành công");
        }
        if (normalized.contains("thanh cong")) {
            return giaTriDb("CHK_HD_TRANGTHAI", "thanh cong", 1, "Đã thanh toán thành công");
        }
        return giaTriDb("CHK_HD_TRANGTHAI", "cho thanh toan", 0, "Đang chờ thanh toán");
    }

    private String trangThaiKhongGianDb(String status) {
        String normalized = chuanHoa(status);
        if (normalized.contains("tam khoa")) {
            return giaTriDb("CHK_KG_TRANGTHAI", "tam khoa", 1, "Tạm khoá");
        }
        if (normalized.contains("dat truoc")) {
            return giaTriDb("CHK_KG_TRANGTHAI", "dat truoc", 1, "Đã đặt trước");
        }
        if (normalized.contains("dang hoat dong")) {
            return giaTriDb("CHK_KG_TRANGTHAI", "dang hoat dong", 2, "Đang hoạt động");
        }
        if (normalized.contains("bao tri")) {
            return giaTriDb("CHK_KG_TRANGTHAI", "bao tri", 5, "Bảo trì");
        }
        return giaTriDb("CHK_KG_TRANGTHAI", "trong", 0, "Trống");
    }

    private List<String> layGiaTriRangBuoc(String constraintName) {
        List<String> values = new ArrayList<>();
        try {
            String condition = mauJdbc.queryForObject(
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

    private String chuanHoa(String value) {
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

    private String rongThanhNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String transferContent(String maDatCho) {
        return "WMS " + (maDatCho == null ? "" : maDatCho.trim());
    }

    private String hienThiTrangThai(String value) {
        if (value == null || value.isBlank()) {
            return "Chưa có trạng thái";
        }

        String decoded = giaiMaLoiFont(value);
        String normalized = chuanHoa(decoded);
        if (normalized.contains("cho thanh toan")) {
            return "Đang chờ thanh toán";
        }
        if (normalized.contains("thanh toan khong thanh cong")) {
            return "Thanh toán không thành công";
        }
        if (normalized.contains("thanh toan thanh cong")) {
            return "Đã thanh toán thành công";
        }
        if (normalized.contains("da su dung")) {
            return "Đã sử dụng";
        }
        return decoded;
    }

    private String hienThiTrangThaiPhien(String value) {
        if (value == null || value.isBlank()) {
            return "Chua co trang thai";
        }
        String decoded = giaiMaLoiFont(value);
        String normalized = chuanHoa(decoded);
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

    private String hienThiTrangThaiHoaDon(String value) {
        if (value == null || value.isBlank()) {
            return "Chưa có hóa đơn";
        }
        String decoded = giaiMaLoiFont(value);
        String normalized = chuanHoa(decoded);
        if (normalized.contains("cho thanh toan")) {
            return "Đang chờ thanh toán";
        }
        if (normalized.contains("thanh toan khong thanh cong")) {
            return "Thanh toán không thành công";
        }
        if (normalized.contains("thanh toan thanh cong")) {
            return "Đã thanh toán thành công";
        }
        return decoded;
    }

    private boolean laDichVuGiaHan(TuyChonDichVuView service) {
        if (service == null) {
            return false;
        }
        return "DV000".equalsIgnoreCase(service.getMaDV()) || chuanHoa(service.getTenDV()).contains("gia han gio");
    }

    private String giaiMaLoiFont(String value) {
        try {
            String decoded = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            return decoded.indexOf('\uFFFD') >= 0 ? value : decoded;
        } catch (RuntimeException ex) {
            return value;
        }
    }

    public record BanGhiXacThuc(
            String maND,
            String hoTen,
            String tenTaiKhoan,
            String matKhauMaHoa,
            String trangThaiND,
            String maKH,
            String maNV
    ) {
        public boolean laNhanVien() {
            return maNV != null && !maNV.isBlank();
        }
    }

    private record DongPhienCho(
            String maDatCho,
            String maKG,
            String maKH,
            Timestamp arrivalTime,
            int durationHours,
            BigDecimal totalAmount
    ) {
    }
}
