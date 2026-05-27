package com.wms.dao.TrangChuQuanLy.QuanLyDichVuDat;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyDichVuDat.DichVuDaDatDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietDichVuDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<DichVuDaDatDTO> layDanhSachDichVuDat(String keyword) {
        return layDanhSachDichVuTheoPhien(keyword);
    }

    public List<DichVuDaDatDTO> layDanhSachDichVuTheoPhien(String maPhien) {
        List<DichVuDaDatDTO> list = new ArrayList<>();
        if (maPhien == null || maPhien.trim().isEmpty()) {
            return list;
        }
        String sql = "SELECT ct.MaPhien, dv.TenDV, ct.SoLuong, p.ThoiGianBatDau, " +
                     "nd.HoTen AS HoTenKH, kg.TenKG, ct.GhiChu, ldv.TenLoaiDV " +
                     "FROM CHITIETDICHVU ct " +
                     "JOIN DICHVU dv ON ct.MaDV = dv.MaDV " +
                     "JOIN LOAIDICHVU ldv ON dv.MaLoaiDV = ldv.MaLoaiDV " +
                     "JOIN PHIENLAMVIEC p ON ct.MaPhien = p.MaPhien " +
                     "JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                     "JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                     "JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                     "WHERE ct.MaPhien = ? " +
                     "ORDER BY dv.TenDV";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maPhien.trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("ThoiGianBatDau");
                    list.add(new DichVuDaDatDTO(
                            rs.getString("MaPhien"),
                            rs.getString("TenDV"),
                            rs.getInt("SoLuong"),
                            ts != null ? ts.toString() : "",
                            rs.getString("HoTenKH"),
                            rs.getString("TenKG"),
                            rs.getString("GhiChu") != null ? rs.getString("GhiChu") : "",
                            rs.getString("TenLoaiDV")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ChiTietDichVuDAO] Lỗi lấy danh sách dịch vụ theo phiên: " + e.getMessage());
        }
        return list;
    }

    public List<Object[]> layDanhSachPhienHoatDong(String keyword) {
        return layDanhSachPhien(keyword, false);
    }

    public List<Object[]> layDanhSachPhien(String keyword, boolean daKetThuc) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT p.MaPhien, nd.HoTen AS HoTenKH, kg.TenKG, p.ThoiGianBatDau, p.ThoiGianDuKienKetThuc, p.TrangThaiPhien " +
                     "FROM PHIENLAMVIEC p " +
                     "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                     "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                     "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                     "WHERE " + (daKetThuc ? "p.TrangThaiPhien = 'Đã kết thúc' " : "p.TrangThaiPhien <> 'Đã kết thúc' ") +
                     "AND (p.MaPhien LIKE ? OR nd.HoTen LIKE ? OR kg.TenKG LIKE ?) " +
                     "ORDER BY p.ThoiGianBatDau DESC";

        String pattern = "%" + (keyword == null ? "" : keyword) + "%";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("MaPhien"),
                        rs.getString("HoTenKH") != null ? rs.getString("HoTenKH") : "Vãng lai",
                        rs.getString("TenKG") != null ? rs.getString("TenKG") : "N/A",
                        rs.getTimestamp("ThoiGianBatDau"),
                        rs.getTimestamp("ThoiGianDuKienKetThuc"),
                        rs.getString("TrangThaiPhien")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("[ChiTietDichVuDAO] Lỗi lấy danh sách phiên: " + e.getMessage());
        }
        return list;
    }

    public void themDichVuMoi(String maPhien, String tenDV, int soLuong, String ghiChu) throws SQLException {
        try (Connection conn = getConn()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                kiemTraPhienDangHoatDong(conn, maPhien);
                ThongTinTonKhoDichVu tonKho = layThongTinTonKhoDichVu(conn, tenDV, true);
                if (tonKho == null) {
                    throw new SQLException("Không tìm thấy dịch vụ: " + tenDV);
                }
                if (tonKho.quanLyTonKho && soLuong > tonKho.soLuongTon) {
                    throw new SQLException("Số lượng tồn không đủ");
                }

                String sql = "MERGE INTO CHITIETDICHVU dest " +
                             "USING (SELECT ? AS MaPhien, ? AS MaDV, ? AS SoLuong, ? AS GhiChu FROM DUAL) src " +
                             "ON (dest.MaPhien = src.MaPhien AND dest.MaDV = src.MaDV) " +
                             "WHEN MATCHED THEN " +
                             "    UPDATE SET dest.SoLuong = dest.SoLuong + src.SoLuong, " +
                             "               dest.GhiChu = NVL(src.GhiChu, dest.GhiChu) " +
                             "WHEN NOT MATCHED THEN " +
                             "    INSERT (MaPhien, MaDV, SoLuong, GhiChu) " +
                             "    VALUES (src.MaPhien, src.MaDV, src.SoLuong, src.GhiChu)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, maPhien);
                    ps.setString(2, tonKho.maDV);
                    ps.setInt(3, soLuong);
                    ps.setString(4, ghiChu);
                    ps.executeUpdate();
                }

                if (tonKho.quanLyTonKho) {
                    capNhatTonKho(conn, tonKho.maDV, -soLuong);
                }
                if (tonKho.laGiaHanGio()) {
                    dieuChinhThoiGianDuKien(conn, maPhien, soLuong);
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    public boolean kiemTraPhienTonTai(String maPhien) {
        String sql = "SELECT 1 FROM PHIENLAMVIEC WHERE MaPhien = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maPhien);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[ChiTietDichVuDAO] Lỗi kiểm tra phiên: " + e.getMessage());
            return false;
        }
    }

    public boolean kiemTraPhienDangHoatDong(String maPhien) {
        try (Connection conn = getConn()) {
            kiemTraPhienDangHoatDong(conn, maPhien);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<String> getDanhSachLoaiDichVu() {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT TenLoaiDV FROM LOAIDICHVU ORDER BY TenLoaiDV";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) ds.add(rs.getString("TenLoaiDV"));

        } catch (SQLException e) {
            System.err.println("[ChiTietDichVuDAO] Lỗi lấy loại dịch vụ: " + e.getMessage());
        }
        return ds;
    }

    public List<String> getDanhSachTenDichVu(String tenLoaiDV) {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT dv.TenDV FROM DICHVU dv " +
                     "JOIN LOAIDICHVU ldv ON dv.MaLoaiDV = ldv.MaLoaiDV " +
                     "WHERE ldv.TenLoaiDV = ? ORDER BY dv.TenDV";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenLoaiDV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(rs.getString("TenDV"));
            }
        } catch (SQLException e) {
            System.err.println("[ChiTietDichVuDAO] Lỗi lấy tên dịch vụ: " + e.getMessage());
        }
        return ds;
    }

    public void xoaDichVu(String maPhien, String tenDV) throws SQLException {
        try (Connection conn = getConn()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                kiemTraPhienDangHoatDong(conn, maPhien);
                ThongTinTonKhoDichVu tonKho = layThongTinTonKhoDichVu(conn, tenDV, true);
                if (tonKho == null) {
                    throw new SQLException("Không tìm thấy dịch vụ: " + tenDV);
                }
                int soLuongCu = laySoLuongDichVuTrongPhien(conn, maPhien, tonKho.maDV);
                if (soLuongCu <= 0) {
                    throw new SQLException("Không tìm thấy dịch vụ trong phiên.");
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM CHITIETDICHVU WHERE MaPhien = ? AND MaDV = ?")) {
                    ps.setString(1, maPhien);
                    ps.setString(2, tonKho.maDV);
                    ps.executeUpdate();
                }
                if (tonKho.quanLyTonKho) {
                    capNhatTonKho(conn, tonKho.maDV, soLuongCu);
                }
                if (tonKho.laGiaHanGio()) {
                    dieuChinhThoiGianDuKien(conn, maPhien, -soLuongCu);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    public void capNhatDichVu(String maPhien, String tenDV, int soLuong, String ghiChu) throws SQLException {
        try (Connection conn = getConn()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                kiemTraPhienDangHoatDong(conn, maPhien);
                ThongTinTonKhoDichVu tonKho = layThongTinTonKhoDichVu(conn, tenDV, true);
                if (tonKho == null) {
                    throw new SQLException("Không tìm thấy dịch vụ: " + tenDV);
                }
                int soLuongCu = laySoLuongDichVuTrongPhien(conn, maPhien, tonKho.maDV);
                if (soLuongCu <= 0) {
                    throw new SQLException("Không tìm thấy dịch vụ trong phiên.");
                }
                int delta = soLuong - soLuongCu;
                if (tonKho.quanLyTonKho && delta > 0 && delta > tonKho.soLuongTon) {
                    throw new SQLException("Số lượng tồn không đủ");
                }

                String sql = "UPDATE CHITIETDICHVU SET SoLuong = ?, GhiChu = ? WHERE MaPhien = ? AND MaDV = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, soLuong);
                    ps.setString(2, ghiChu);
                    ps.setString(3, maPhien);
                    ps.setString(4, tonKho.maDV);
                    ps.executeUpdate();
                }
                if (tonKho.quanLyTonKho && delta != 0) {
                    capNhatTonKho(conn, tonKho.maDV, -delta);
                }
                if (tonKho.laGiaHanGio() && delta != 0) {
                    dieuChinhThoiGianDuKien(conn, maPhien, delta);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    private String getMaDV(String tenDV) {
        String sql = "SELECT MaDV FROM DICHVU WHERE TenDV = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenDV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("MaDV");
            }
        } catch (SQLException e) {
            System.err.println("[ChiTietDichVuDAO] Lỗi lấy mã dịch vụ: " + e.getMessage());
        }
        return null;
    }

    private void kiemTraPhienDangHoatDong(Connection conn, String maPhien) throws SQLException {
        if (maPhien == null || maPhien.trim().isEmpty()) {
            throw new SQLException("Vui lòng chọn một phiên làm việc.");
        }
        String sql = "SELECT TrangThaiPhien FROM PHIENLAMVIEC WHERE MaPhien = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhien.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Mã phiên không tồn tại trong hệ thống!");
                }
                String trangThai = rs.getString("TrangThaiPhien");
                if (!"Đang hoạt động".equals(trangThai)) {
                    throw new SQLException("Không thể thay đổi dịch vụ của phiên đã kết thúc.");
                }
            }
        }
    }

    private ThongTinTonKhoDichVu layThongTinTonKhoDichVu(Connection conn, String tenDV, boolean forUpdate) throws SQLException {
        String sql = "SELECT dv.MaDV, dv.MaLoaiDV, dv.TenDV, ldv.TenLoaiDV, NVL(dv.SoLuong, 0) AS SoLuong " +
                     "FROM DICHVU dv JOIN LOAIDICHVU ldv ON dv.MaLoaiDV = ldv.MaLoaiDV " +
                     "WHERE dv.TenDV = ?" + (forUpdate ? " FOR UPDATE OF dv.SoLuong" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDV);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new ThongTinTonKhoDichVu(
                        rs.getString("MaDV"),
                        rs.getString("MaLoaiDV"),
                        rs.getString("TenDV"),
                        rs.getString("TenLoaiDV"),
                        rs.getInt("SoLuong")
                );
            }
        }
    }

    private int laySoLuongDichVuTrongPhien(Connection conn, String maPhien, String maDV) throws SQLException {
        String sql = "SELECT SoLuong FROM CHITIETDICHVU WHERE MaPhien = ? AND MaDV = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhien);
            ps.setString(2, maDV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("SoLuong") : 0;
            }
        }
    }

    private void capNhatTonKho(Connection conn, String maDV, int delta) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE DICHVU SET SoLuong = NVL(SoLuong, 0) + ? WHERE MaDV = ?")) {
            ps.setInt(1, delta);
            ps.setString(2, maDV);
            ps.executeUpdate();
        }
    }

    private void dieuChinhThoiGianDuKien(Connection conn, String maPhien, int soGio) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE PHIENLAMVIEC SET ThoiGianDuKienKetThuc = ThoiGianDuKienKetThuc + NUMTODSINTERVAL(?, 'HOUR') WHERE MaPhien = ?")) {
            ps.setInt(1, soGio);
            ps.setString(2, maPhien);
            ps.executeUpdate();
        }
    }

    private static final class ThongTinTonKhoDichVu {
        private final String maDV;
        private final String maLoaiDV;
        private final String tenDV;
        private final String tenLoaiDV;
        private final int soLuongTon;
        private final boolean quanLyTonKho;

        private ThongTinTonKhoDichVu(String maDV, String maLoaiDV, String tenDV, String tenLoaiDV, int soLuongTon) {
            this.maDV = maDV;
            this.maLoaiDV = maLoaiDV;
            this.tenDV = tenDV;
            this.tenLoaiDV = tenLoaiDV;
            this.soLuongTon = soLuongTon;
            this.quanLyTonKho = !laTienIch() && !laGiaHanGio();
        }

        private boolean laTienIch() {
            return tenLoaiDV != null && tenLoaiDV.toLowerCase(java.util.Locale.ROOT).contains("tiện ích");
        }

        private boolean laGiaHanGio() {
            return "DV0000".equalsIgnoreCase(maDV)
                    || (tenDV != null && tenDV.equalsIgnoreCase("Gia hạn giờ"));
        }
    }
}
