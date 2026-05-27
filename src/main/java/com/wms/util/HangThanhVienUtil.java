package com.wms.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class HangThanhVienUtil {
    public static final String TEN_HANG_KHONG_CO = "Không có";
    public static final String TEN_HANG_DONG = "Đồng";

    public static final String SQL_MA_HANG_KHACH_HANG_MAC_DINH = """
            COALESCE(
                (SELECT MaHangThanhVien
                 FROM HANGTHANHVIEN
                 WHERE TenHangThanhVien = 'Đồng'
                   AND ROWNUM = 1),
                (SELECT MaHangThanhVien
                 FROM (
                     SELECT MaHangThanhVien
                     FROM HANGTHANHVIEN
                     WHERE TenHangThanhVien <> 'Không có'
                     ORDER BY NVL(TongChiTieuToiThieu, 0), MaHangThanhVien
                 )
                 WHERE ROWNUM = 1)
            )
            """;

    private HangThanhVienUtil() {
    }

    public static String layMaHangKhachHangMacDinh(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT " + SQL_MA_HANG_KHACH_HANG_MAC_DINH + " FROM DUAL");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    public static HangThanhVienSnapshot layHangKhachHangMacDinh(Connection conn) throws SQLException {
        String sql = """
                SELECT TenHangThanhVien, NVL(PhanTramTienGiam, 0) AS PhanTramTienGiam
                FROM HANGTHANHVIEN
                WHERE MaHangThanhVien = (
                    SELECT %s
                    FROM DUAL
                )
                """.formatted(SQL_MA_HANG_KHACH_HANG_MAC_DINH);
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new HangThanhVienSnapshot(
                        rs.getString("TenHangThanhVien"),
                        rs.getBigDecimal("PhanTramTienGiam"));
            }
        }
        return new HangThanhVienSnapshot(TEN_HANG_DONG, BigDecimal.ZERO);
    }

    public static boolean laHangKhongCo(String tenHangThanhVien) {
        return tenHangThanhVien == null
                || tenHangThanhVien.isBlank()
                || TEN_HANG_KHONG_CO.equalsIgnoreCase(tenHangThanhVien.trim());
    }

    public record HangThanhVienSnapshot(
            String tenHangThanhVien,
            BigDecimal phanTramTienGiam
    ) {
    }
}
