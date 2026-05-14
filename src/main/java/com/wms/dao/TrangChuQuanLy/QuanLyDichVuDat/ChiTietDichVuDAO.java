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
        String sql = "SELECT ct.MaPhien, dv.TenDV, ct.SoLuong, p.ThoiGianBatDau, " +
                     "nd.HoTen AS HoTenKH, kg.TenKG, ct.GhiChu, ldv.TenLoaiDV " +
                     "FROM CHITIETDICHVU ct " +
                     "JOIN DICHVU dv ON ct.MaDV = dv.MaDV " +
                     "JOIN LOAIDICHVU ldv ON dv.MaLoaiDV = ldv.MaLoaiDV " +
                     "JOIN PHIENLAMVIEC p ON ct.MaPhien = p.MaPhien " +
                     "JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                     "JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                     "JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                     "WHERE ct.MaPhien = ? OR ? IS NULL " +
                     "ORDER BY p.ThoiGianBatDau DESC";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maPhien);
            ps.setString(2, maPhien);

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
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT p.MaPhien, nd.HoTen AS HoTenKH, kg.TenKG, p.ThoiGianBatDau, p.ThoiGianDuKienKetThuc, p.TrangThaiPhien " +
                     "FROM PHIENLAMVIEC p " +
                     "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                     "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                     "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                     "WHERE p.TrangThaiPhien = 'Đang hoạt động' " +
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

    public boolean themDichVuMoi(String maPhien, String tenDV, int soLuong, String ghiChu) {
        String maDV = getMaDV(tenDV);
        if (maDV == null) {
            System.err.println("[ChiTietDichVuDAO] Không tìm thấy dịch vụ: " + tenDV);
            return false;
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
        try (Connection conn = getConn()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, maPhien);
                    ps.setString(2, maDV);
                    ps.setInt(3, soLuong);
                    ps.setString(4, ghiChu);
                    ps.executeUpdate();
                }

                // Nếu là dịch vụ "Gia hạn giờ", tự động tăng thời gian kết thúc dự kiến (mỗi số lượng = 1 giờ)
                if ("DV000".equals(maDV) || "Gia hạn giờ".equalsIgnoreCase(tenDV)) {
                    String updateTimeSql = "UPDATE PHIENLAMVIEC SET ThoiGianDuKienKetThuc = ThoiGianDuKienKetThuc + INTERVAL '1' HOUR * ? WHERE MaPhien = ?";
                    try (PreparedStatement psTime = conn.prepareStatement(updateTimeSql)) {
                        psTime.setInt(1, soLuong);
                        psTime.setString(2, maPhien);
                        psTime.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            System.err.println("[ChiTietDichVuDAO] Lỗi thêm dịch vụ: " + e.getMessage());
            return false;
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

    public boolean xoaDichVu(String maPhien, String tenDV) {
        String maDV = getMaDV(tenDV);
        if (maDV == null) return false;
        String sql = "DELETE FROM CHITIETDICHVU WHERE MaPhien = ? AND MaDV = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhien);
            ps.setString(2, maDV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ChiTietDichVuDAO] Lỗi xóa dịch vụ: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatDichVu(String maPhien, String tenDV, int soLuong, String ghiChu) {
        String maDV = getMaDV(tenDV);
        if (maDV == null) return false;
        String sql = "UPDATE CHITIETDICHVU SET SoLuong = ?, GhiChu = ? WHERE MaPhien = ? AND MaDV = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLuong);
            ps.setString(2, ghiChu);
            ps.setString(3, maPhien);
            ps.setString(4, maDV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ChiTietDichVuDAO] Lỗi cập nhật dịch vụ: " + e.getMessage());
            return false;
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
}
