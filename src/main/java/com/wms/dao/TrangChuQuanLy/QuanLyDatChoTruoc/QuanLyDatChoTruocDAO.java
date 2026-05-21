package com.wms.dao.TrangChuQuanLy.QuanLyDatChoTruoc;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyDatChoTruoc.DatChoTruocDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) {
            return list;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            ps.setString(4, search);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException ex) {
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
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null || dto == null || dto.getMaDatCho() == null || dto.getMaDatCho().isBlank()) {
            return false;
        }
        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            if ("Đã sử dụng".equals(dto.getTrangThaiDatTruoc())) {
                boolean ok = taoPhienKhiNhanCho(conn, dto.getMaDatCho());
                if (ok) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
                return ok;
            }
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
                ps.setString(6, dto.getTrangThaiDatTruoc());
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
    }

    private boolean taoPhienKhiNhanCho(Connection conn, String maDatCho) throws SQLException {
        if (!coTheNhanCho(conn, maDatCho)) {
            return false;
        }
        String maPhien = taoMaTiepTheo(conn, "PHIENLAMVIEC", "MaPhien", "PH", 4);
        String maHoaDon = taoMaTiepTheo(conn, "HOADON", "MaHoaDon", "HD", 6);

        try (PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO PHIENLAMVIEC
                    (MaPhien, ThoiGianBatDau, ThoiGianDuKienKetThuc, TrangThaiPhien,
                     CapNhatLanCuoi, MaKG, MaKH, MaDatCho)
                SELECT ?, CURRENT_TIMESTAMP,
                       CURRENT_TIMESTAMP + NUMTODSINTERVAL(NVL(KhoangThoiGianSuDung, 1), 'HOUR'),
                       'Đang hoạt động', CURRENT_TIMESTAMP, MaKG, MaKH, MaDatCho
                FROM DATCHO
                WHERE MaDatCho = ?
                """)) {
            ps.setString(1, maPhien);
            ps.setString(2, maDatCho);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO HOADON
                    (MaHoaDon, SoHD, TongTien, ThanhTien, NgayLapHoaDon,
                     PhuongThucThanhToan, TrangThaiThanhToan, MaPhien)
                SELECT ?, ?, NVL(ThanhTien, 0), NVL(ThanhTien, 0), CURRENT_TIMESTAMP,
                       'Chuyển khoản', 'Đã thanh toán thành công', ?
                FROM DATCHO
                WHERE MaDatCho = ?
                """)) {
            ps.setString(1, maHoaDon);
            ps.setString(2, maHoaDon);
            ps.setString(3, maPhien);
            ps.setString(4, maDatCho);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("""
                UPDATE DATCHO
                SET TrangThaiDatTruoc = 'Đã sử dụng', CapNhatLanCuoi = CURRENT_TIMESTAMP
                WHERE MaDatCho = ?
                """)) {
            ps.setString(1, maDatCho);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("""
                UPDATE KHONGGIAN
                SET TrangThaiKG = 'Đang hoạt động'
                WHERE MaKG = (SELECT MaKG FROM DATCHO WHERE MaDatCho = ?)
                """)) {
            ps.setString(1, maDatCho);
            ps.executeUpdate();
        }
        return true;
    }

    private boolean coTheNhanCho(Connection conn, String maDatCho) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM DATCHO dc
                WHERE dc.MaDatCho = ?
                  AND dc.TrangThaiDatTruoc = 'Đã thanh toán thành công'
                  AND NOT EXISTS (SELECT 1 FROM PHIENLAMVIEC p WHERE p.MaDatCho = dc.MaDatCho)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDatCho);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private String taoMaTiepTheo(Connection conn, String tableName, String columnName, String prefix, int width) throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(" + columnName + ", '[0-9]+'))), 0) FROM " + tableName;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int next = 1;
            if (rs.next()) {
                next = rs.getInt(1) + 1;
            }
            return prefix + String.format("%0" + width + "d", next);
        }
    }

    private void capNhatTrangThaiKhongGian(Connection conn, DatChoTruocDTO dto) throws SQLException {
        String status = dto.getTrangThaiDatTruoc();
        String trangThaiKG = null;
        if ("Đang chờ thanh toán".equals(status)) {
            trangThaiKG = "Tạm khoá";
        } else if ("Đã thanh toán thành công".equals(status)) {
            trangThaiKG = "Đã đặt trước";
        } else if ("Thanh toán không thành công".equals(status)) {
            trangThaiKG = "Trống";
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
        dto.setTrangThaiDatTruoc(rs.getString("TrangThaiDatTruoc"));
        dto.setThanhTien(rs.getBigDecimal("ThanhTien"));
        dto.setGhiChu(rs.getString("GhiChu"));
        return dto;
    }
}
