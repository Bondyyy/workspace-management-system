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
                     "kh.HoTenKH, kg.TenKG, ct.GhiChu " +
                     "FROM CHITIETDICHVU ct " +
                     "JOIN DICHVU dv ON ct.MaDV = dv.MaDV " +
                     "JOIN PHIENLAMVIEC p ON ct.MaPhien = p.MaPhien " +
                     "JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
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
                            rs.getString("GhiChu") != null ? rs.getString("GhiChu") : ""
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
        String sql = "SELECT p.MaPhien, kh.HoTenKH, kg.TenKG, p.ThoiGianBatDau, p.ThoiGianDuKienKetThuc, p.TrangThaiPhien " +
                     "FROM PHIENLAMVIEC p " +
                     "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                     "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                     "WHERE p.TrangThaiPhien IN ('Đang sử dụng', 'Đang hoạt động') " +
                     "AND (p.MaPhien LIKE ? OR kh.HoTenKH LIKE ? OR kg.TenKG LIKE ?) " +
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

        String sql = "INSERT INTO CHITIETDICHVU (MaPhien, MaDV, SoLuong, GhiChu) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maPhien);
            ps.setString(2, maDV);
            ps.setInt(3, soLuong);
            ps.setString(4, ghiChu);
            return ps.executeUpdate() > 0;

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
