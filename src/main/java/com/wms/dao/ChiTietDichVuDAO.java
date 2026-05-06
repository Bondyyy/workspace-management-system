package com.wms.dao;

import com.wms.model.DichVuDaDatDTO;

import com.wms.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChiTietDichVuDAO {
    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<DichVuDaDatDTO> layDanhSachDichVuDat(String keyword) {
        List<DichVuDaDatDTO> list = new ArrayList<>();
        String sql = "SELECT ct.MaPhien, dv.TenDV, ct.SoLuong, p.ThoiGianBatDau, kh.HoTenKH, kg.TenKG, ct.GhiChu "
                +
                "FROM CHITIETDICHVU ct " +
                "JOIN DICHVU dv ON ct.MaDV = dv.MaDV " +
                "JOIN PHIENLAMVIEC p ON ct.MaPhien = p.MaPhien " +
                "JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                "JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                "WHERE ct.MaPhien LIKE ? OR dv.TenDV LIKE ? " +
                "ORDER BY p.ThoiGianBatDau DESC";

        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
                while (rs.next()) {
                    DichVuDaDatDTO dto = new DichVuDaDatDTO();
                    dto.setMaPhien(rs.getString("MaPhien"));
                    dto.setTenDichVu(rs.getString("TenDV"));
                    dto.setSoLuong(rs.getInt("SoLuong"));

                    java.sql.Timestamp ts = rs.getTimestamp("ThoiGianBatDau");
                    if (ts != null) {
                        dto.setThoiGianDat(sdf.format(ts));
                    } else {
                        dto.setThoiGianDat("");
                    }

                    dto.setKhachHang(rs.getString("HoTenKH"));
                    dto.setTenKhongGian(rs.getString("TenKG"));
                    dto.setGhiChu(rs.getString("GhiChu") != null ? rs.getString("GhiChu") : "");

                    list.add(dto);
                }
            }
        } catch (Exception e) {
            System.err.println("[ChiTietDichVuDAO] Lỗi lấy danh sách: " + e.getMessage());
        }
        return list;
    }

    public boolean themDichVuMoi(String maPhien, String tenDV, int soLuong, String ghiChu) {
        // Find MaDV based on TenDV
        String maDV = getMaDV(tenDV);
        if (maDV == null) {
            System.err.println("Không tìm thấy Mã Dịch Vụ cho Tên: " + tenDV);
            return false;
        }

        // Check if MaPhien exists
        if (!kiemTraPhienTonTai(maPhien)) {
            System.err.println("Mã phiên không tồn tại: " + maPhien);
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
        } catch (Exception e) {
            System.err.println("[ChiTietDichVuDAO] Lỗi thêm dịch vụ: " + e.getMessage());
            return false;
        }
    }

    private String getMaDV(String tenDV) {
        String sql = "SELECT MaDV FROM DICHVU WHERE TenDV = ?";
        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getString("MaDV");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean kiemTraPhienTonTai(String maPhien) {
        String sql = "SELECT 1 FROM PHIENLAMVIEC WHERE MaPhien = ?";
        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhien);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getDanhSachLoaiDichVu() {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT TenLoaiDV FROM LOAIDICHVU";
        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(rs.getString("TenLoaiDV"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public List<String> getDanhSachTenDichVu(String tenLoaiDV) {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT dv.TenDV FROM DICHVU dv JOIN LOAIDICHVU ldv ON dv.MaLoaiDV = ldv.MaLoaiDV WHERE ldv.TenLoaiDV = ?";
        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenLoaiDV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(rs.getString("TenDV"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }
}
