package com.wms.dao.TrangChuQuanLy.QuanLyDatChoTruoc;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyDatChoTruoc.DatChoTruocDTO;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuanLyDatChoTruocDAO {

    public List<DatChoTruocDTO> layDanhSach(String keyword) {
        List<DatChoTruocDTO> list = new ArrayList<>();
        String search = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String sql = """
                SELECT dc.MaDatCho, dc.MaKH, nd.HoTen, dc.MaKG, kg.TenKG,
                       dc.ThoiGianDuKienToi, dc.KhoangThoiGianSuDung,
                       dc.TrangThaiDatTruoc, dc.ThanhTien, dc.GhiChu
                FROM DATCHO dc
                JOIN KHACHHANG kh ON kh.MaKH = dc.MaKH
                JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND
                JOIN KHONGGIAN kg ON kg.MaKG = dc.MaKG
                WHERE dc.MaDatCho LIKE ?
                   OR dc.MaKH LIKE ?
                   OR nd.HoTen LIKE ?
                   OR kg.TenKG LIKE ?
                ORDER BY dc.ThoiGianDat DESC
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            ps.setString(4, search);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (Exception ex) {
            System.err.println("[QuanLyDatChoTruocDAO] Lỗi tải danh sách đặt chỗ: " + ex.getMessage());
        }
        return list;
    }

    public boolean capNhat(DatChoTruocDTO dto) {
        String sql = """
                UPDATE DATCHO
                SET MaKH = ?,
                    MaKG = ?,
                    ThoiGianDuKienToi = ?,
                    KhoangThoiGianSuDung = ?,
                    ThanhTien = ?,
                    TrangThaiDatTruoc = ?,
                    GhiChu = ?,
                    CapNhatLanCuoi = CURRENT_TIMESTAMP
                WHERE MaDatCho = ?
                """;
        if (dto == null || dto.getMaDatCho() == null || dto.getMaDatCho().isBlank()) {
            return false;
        }
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                String trangThaiHienThi = hienThiTrangThaiDatCho(dto.getTrangThaiDatTruoc());
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, dto.getMaKH());
                    ps.setString(2, dto.getMaKG());
                    ps.setTimestamp(3, dto.getThoiGianDuKienToi());
                    if (dto.getKhoangThoiGianSuDung() == null) {
                        ps.setNull(4, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(4, dto.getKhoangThoiGianSuDung());
                    }
                    ps.setBigDecimal(5, dto.getThanhTien());
                    ps.setString(6, trangThaiDatChoDb(conn, trangThaiHienThi));
                    ps.setString(7, dto.getGhiChu());
                    ps.setString(8, dto.getMaDatCho());
                    int updated = ps.executeUpdate();
                    capNhatTrangThaiKhongGian(conn, dto);
                    conn.commit();
                    return updated > 0;
                }
            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
                System.err.println("[QuanLyDatChoTruocDAO] Lỗi cập nhật đặt chỗ: " + ex.getMessage());
                return false;
            } finally {
                try {
                    conn.setAutoCommit(oldAutoCommit);
                } catch (SQLException ignored) {
                }
            }
        } catch (Exception e) {
            System.err.println("[QuanLyDatChoTruocDAO] Lỗi kết nối CSDL: " + e.getMessage());
            return false;
        }
    }

    private void capNhatTrangThaiKhongGian(Connection conn, DatChoTruocDTO dto) throws SQLException {
        String status = hienThiTrangThaiDatCho(dto.getTrangThaiDatTruoc());
        String trangThaiKG = null;
        if ("Đang chờ thanh toán".equals(status)) {
            trangThaiKG = trangThaiKhongGianDb(conn, "Tam khoa");
        } else if ("Đã thanh toán thành công".equals(status)) {
            trangThaiKG = trangThaiKhongGianDb(conn, "Da dat truoc");
        } else if ("Thanh toán không thành công".equals(status)) {
            trangThaiKG = trangThaiKhongGianDb(conn, "Trong");
        }
        if (trangThaiKG == null) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement("UPDATE KHONGGIAN SET TrangThaiKG = ? WHERE MaKG = ?")) {
            ps.setString(1, trangThaiKG);
            ps.setString(2, dto.getMaKG());
            ps.executeUpdate();
        }
    }

    private DatChoTruocDTO map(ResultSet rs) throws SQLException {
        DatChoTruocDTO dto = new DatChoTruocDTO();
        dto.setMaDatCho(rs.getString("MaDatCho"));
        dto.setMaKH(rs.getString("MaKH"));
        dto.setHoTenKhachHang(rs.getString("HoTen"));
        dto.setMaKG(rs.getString("MaKG"));
        dto.setTenKhongGian(rs.getString("TenKG"));
        dto.setThoiGianDuKienToi(rs.getTimestamp("ThoiGianDuKienToi"));
        int duration = rs.getInt("KhoangThoiGianSuDung");
        dto.setKhoangThoiGianSuDung(rs.wasNull() ? null : duration);
        dto.setTrangThaiDatTruoc(hienThiTrangThaiDatCho(rs.getString("TrangThaiDatTruoc")));
        dto.setThanhTien(rs.getBigDecimal("ThanhTien"));
        dto.setGhiChu(rs.getString("GhiChu"));
        return dto;
    }

    private String trangThaiDatChoDb(Connection conn, String status) {
        String normalized = chuanHoa(status);
        if (normalized.contains("khong thanh cong")) {
            return giaTriDb(conn, "CHK_DC_TRANGTHAI", "khong thanh cong", 2, "Thanh toán không thành công");
        }
        if (normalized.contains("thanh cong")) {
            return giaTriDb(conn, "CHK_DC_TRANGTHAI", "thanh cong", 1, "Đã thanh toán thành công");
        }
        if (normalized.contains("su dung")) {
            return giaTriDb(conn, "CHK_DC_TRANGTHAI", "su dung", 3, "Đã sử dụng");
        }
        return giaTriDb(conn, "CHK_DC_TRANGTHAI", "cho thanh toan", 0, "Đang chờ thanh toán");
    }

    private String trangThaiKhongGianDb(Connection conn, String status) {
        String normalized = chuanHoa(status);
        if (normalized.contains("tam khoa")) {
            return giaTriDb(conn, "CHK_KG_TRANGTHAI", "tam khoa", 1, "Tạm khoá");
        }
        if (normalized.contains("dat truoc")) {
            return giaTriDb(conn, "CHK_KG_TRANGTHAI", "dat truoc", 2, "Đã đặt trước");
        }
        if (normalized.contains("dang hoat dong")) {
            return giaTriDb(conn, "CHK_KG_TRANGTHAI", "dang hoat dong", 3, "Đang hoạt động");
        }
        if (normalized.contains("bao tri")) {
            return giaTriDb(conn, "CHK_KG_TRANGTHAI", "bao tri", 5, "Bảo trì");
        }
        return giaTriDb(conn, "CHK_KG_TRANGTHAI", "trong", 0, "Trống");
    }

    private String giaTriDb(Connection conn, String constraintName, String normalizedNeedle, int fallbackIndex, String fallbackValue) {
        List<String> values = layGiaTriRangBuoc(conn, constraintName);
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

    private List<String> layGiaTriRangBuoc(Connection conn, String constraintName) {
        List<String> values = new ArrayList<>();
        if (conn == null || constraintName == null || constraintName.isBlank()) {
            return values;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT search_condition_vc FROM user_constraints WHERE constraint_name = ?")) {
            ps.setString(1, constraintName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String condition = rs.getString(1);
                    if (condition != null) {
                        Matcher matcher = Pattern.compile("'([^']*)'").matcher(condition);
                        while (matcher.find()) {
                            values.add(matcher.group(1));
                        }
                    }
                }
            }
        } catch (SQLException ignored) {
            // Fallback labels keep the management screen usable without metadata access.
        }
        return values;
    }

    private String hienThiTrangThaiDatCho(String value) {
        if (value == null || value.isBlank()) {
            return "Chưa có trạng thái";
        }
        String decoded = giaiMaLoiFont(value);
        String normalized = chuanHoa(decoded);
        if (normalized.contains("cho thanh toan")) {
            return "Đang chờ thanh toán";
        }
        if (normalized.contains("khong thanh cong")) {
            return "Thanh toán không thành công";
        }
        if (normalized.contains("thanh cong")) {
            return "Đã thanh toán thành công";
        }
        if (normalized.contains("su dung")) {
            return "Đã sử dụng";
        }
        return decoded;
    }

    private String chuanHoa(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(giaiMaLoiFont(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String giaiMaLoiFont(String value) {
        if (value == null || value.isBlank() || !coDauHieuLoiFont(value)) {
            return value;
        }
        try {
            String decoded = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            return decoded.indexOf('\uFFFD') >= 0 ? value : decoded;
        } catch (RuntimeException ex) {
            return value;
        }
    }

    private boolean coDauHieuLoiFont(String value) {
        return value.contains("Ã")
                || value.contains("Ä")
                || value.contains("Â")
                || value.contains("Æ")
                || value.contains("áº")
                || value.contains("á»");
    }
}
