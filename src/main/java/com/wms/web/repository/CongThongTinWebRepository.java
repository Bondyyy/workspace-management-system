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
import com.wms.web.model.ThongTinNhanChoBangQR;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.ThongTinXacNhanDatChoDTO;
import com.wms.util.ChuyenKhoanQrUtil;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class CongThongTinWebRepository {

    private static final ZoneId MUI_GIO_VIET_NAM = ZoneId.of("Asia/Ho_Chi_Minh");

    private final JdbcTemplate mauJdbc;

    public CongThongTinWebRepository(JdbcTemplate mauJdbc) {
        this.mauJdbc = mauJdbc;
    }

    public BanGhiXacThuc timThongTinXacThuc(String identifier) {
        String sql = """
                SELECT n.MaND, n.HoTen, n.TenTaiKhoan, n.Email, n.MatKhauMaHoa, n.TrangThaiND,
                       kh.MaKH, nv.MaNV, nv.MaCN, cn.TenCN
                FROM NGUOIDUNG n
                LEFT JOIN KHACHHANG kh ON kh.MaND = n.MaND
                LEFT JOIN NHANVIEN nv ON nv.MaND = n.MaND
                LEFT JOIN CHINHANH cn ON cn.MaCN = nv.MaCN
                WHERE n.TenTaiKhoan = ? OR n.Email = ? OR n.SDT = ?
                """;
        try {
            return mauJdbc.queryForObject(sql, (rs, rowNum) -> new BanGhiXacThuc(
                    rs.getString("MaND"),
                    rs.getString("HoTen"),
                    rs.getString("TenTaiKhoan"),
                    rs.getString("Email"),
                    rs.getString("MatKhauMaHoa"),
                    rs.getString("TrangThaiND"),
                    rs.getString("MaKH"),
                    rs.getString("MaNV"),
                    rs.getString("MaCN"),
                    rs.getString("TenCN")
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
        String maND = mauJdbc.execute((ConnectionCallback<String>) conn -> {
            String sql = """
                    BEGIN
                        INSERT INTO NGUOIDUNG (
                            HoTen, TenTaiKhoan, MatKhauMaHoa, Email,
                            TrangThaiND, ThoiGianTao, CapNhatLanCuoi
                        ) VALUES (
                            ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                        )
                        RETURNING MaND INTO ?;
                    END;
                    """;
            try (var cs = conn.prepareCall(sql)) {
                cs.setString(1, fullName);
                cs.setString(2, username);
                cs.setString(3, hashedPassword);
                cs.setString(4, email);
                cs.setString(5, giaTriDb("CHK_ND_TRANGTHAI", "dang hoat dong", 0, "Đang hoạt động"));
                cs.registerOutParameter(6, Types.VARCHAR);
                cs.execute();
                return cs.getString(6);
            }
        });

        mauJdbc.update(
                "INSERT INTO KHACHHANG (MaHangThanhVien, TongChiTieu, CapNhatLanCuoi, MaND) VALUES ('HTV01', 0, CURRENT_TIMESTAMP, ?)",
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
                       CASE WHEN n.AnhDaiDien IS NULL THEN 0 ELSE 1 END AS CoAnhDaiDien,
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
                        rs.getString("TenHangThanhVien"),
                        rs.getInt("CoAnhDaiDien") == 1
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

    public void capNhatAnhDaiDien(String maND, byte[] bytes) {
        mauJdbc.update(
                """
                UPDATE NGUOIDUNG
                SET AnhDaiDien = ?,
                    CapNhatLanCuoi = CURRENT_TIMESTAMP
                WHERE MaND = ?
                """,
                bytes,
                maND
        );
    }

    public byte[] layAnhDaiDien(String maND) {
        if (maND == null || maND.isBlank()) {
            return null;
        }
        try {
            return mauJdbc.queryForObject(
                    "SELECT AnhDaiDien FROM NGUOIDUNG WHERE MaND = ?",
                    byte[].class,
                    maND
            );
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public String layMatKhauMaHoaTheoMaND(String maND) {
        if (maND == null || maND.isBlank()) {
            return null;
        }
        try {
            return mauJdbc.queryForObject(
                    "SELECT MatKhauMaHoa FROM NGUOIDUNG WHERE MaND = ?",
                    String.class,
                    maND
            );
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public void capNhatMatKhau(String maND, String matKhauMaHoaMoi) {
        mauJdbc.update(
                """
                UPDATE NGUOIDUNG
                SET MatKhauMaHoa = ?,
                    CapNhatLanCuoi = CURRENT_TIMESTAMP
                WHERE MaND = ?
                """,
                matKhauMaHoaMoi,
                maND
        );
    }

    public List<ChiNhanhView> timChiNhanhHoatDong() {
        String sql = """
                SELECT MaCN, TenCN, DiaChi, ThoiGianMoCua, ThoiGianDongCua, DuongDayNong
                FROM CHINHANH
                WHERE TrangThai = ?
                ORDER BY
                    CASE
                        WHEN REGEXP_LIKE(MaCN, '^CN[0-9]+$')
                        THEN TO_NUMBER(REGEXP_SUBSTR(MaCN, '[0-9]+$'))
                    END NULLS LAST,
                    MaCN
                """;
        return mauJdbc.query(sql, (rs, rowNum) -> new ChiNhanhView(
                rs.getString("MaCN"),
                rs.getString("TenCN"),
                rs.getString("DiaChi"),
                    rs.getString("ThoiGianMoCua"),
                    rs.getString("ThoiGianDongCua"),
                    rs.getString("DuongDayNong")
        ), giaTriDb("CHK_CN_TRANGTHAI", "dang hoat dong", 0, "Đang hoạt động"));
    }

    public List<KhongGianView> timKhongGian(String branchId) {
        String baseSql = """
                SELECT kg.MaKG, kg.TenKG, kg.ViTri, kg.TrangThaiKG,
                       kg.MaCN, cn.TenCN, cn.ThoiGianMoCua, cn.ThoiGianDongCua,
                       lkg.MaLoaiKG, lkg.TenLoaiKG, lkg.SucChua,
                       NVL(lkg.DonGiaTheoGio, 0) AS DonGiaTheoGio,
                       NVL(kg.ToaDoX, 0) AS ToaDoX, NVL(kg.ToaDoY, 0) AS ToaDoY,
                       NVL(kg.ChieuDai, 1) AS ChieuDai, NVL(kg.ChieuRong, 1) AS ChieuRong
                FROM KHONGGIAN kg
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                LEFT JOIN LOAIKHONGGIAN lkg ON lkg.MaLoaiKG = kg.MaLoaiKG
                """;
        String sql = baseSql + """
                WHERE kg.MaCN = ?
                ORDER BY
                    CASE
                        WHEN REGEXP_LIKE(kg.MaCN, '^CN[0-9]+$')
                        THEN TO_NUMBER(REGEXP_SUBSTR(kg.MaCN, '[0-9]+$'))
                    END NULLS LAST,
                    kg.MaCN,
                    kg.ViTri NULLS LAST,
                    kg.TenKG,
                    kg.MaKG
                """;
        Object[] params = { branchId };
        if (branchId == null || branchId.isBlank()) {
            sql = baseSql + """
                    ORDER BY
                        CASE
                            WHEN REGEXP_LIKE(kg.MaCN, '^CN[0-9]+$')
                            THEN TO_NUMBER(REGEXP_SUBSTR(kg.MaCN, '[0-9]+$'))
                        END NULLS LAST,
                        kg.MaCN,
                        kg.ViTri NULLS LAST,
                        kg.TenKG,
                        kg.MaKG
                    """;
            params = new Object[0];
        }
        return mauJdbc.query(sql, (rs, rowNum) -> {
            int sucChua = rs.getInt("SucChua");
            boolean sucChuaNull = rs.wasNull();
            return new KhongGianView(
                    rs.getString("MaKG"),
                    rs.getString("TenKG"),
                    rs.getString("TenLoaiKG"),
                    rs.getString("ViTri"),
                    rs.getString("TrangThaiKG"),
                    rs.getString("MaCN"),
                    rs.getString("TenCN"),
                    rs.getString("ThoiGianMoCua"),
                    rs.getString("ThoiGianDongCua"),
                    sucChuaNull ? null : sucChua,
                    rs.getBigDecimal("DonGiaTheoGio"),
                    rs.getInt("ToaDoX"),
                    rs.getInt("ToaDoY"),
                    rs.getInt("ChieuDai"),
                    rs.getInt("ChieuRong"),
                    true,
                    null
            );
        }, params);
    }

    public List<KhongGianView> timKhongGian(String branchId, LocalDateTime selectedStart, LocalDateTime selectedEnd) {
        if (selectedStart == null || selectedEnd == null || !selectedEnd.isAfter(selectedStart)) {
            return timKhongGian(branchId);
        }

        String baseSql = """
                SELECT kg.MaKG, kg.TenKG, kg.ViTri,
                       CASE
                           WHEN kg.TrangThaiKG = ? THEN ?
                           WHEN kg.TrangThaiKG = ?
                                OR EXISTS (
                                    SELECT 1
                                    FROM PHIENLAMVIEC p
                                    WHERE p.MaKG = kg.MaKG
                                      AND p.TrangThaiPhien = ?
                                      AND p.ThoiGianBatDau < ?
                                      AND p.ThoiGianDuKienKetThuc > ?
                                      AND (p.ThoiGianKetThuc IS NULL OR p.ThoiGianKetThuc > ?)
                                ) THEN ?
                           WHEN EXISTS (
                               SELECT 1
                               FROM DATCHO dc
                               WHERE dc.MaKG = kg.MaKG
                                 AND dc.TrangThaiDatTruoc = ?
                                 AND dc.ThoiGianDat >= CAST(CURRENT_TIMESTAMP AS TIMESTAMP) - INTERVAL '10' MINUTE
                                 AND dc.ThoiGianDuKienToi < ?
                                 AND (dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR')) > ?
                           ) THEN ?
                           WHEN EXISTS (
                               SELECT 1
                               FROM DATCHO dc
                               WHERE dc.MaKG = kg.MaKG
                                 AND dc.TrangThaiDatTruoc IN (?, ?)
                                 AND dc.ThoiGianDuKienToi < ?
                                 AND (dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR')) > ?
                           ) THEN ?
                           ELSE ?
                       END AS TrangThaiKG,
                       kg.MaCN, cn.TenCN, cn.ThoiGianMoCua, cn.ThoiGianDongCua,
                       lkg.MaLoaiKG, lkg.TenLoaiKG, lkg.SucChua,
                       NVL(lkg.DonGiaTheoGio, 0) AS DonGiaTheoGio,
                       NVL(kg.ToaDoX, 0) AS ToaDoX, NVL(kg.ToaDoY, 0) AS ToaDoY,
                       NVL(kg.ChieuDai, 1) AS ChieuDai, NVL(kg.ChieuRong, 1) AS ChieuRong,
                       (SELECT MAX(p.ThoiGianDuKienKetThuc)
                        FROM PHIENLAMVIEC p
                        WHERE p.MaKG = kg.MaKG
                          AND p.TrangThaiPhien = ?
                          AND p.ThoiGianBatDau < ?
                          AND p.ThoiGianDuKienKetThuc > ?
                          AND (p.ThoiGianKetThuc IS NULL OR p.ThoiGianKetThuc > ?)) AS PhienBusyUntil,
                       (SELECT MAX(dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR'))
                        FROM DATCHO dc
                        WHERE dc.MaKG = kg.MaKG
                          AND dc.ThoiGianDuKienToi < ?
                          AND (dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR')) > ?
                          AND (
                              dc.TrangThaiDatTruoc IN (?, ?)
                              OR (
                                  dc.TrangThaiDatTruoc = ?
                                  AND dc.ThoiGianDat >= CAST(CURRENT_TIMESTAMP AS TIMESTAMP) - INTERVAL '10' MINUTE
                              )
                          )) AS DatChoBusyUntil
                FROM KHONGGIAN kg
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                LEFT JOIN LOAIKHONGGIAN lkg ON lkg.MaLoaiKG = kg.MaLoaiKG
                """;
        String sql = baseSql + """
                WHERE kg.MaCN = ?
                ORDER BY
                    CASE
                        WHEN REGEXP_LIKE(kg.MaCN, '^CN[0-9]+$')
                        THEN TO_NUMBER(REGEXP_SUBSTR(kg.MaCN, '[0-9]+$'))
                    END NULLS LAST,
                    kg.MaCN,
                    kg.ViTri NULLS LAST,
                    kg.TenKG,
                    kg.MaKG
                """;
        Object[] params = {
                trangThaiKhongGianDb("Bao tri"),
                "Bảo trì",
                trangThaiKhongGianDb("Dang hoat dong"),
                giaTriDb("CHK_PLV_TRANGTHAI", "dang hoat dong", 0, "Đang hoạt động"),
                Timestamp.valueOf(selectedEnd),
                Timestamp.valueOf(selectedStart),
                Timestamp.valueOf(selectedStart),
                "Đang hoạt động",
                trangThaiDatChoDb("Dang cho thanh toan"),
                Timestamp.valueOf(selectedEnd),
                Timestamp.valueOf(selectedStart),
                "Tạm khóa",
                trangThaiDatChoDb("Da thanh toan thanh cong"),
                "Đã đặt trước",
                Timestamp.valueOf(selectedEnd),
                Timestamp.valueOf(selectedStart),
                "Đã đặt trước",
                "Trống",
                giaTriDb("CHK_PLV_TRANGTHAI", "dang hoat dong", 0, "Đang hoạt động"),
                Timestamp.valueOf(selectedEnd),
                Timestamp.valueOf(selectedStart),
                Timestamp.valueOf(selectedStart),
                Timestamp.valueOf(selectedEnd),
                Timestamp.valueOf(selectedStart),
                trangThaiDatChoDb("Da thanh toan thanh cong"),
                "Đã đặt trước",
                trangThaiDatChoDb("Dang cho thanh toan"),
                branchId
        };
        if (branchId == null || branchId.isBlank()) {
            sql = baseSql + """
                    ORDER BY
                        CASE
                            WHEN REGEXP_LIKE(kg.MaCN, '^CN[0-9]+$')
                            THEN TO_NUMBER(REGEXP_SUBSTR(kg.MaCN, '[0-9]+$'))
                        END NULLS LAST,
                        kg.MaCN,
                        kg.ViTri NULLS LAST,
                        kg.TenKG,
                        kg.MaKG
                    """;
            params = new Object[] {
                    trangThaiKhongGianDb("Bao tri"),
                    "Bảo trì",
                    trangThaiKhongGianDb("Dang hoat dong"),
                    giaTriDb("CHK_PLV_TRANGTHAI", "dang hoat dong", 0, "Đang hoạt động"),
                    Timestamp.valueOf(selectedEnd),
                    Timestamp.valueOf(selectedStart),
                    Timestamp.valueOf(selectedStart),
                    "Đang hoạt động",
                    trangThaiDatChoDb("Dang cho thanh toan"),
                    Timestamp.valueOf(selectedEnd),
                    Timestamp.valueOf(selectedStart),
                    "Tạm khóa",
                    trangThaiDatChoDb("Da thanh toan thanh cong"),
                    "Đã đặt trước",
                    Timestamp.valueOf(selectedEnd),
                    Timestamp.valueOf(selectedStart),
                    "Đã đặt trước",
                    "Trống",
                    giaTriDb("CHK_PLV_TRANGTHAI", "dang hoat dong", 0, "Đang hoạt động"),
                    Timestamp.valueOf(selectedEnd),
                    Timestamp.valueOf(selectedStart),
                    Timestamp.valueOf(selectedStart),
                    Timestamp.valueOf(selectedEnd),
                    Timestamp.valueOf(selectedStart),
                    trangThaiDatChoDb("Da thanh toan thanh cong"),
                    "Đã đặt trước",
                    trangThaiDatChoDb("Dang cho thanh toan")
            };
        }

        return mauJdbc.query(sql, (rs, rowNum) -> {
            Timestamp busyUntil = thoiGianBanRonLonHon(rs.getTimestamp("PhienBusyUntil"), rs.getTimestamp("DatChoBusyUntil"));
            int sucChua = rs.getInt("SucChua");
            boolean sucChuaNull = rs.wasNull();
            return new KhongGianView(
                    rs.getString("MaKG"),
                    rs.getString("TenKG"),
                    rs.getString("TenLoaiKG"),
                    rs.getString("ViTri"),
                    rs.getString("TrangThaiKG"),
                    rs.getString("MaCN"),
                    rs.getString("TenCN"),
                    rs.getString("ThoiGianMoCua"),
                    rs.getString("ThoiGianDongCua"),
                    sucChuaNull ? null : sucChua,
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
                       kg.MaCN, cn.TenCN, cn.ThoiGianMoCua, cn.ThoiGianDongCua,
                       lkg.MaLoaiKG, lkg.TenLoaiKG, lkg.SucChua,
                       NVL(lkg.DonGiaTheoGio, 0) AS DonGiaTheoGio,
                       NVL(kg.ToaDoX, 0) AS ToaDoX, NVL(kg.ToaDoY, 0) AS ToaDoY,
                       NVL(kg.ChieuDai, 1) AS ChieuDai, NVL(kg.ChieuRong, 1) AS ChieuRong
                FROM KHONGGIAN kg
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                LEFT JOIN LOAIKHONGGIAN lkg ON lkg.MaLoaiKG = kg.MaLoaiKG
                WHERE kg.MaKG = ?
                """;
        try {
            return mauJdbc.queryForObject(sql, (rs, rowNum) -> {
                int sucChua = rs.getInt("SucChua");
                boolean sucChuaNull = rs.wasNull();
                return new KhongGianView(
                        rs.getString("MaKG"),
                        rs.getString("TenKG"),
                        rs.getString("TenLoaiKG"),
                        rs.getString("ViTri"),
                        rs.getString("TrangThaiKG"),
                        rs.getString("MaCN"),
                        rs.getString("TenCN"),
                        rs.getString("ThoiGianMoCua"),
                        rs.getString("ThoiGianDongCua"),
                        sucChuaNull ? null : sucChua,
                        rs.getBigDecimal("DonGiaTheoGio"),
                        rs.getInt("ToaDoX"),
                        rs.getInt("ToaDoY"),
                        rs.getInt("ChieuDai"),
                        rs.getInt("ChieuRong"),
                        true,
                        null
                );
            }, maKG);
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
                       AND TrangThaiPhien = ?
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
                           TrangThaiDatTruoc IN (?, ?)
                           OR (
                               TrangThaiDatTruoc = ?
                               AND ThoiGianDat >= CAST(CURRENT_TIMESTAMP AS TIMESTAMP) - INTERVAL '10' MINUTE
                           )
                       ))
                FROM DUAL
                """,
                Integer.class,
                maKG,
                giaTriDb("CHK_PLV_TRANGTHAI", "dang hoat dong", 0, "Đang hoạt động"),
                Timestamp.valueOf(selectedEnd),
                Timestamp.valueOf(selectedStart),
                Timestamp.valueOf(selectedStart),
                maKG,
                Timestamp.valueOf(selectedEnd),
                Timestamp.valueOf(selectedStart),
                trangThaiDatChoDb("Da thanh toan thanh cong"),
                "Đã đặt trước",
                trangThaiDatChoDb("Dang cho thanh toan")
        );
        return count != null && count > 0;
    }

    public List<PhieuGiamGiaView> timPhieuGiamGiaHieuLuc() {
        String sql = """
                SELECT MaPGG, MaChuSoPGG, GiaTriGiamGia, GiaTriApDungToiThieu,
                       NgayKetThucApDung, SLDaDung, SLToiDa
                FROM PHIEUGIAMGIA
                WHERE CAST(CURRENT_TIMESTAMP AS TIMESTAMP) BETWEEN NgayBatDauApDung AND NgayKetThucApDung
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
                  AND CAST(CURRENT_TIMESTAMP AS TIMESTAMP) BETWEEN NgayBatDauApDung AND NgayKetThucApDung
                  AND NVL(SLDaDung, 0) < NVL(SLToiDa, 0)
                """;
        try {
            List<PhieuGiamGiaView> matches = mauJdbc.query(sql, (rs, rowNum) -> anhXaPhieuGiamGia(rs), voucherCode.trim());
            return matches.stream().findFirst().orElse(null);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    public String taoMaDatChoTiepTheo() {
        return "";
    }

    public String taoMaPhienTiepTheo() {
        return "";
    }

    public String taoMaHoaDonTiepTheo() {
        return "";
    }

    public Optional<ThongTinNhanChoBangQR> timDatChoTheoMaQRDeNhanCho(String noiDungQR) {
        if (noiDungQR == null || noiDungQR.isBlank()) {
            return Optional.empty();
        }
        String sql = """
                SELECT dc.MaDatCho,
                       dc.MaQR,
                       dc.TrangThaiDatTruoc,
                       dc.ThoiGianDuKienToi,
                       dc.KhoangThoiGianSuDung,
                       dc.MaKH,
                       dc.MaKG,
                       kg.TenKG,
                       kg.MaCN,
                       cn.TenCN,
                       nd.HoTen AS TenKhachHang
                FROM DATCHO dc
                JOIN KHONGGIAN kg ON dc.MaKG = kg.MaKG
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                LEFT JOIN KHACHHANG kh ON dc.MaKH = kh.MaKH
                LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND
                WHERE dc.MaQR = ?
                FOR UPDATE OF dc.MaDatCho
                """;
        List<ThongTinNhanChoBangQR> ketQua = mauJdbc.query(
                sql,
                (rs, rowNum) -> anhXaThongTinNhanChoBangQR(rs),
                noiDungQR.trim()
        );
        return ketQua.stream().findFirst();
    }

    public LocalDateTime layThoiGianHeThong() {
        Timestamp thoiGian = mauJdbc.queryForObject(
                "SELECT CAST(CURRENT_TIMESTAMP AS TIMESTAMP) FROM DUAL",
                Timestamp.class
        );
        return thoiGian == null ? layThoiGianHienTaiVietNam() : thoiGian.toLocalDateTime();
    }

    public boolean daCoPhienTheoDatCho(String maDatCho) {
        if (maDatCho == null || maDatCho.isBlank()) {
            return false;
        }
        Integer soLuong = mauJdbc.queryForObject(
                "SELECT COUNT(*) FROM PHIENLAMVIEC WHERE MaDatCho = ?",
                Integer.class,
                maDatCho
        );
        return soLuong != null && soLuong > 0;
    }

    public String timMaPhienTheoDatCho(String maDatCho) {
        if (maDatCho == null || maDatCho.isBlank()) {
            return null;
        }
        try {
            return mauJdbc.queryForObject(
                    """
                    SELECT MaPhien
                    FROM PHIENLAMVIEC
                    WHERE MaDatCho = ?
                    ORDER BY
                        CASE WHEN TrangThaiPhien = ? THEN 0 ELSE 1 END,
                        ThoiGianBatDau DESC
                    FETCH FIRST 1 ROW ONLY
                    """,
                    String.class,
                    maDatCho,
                    giaTriDb("CHK_PLV_TRANGTHAI", "dang hoat dong", 0, "Đang hoạt động")
            );
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public String moPhienTuDatCho(ThongTinNhanChoBangQR thongTin) {
        if (thongTin == null) {
            throw new IllegalArgumentException("Thiếu thông tin đặt chỗ để mở phiên.");
        }
        String trangThaiKhongGian = mauJdbc.queryForObject(
                "SELECT TrangThaiKG FROM KHONGGIAN WHERE MaKG = ? FOR UPDATE",
                String.class,
                thongTin.getMaKG()
        );
        String normalizedSpaceStatus = chuanHoa(trangThaiKhongGian);
        if (normalizedSpaceStatus.contains("bao tri")) {
            throw new IllegalStateException("Không gian đang bảo trì, không thể mở phiên.");
        }
        if (!normalizedSpaceStatus.equals("trong")
                && !normalizedSpaceStatus.contains("dat truoc")
                && !normalizedSpaceStatus.contains("tam khoa")) {
            throw new IllegalStateException("Không gian của đặt chỗ chưa sẵn sàng để nhận khách. Trạng thái hiện tại: "
                    + trangThaiKhongGian + ".");
        }

        java.time.LocalDateTime thoiGianDuKienKetThuc = thongTin.getThoiGianDuKienToi() != null
                ? thongTin.getThoiGianDuKienToi().plusHours(thongTin.laySoGioSuDungAnToan())
                : layThoiGianHienTaiVietNam().plusHours(thongTin.laySoGioSuDungAnToan());
        String maPhien = mauJdbc.execute((ConnectionCallback<String>) conn -> {
            String sql = """
                    BEGIN
                        INSERT INTO PHIENLAMVIEC (
                            ThoiGianBatDau,
                            ThoiGianDuKienKetThuc,
                            TrangThaiPhien,
                            ThoiGianKetThuc,
                            CapNhatLanCuoi,
                            MaKG,
                            MaKH,
                            MaDatCho
                        ) VALUES (
                            CURRENT_TIMESTAMP,
                            ?,
                            ?,
                            NULL,
                            CURRENT_TIMESTAMP,
                            ?,
                            ?,
                            ?
                        )
                        RETURNING MaPhien INTO ?;
                    END;
                    """;
            try (var cs = conn.prepareCall(sql)) {
                cs.setTimestamp(1, java.sql.Timestamp.valueOf(thoiGianDuKienKetThuc));
                cs.setString(2, giaTriDb("CHK_PLV_TRANGTHAI", "dang hoat dong", 0, "Đang hoạt động"));
                cs.setString(3, thongTin.getMaKG());
                cs.setString(4, thongTin.getMaKH());
                cs.setString(5, thongTin.getMaDatCho());
                cs.registerOutParameter(6, Types.VARCHAR);
                cs.execute();
                return cs.getString(6);
            }
        });

        mauJdbc.update(
                """
                UPDATE HOADON
                SET DaTraTruoc = (SELECT NVL(ThanhTien, 0) FROM DATCHO WHERE MaDatCho = ?),
                    TongTien = (SELECT NVL(ThanhTien, 0) FROM DATCHO WHERE MaDatCho = ?),
                    ThanhTien = 0,
                    NgayLapHoaDon = CURRENT_TIMESTAMP,
                    PhuongThucThanhToan = ?,
                    TrangThaiThanhToan = ?
                WHERE MaPhien = ?
                """,
                thongTin.getMaDatCho(),
                thongTin.getMaDatCho(),
                giaTriDb("CHK_HD_PTTT", "dat truoc", 2, "Đặt trước"),
                trangThaiHoaDonDb("Da tra truoc"),
                maPhien
        );

        mauJdbc.update(
                """
                UPDATE DATCHO
                SET TrangThaiDatTruoc = ?, MaQR = NULL, CapNhatLanCuoi = CURRENT_TIMESTAMP
                WHERE MaDatCho = ?
                """,
                trangThaiDatChoDb("Da su dung"),
                thongTin.getMaDatCho()
        );

        mauJdbc.update(
                """
                UPDATE KHONGGIAN
                SET TrangThaiKG = ?
                WHERE MaKG = ?
                """,
                trangThaiKhongGianDb("Dang hoat dong"),
                thongTin.getMaKG()
        );

        return maPhien;
    }

    public String taoDatCho(NguoiDungPhien user, String maKG, LocalDateTime arrivalTime,
                              Integer durationHours, BigDecimal totalAmount, String note) {
        String maDatCho = mauJdbc.execute((ConnectionCallback<String>) conn -> {
            String sql = """
                    BEGIN
                        INSERT INTO DATCHO (
                            ThoiGianDat, ThoiGianDuKienToi, KhoangThoiGianSuDung,
                            TrangThaiDatTruoc, ThanhTien, GhiChu, CapNhatLanCuoi, MaKH, MaKG
                        ) VALUES (
                            ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?
                        )
                        RETURNING MaDatCho INTO ?;
                    END;
                    """;
            try (var cs = conn.prepareCall(sql)) {
                cs.setTimestamp(1, Timestamp.valueOf(layThoiGianHienTaiVietNam()));
                cs.setTimestamp(2, Timestamp.valueOf(arrivalTime));
                cs.setObject(3, durationHours);
                cs.setString(4, giaTriDb("CHK_DC_TRANGTHAI", "dang cho thanh toan", 0, "Đang chờ thanh toán"));
                cs.setBigDecimal(5, totalAmount);
                cs.setString(6, note);
                cs.setString(7, user.getMaKH());
                cs.setString(8, maKG);
                cs.registerOutParameter(9, Types.VARCHAR);
                cs.execute();
                return cs.getString(9);
            }
        });

        return maDatCho;
    }

    private LocalDateTime layThoiGianHienTaiVietNam() {
        return LocalDateTime.now(MUI_GIO_VIET_NAM);
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
                       dc.KhoangThoiGianSuDung, dc.TrangThaiDatTruoc, dc.ThanhTien, dc.GhiChu,
                       (SELECT COUNT(*) FROM PHIENLAMVIEC plv WHERE plv.MaDatCho = dc.MaDatCho) AS SoPhien
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
                       dc.KhoangThoiGianSuDung, dc.TrangThaiDatTruoc, dc.ThanhTien, dc.GhiChu,
                       (SELECT COUNT(*) FROM PHIENLAMVIEC plv WHERE plv.MaDatCho = dc.MaDatCho) AS SoPhien
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

    public String timGhiChuDatCho(String maDatCho) {
        if (maDatCho == null || maDatCho.isBlank()) {
            return null;
        }
        try {
            return mauJdbc.queryForObject(
                    "SELECT GhiChu FROM DATCHO WHERE MaDatCho = ?",
                    String.class,
                    maDatCho
            );
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public ThanhToanDatChoView timThanhToanDatChoForUpdate(String maDatCho) {
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
                FOR UPDATE OF dc.TrangThaiDatTruoc NOWAIT
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
            }, maDatCho);
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
        return updated > 0;
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
                  AND dc.ThoiGianDat < CAST(CURRENT_TIMESTAMP AS TIMESTAMP) - INTERVAL '10' MINUTE
                ORDER BY dc.ThoiGianDat ASC
                """;
        return mauJdbc.query(sql, (rs, rowNum) -> mapThongTinXacNhanDatCho(rs),
                trangThaiDatChoDb("Dang cho thanh toan"));
    }

    public boolean taoPhienChoDatChoDaCheckIn(String maDatCho) {
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

        String maPhien = mauJdbc.execute((ConnectionCallback<String>) conn -> {
            String sql = """
                    DECLARE
                        v_SoGio NUMBER;
                        v_MaKG DATCHO.MaKG%TYPE;
                        v_MaKH DATCHO.MaKH%TYPE;
                    BEGIN
                        SELECT NVL(KhoangThoiGianSuDung, 1), MaKG, MaKH
                        INTO v_SoGio, v_MaKG, v_MaKH
                        FROM DATCHO
                        WHERE MaDatCho = ?
                        FOR UPDATE;

                        INSERT INTO PHIENLAMVIEC (
                            ThoiGianBatDau, ThoiGianDuKienKetThuc, TrangThaiPhien,
                            CapNhatLanCuoi, MaKG, MaKH, MaDatCho
                        ) VALUES (
                            CURRENT_TIMESTAMP,
                            CURRENT_TIMESTAMP + NUMTODSINTERVAL(v_SoGio, 'HOUR'),
                            ?, CURRENT_TIMESTAMP, v_MaKG, v_MaKH, ?
                        )
                        RETURNING MaPhien INTO ?;
                    END;
                    """;
            try (var cs = conn.prepareCall(sql)) {
                cs.setString(1, maDatCho);
                cs.setString(2, giaTriDb("CHK_PLV_TRANGTHAI", "dang hoat dong", 0, "Đang hoạt động"));
                cs.setString(3, maDatCho);
                cs.registerOutParameter(4, Types.VARCHAR);
                cs.execute();
                return cs.getString(4);
            }
        });

        mauJdbc.update(
                """
                UPDATE HOADON
                SET DaTraTruoc = (SELECT NVL(ThanhTien, 0) FROM DATCHO WHERE MaDatCho = ?),
                    TongTien = (SELECT NVL(ThanhTien, 0) FROM DATCHO WHERE MaDatCho = ?),
                    ThanhTien = 0,
                    NgayLapHoaDon = CURRENT_TIMESTAMP,
                    PhuongThucThanhToan = ?,
                    TrangThaiThanhToan = ?
                WHERE MaPhien = ?
                """,
                maDatCho,
                maDatCho,
                giaTriDb("CHK_HD_PTTT", "dat truoc", 2, "Đặt trước"),
                trangThaiHoaDonDb("Da tra truoc"),
                maPhien
        );

        mauJdbc.update(
                """
                UPDATE DATCHO
                SET TrangThaiDatTruoc = ?, MaQR = NULL, CapNhatLanCuoi = CURRENT_TIMESTAMP
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
                WHERE dc.MaKH = ?
                  AND dc.MaDatCho = ?
                  AND dc.MaQR IS NOT NULL
                  AND dc.TrangThaiDatTruoc = ?
                """;
        try {
            return mauJdbc.queryForObject(sql, String.class, maKH, maDatCho,
                    trangThaiDatChoDb("Da thanh toan thanh cong"));
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
                       h.NgayLapHoaDon,
                       CASE WHEN p.MaPhien IS NULL THEN 0 ELSE 1 END AS SoPhien
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
            int soPhien = rs.getInt("SoPhien");
            String trangThaiHienThi = maPhien == null
                    ? hienThiTrangThaiDatCho(rs.getString("TrangThaiPhien"), soPhien)
                    : hienThiTrangThaiPhien(rs.getString("TrangThaiPhien"));
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
                    trangThaiHienThi,
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
                    TrangThaiThanhToan = CASE
                        WHEN NVL(TrangThaiThanhToan, '') = 'Đã trả trước' THEN 'Đã trả trước'
                        ELSE ?
                    END,
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
        int soPhien = rs.getInt("SoPhien");
        return new DatChoView(
                rs.getString("MaDatCho"),
                rs.getString("HoTen"),
                rs.getString("TenKG"),
                rs.getString("TenCN"),
                arrivalTime == null ? null : arrivalTime.toLocalDateTime(),
                durationNull ? null : durationHours,
                hienThiTrangThaiDatCho(rs.getString("TrangThaiDatTruoc"), soPhien),
                rs.getBigDecimal("ThanhTien"),
                rs.getString("GhiChu")
        );
    }

    private Timestamp thoiGianBanRonLonHon(Timestamp first, Timestamp second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.after(second) ? first : second;
    }

    private ThongTinNhanChoBangQR anhXaThongTinNhanChoBangQR(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp thoiGianDuKienToi = rs.getTimestamp("ThoiGianDuKienToi");
        int khoangThoiGian = rs.getInt("KhoangThoiGianSuDung");
        Integer khoangThoiGianSuDung = rs.wasNull() ? null : khoangThoiGian;
        return new ThongTinNhanChoBangQR(
                rs.getString("MaDatCho"),
                rs.getString("MaQR"),
                rs.getString("TrangThaiDatTruoc"),
                thoiGianDuKienToi == null ? null : thoiGianDuKienToi.toLocalDateTime(),
                khoangThoiGianSuDung,
                rs.getString("MaKH"),
                rs.getString("MaKG"),
                giaiMaLoiFont(rs.getString("TenKG")),
                rs.getString("MaCN"),
                giaiMaLoiFont(rs.getString("TenCN")),
                giaiMaLoiFont(rs.getString("TenKhachHang"))
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
            return "Thanh toán không thành công";
        }
        if (normalized.contains("thanh cong")) {
            return "Đã thanh toán thành công";
        }
        if (normalized.contains("su dung")) {
            return "Đã sử dụng";
        }
        if (normalized.contains("qua han nhan cho")) {
            return "Quá hạn nhận chỗ";
        }
        return "Đang chờ thanh toán";
    }

    private String trangThaiHoaDonDb(String status) {
        String normalized = chuanHoa(status);
        if (normalized.contains("khong thanh cong")) {
            return "Thanh toán không thành công";
        }
        if (normalized.contains("tra truoc")) {
            return "Đã trả trước";
        }
        if (normalized.contains("thanh cong")) {
            return "Đã thanh toán thành công";
        }
        return "Đang chờ thanh toán";
    }

    private String trangThaiKhongGianDb(String status) {
        String normalized = chuanHoa(status);
        if (normalized.contains("tam khoa")) {
            return "Tạm khoá";
        }
        if (normalized.contains("dat truoc")) {
            return "Đã đặt trước";
        }
        if (normalized.contains("dang hoat dong")) {
            return "Đang hoạt động";
        }
        if (normalized.contains("bao tri")) {
            return "Bảo trì";
        }

        return "Trống";
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
        return ChuyenKhoanQrUtil.taoNoiDungDatCho(maDatCho);
    }

    private String hienThiTrangThai(String value) {
        return hienThiTrangThaiDatCho(value, 0);
    }

    private String hienThiTrangThaiDatCho(String value, int soPhien) {
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
        if (normalized.contains("qua han nhan cho")) {
            return com.wms.config.AppConstants.TRANG_THAI_DAT_CHO_QUA_HAN;
        }
        if (normalized.contains("da su dung")) {
            if (soPhien > 0) {
                return "Đã nhận chỗ";
            }
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
        if (normalized.contains("tra truoc")) {
            return "Đã trả trước";
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
        return "DV0000".equalsIgnoreCase(service.getMaDV()) || chuanHoa(service.getTenDV()).contains("gia han gio");
    }

    public List<DatChoView> timDatChoDaThanhToanNhungKhongDenQuaGio() {
        String sql = """
                SELECT dc.MaDatCho, nd.HoTen, kg.TenKG, cn.TenCN, dc.ThoiGianDuKienToi,
                       dc.KhoangThoiGianSuDung, dc.TrangThaiDatTruoc, dc.ThanhTien, dc.GhiChu,
                       (SELECT COUNT(*) FROM PHIENLAMVIEC plv WHERE plv.MaDatCho = dc.MaDatCho) AS SoPhien
                FROM DATCHO dc
                JOIN KHACHHANG kh ON kh.MaKH = dc.MaKH
                JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND
                JOIN KHONGGIAN kg ON kg.MaKG = dc.MaKG
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                WHERE dc.TrangThaiDatTruoc = ?
                  AND SYSTIMESTAMP > dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR')
                  AND NOT EXISTS (
                      SELECT 1
                      FROM PHIENLAMVIEC plv
                      WHERE plv.MaDatCho = dc.MaDatCho
                  )
                """;
        return mauJdbc.query(sql, (rs, rowNum) -> anhXaDatCho(rs), trangThaiDatChoDb("Da thanh toan thanh cong"));
    }

    public List<java.util.Map<String, Object>> timDatChoDaThanhToanDeDebug() {
        String daThanhToan = trangThaiDatChoDb("Da thanh toan thanh cong");
        String sql = """
                SELECT dc.MaDatCho,
                       dc.TrangThaiDatTruoc,
                       TO_CHAR(dc.ThoiGianDuKienToi, 'YYYY-MM-DD HH24:MI:SS') AS ThoiGianDuKienToi,
                       dc.KhoangThoiGianSuDung,
                       TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD HH24:MI:SS.FF') AS GioHeThong,
                       (SELECT COUNT(*) FROM PHIENLAMVIEC plv WHERE plv.MaDatCho = dc.MaDatCho) AS SoPhien
                FROM DATCHO dc
                WHERE dc.TrangThaiDatTruoc = ?
                ORDER BY dc.ThoiGianDat DESC
                """;
        try {
            return mauJdbc.queryForList(sql + " FETCH FIRST 5 ROWS ONLY", daThanhToan);
        } catch (Exception ex) {
            return mauJdbc.queryForList("""
                SELECT * FROM (
                    SELECT dc.MaDatCho,
                           dc.TrangThaiDatTruoc,
                           TO_CHAR(dc.ThoiGianDuKienToi, 'YYYY-MM-DD HH24:MI:SS') AS ThoiGianDuKienToi,
                           dc.KhoangThoiGianSuDung,
                           TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD HH24:MI:SS.FF') AS GioHeThong,
                           (SELECT COUNT(*) FROM PHIENLAMVIEC plv WHERE plv.MaDatCho = dc.MaDatCho) AS SoPhien
                    FROM DATCHO dc
                    WHERE dc.TrangThaiDatTruoc = ?
                    ORDER BY dc.ThoiGianDat DESC
                ) WHERE ROWNUM <= 5
            """, daThanhToan);
        }
    }

    public int danhDauDatChoKhongDenThanhDaSuDung(String maDatCho) {
        String sql = """
                UPDATE DATCHO
                SET TrangThaiDatTruoc = ?,
                    MaQR = NULL,
                    GhiChu = CASE
                        WHEN GhiChu LIKE '%[SYSTEM_NO_SHOW]%' THEN GhiChu
                        ELSE NVL(GhiChu, '') || ' | [SYSTEM_NO_SHOW] Tự động kết thúc do quá hạn nhận chỗ.'
                    END,
                    CapNhatLanCuoi = CURRENT_TIMESTAMP
                WHERE MaDatCho = ?
                  AND TrangThaiDatTruoc = ?
                  AND SYSTIMESTAMP > ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(KhoangThoiGianSuDung, 1), 'HOUR')
                  AND NOT EXISTS (
                      SELECT 1
                      FROM PHIENLAMVIEC plv
                      WHERE plv.MaDatCho = DATCHO.MaDatCho
                  )
                """;
        return mauJdbc.update(
                sql,
                trangThaiDatChoDb("Qua han nhan cho"),
                maDatCho,
                trangThaiDatChoDb("Da thanh toan thanh cong")
        );
    }

    public void capNhatTrangThaiKhongGianSauKhiDatChoHetHieuLuc(String maKG) {
        String sql = """
                UPDATE KHONGGIAN kg
                SET TrangThaiKG =
                    CASE
                        WHEN EXISTS (
                            SELECT 1
                            FROM PHIENLAMVIEC p
                            WHERE p.MaKG = kg.MaKG
                              AND p.TrangThaiPhien = ?
                        ) THEN ?
                
                        ELSE ?
                    END
                WHERE kg.MaKG = ?
                  AND kg.TrangThaiKG IN (?, ?)
                """;
        mauJdbc.update(
                sql,
                giaTriDb("CHK_PLV_TRANGTHAI", "dang hoat dong", 0, "Đang hoạt động"),
                trangThaiKhongGianDb("Dang hoat dong"),
                trangThaiKhongGianDb("Trong"),
                maKG,
                trangThaiKhongGianDb("Dat truoc"),
                trangThaiKhongGianDb("Tam khoa")
        );
    }

    public String timMaKGCuaDatCho(String maDatCho) {
        if (maDatCho == null || maDatCho.isBlank()) {
            return null;
        }
        try {
            return mauJdbc.queryForObject(
                    "SELECT MaKG FROM DATCHO WHERE MaDatCho = ?",
                    String.class,
                    maDatCho
            );
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public Optional<ThongTinNhanChoBangQR> timDatChoTheoMaDatChoDeNhanCho(String maDatCho) {
        if (maDatCho == null || maDatCho.isBlank()) {
            return Optional.empty();
        }
        String sql = """
                SELECT dc.MaDatCho,
                       dc.MaQR,
                       dc.TrangThaiDatTruoc,
                       dc.ThoiGianDuKienToi,
                       dc.KhoangThoiGianSuDung,
                       dc.MaKH,
                       dc.MaKG,
                       kg.TenKG,
                       kg.MaCN,
                       cn.TenCN,
                       nd.HoTen AS TenKhachHang
                FROM DATCHO dc
                JOIN KHONGGIAN kg ON dc.MaKG = kg.MaKG
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                LEFT JOIN KHACHHANG kh ON dc.MaKH = kh.MaKH
                LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND
                WHERE dc.MaDatCho = ?
                FOR UPDATE OF dc.MaDatCho
                """;
        List<ThongTinNhanChoBangQR> ketQua = mauJdbc.query(
                sql,
                (rs, rowNum) -> anhXaThongTinNhanChoBangQR(rs),
                maDatCho.trim()
        );
        return ketQua.stream().findFirst();
    }

    private String giaiMaLoiFont(String value) {
        return value;
    }

    public record BanGhiXacThuc(
            String maND,
            String hoTen,
            String tenTaiKhoan,
            String email,
            String matKhauMaHoa,
            String trangThaiND,
            String maKH,
            String maNV,
            String maCN,
            String tenCN
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
