package com.wms.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;

public final class MaTuDongUtil {

    private MaTuDongUtil() {
    }

    public enum MaDoiTuong {
        NGUOI_DUNG("NGUOIDUNG", "MaND", "ND", 4),
        KHACH_HANG("KHACHHANG", "MaKH", "HV", 4),
        NHAN_VIEN("NHANVIEN", "MaNV", "NV", 4),
        CHI_NHANH("CHINHANH", "MaCN", "CN", 4),
        KHONG_GIAN("KHONGGIAN", "MaKG", "KG", 4),
        LOAI_KHONG_GIAN("LOAIKHONGGIAN", "MaLoaiKG", "LKG", 4),
        DICH_VU("DICHVU", "MaDV", "DV", 4),
        LOAI_DICH_VU("LOAIDICHVU", "MaLoaiDV", "LDV", 4),
        DAT_CHO("DATCHO", "MaDatCho", "DC", 4),
        PHIEN_LAM_VIEC("PHIENLAMVIEC", "MaPhien", "PLV", 4),
        HOA_DON("HOADON", "MaHoaDon", "HD", 4),
        PHIEU_GIAM_GIA("PHIEUGIAMGIA", "MaPGG", "PGG", 4),
        CHUNG_TU_NHAP_KHO("CHUNGTUNHAPKHO", "MaChungTu", "CT", 4);

        private final String tableName;
        private final String columnName;
        private final String prefix;
        private final int width;

        MaDoiTuong(String tableName, String columnName, String prefix, int width) {
            this.tableName = tableName;
            this.columnName = columnName;
            this.prefix = prefix;
            this.width = width;
        }

        public String tableName() {
            return tableName;
        }

        public String columnName() {
            return columnName;
        }

        public String prefix() {
            return prefix;
        }

        public int width() {
            return width;
        }
    }

    public static String format(String prefix, int number, int width) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("Prefix không được rỗng.");
        }
        if (number < 0) {
            throw new IllegalArgumentException("Số thứ tự không được âm.");
        }
        if (width < 1) {
            throw new IllegalArgumentException("Độ rộng phải lớn hơn 0.");
        }
        return prefix.trim().toUpperCase() + String.format("%0" + width + "d", number);
    }

    public static int parseNumber(String prefix, String code) {
        if (code == null || code.isBlank()) {
            return 0;
        }
        String normalizedPrefix = prefix == null ? "" : prefix.trim().toUpperCase();
        String normalizedCode = code.trim().toUpperCase();
        if (!normalizedPrefix.isBlank() && !normalizedCode.startsWith(normalizedPrefix)) {
            return 0;
        }

        int end = normalizedCode.length() - 1;
        while (end >= 0 && Character.isDigit(normalizedCode.charAt(end))) {
            end--;
        }
        if (end == normalizedCode.length() - 1) {
            return 0;
        }
        try {
            return Integer.parseInt(normalizedCode.substring(end + 1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public static String nextFromExisting(Collection<String> existingCodes, String prefix, int width) {
        int max = 0;
        if (existingCodes != null) {
            for (String code : existingCodes) {
                max = Math.max(max, parseNumber(prefix, code));
            }
        }
        return format(prefix, max + 1, width);
    }

    public static String sinhMaTiepTheo(Connection conn, MaDoiTuong doiTuong) throws SQLException {
        Objects.requireNonNull(conn, "Connection không được null.");
        Objects.requireNonNull(doiTuong, "Đối tượng sinh mã không được null.");

        String sql = "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR("
                + doiTuong.columnName()
                + ", '[0-9]+$'))), 0) FROM "
                + doiTuong.tableName()
                + " WHERE REGEXP_LIKE("
                + doiTuong.columnName()
                + ", '^"
                + doiTuong.prefix()
                + "[0-9]+$')";
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            int max = rs.next() ? rs.getInt(1) : 0;
            return format(doiTuong.prefix(), max + 1, doiTuong.width());
        }
    }
}
