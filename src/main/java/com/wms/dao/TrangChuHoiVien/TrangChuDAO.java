package com.wms.dao.TrangChuHoiVien;

import com.wms.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TrangChuDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public int layDiemTichLuy(String maND) {
        int diem = 0;
        String sql = "SELECT NVL(TongChiTieu, 0) AS DiemTichLuy FROM KHACHHANG WHERE MaND = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                diem = rs.getInt("DiemTichLuy");
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy điểm tích lũy: " + e.getMessage());
        }
        return diem;
    }

    public String layHangHienTai(String maND) {
        String hang = "Thành viên mới";
        String sql = "SELECT h.TenHangThanhVien FROM KHACHHANG kh " +
                     "JOIN HANGTHANHVIEN h ON kh.MaHangThanhVien = h.MaHangThanhVien " +
                     "WHERE kh.MaND = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                hang = rs.getString("TenHangThanhVien");
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy hạng thành viên: " + e.getMessage());
        }
        return hang;
    }

    public int layTongGioSuDung(String maND) {
        int gio = 0;
        String sql = """
                SELECT NVL(SUM(NVL(dc.KhoangThoiGianSuDung, 0)), 0) AS TongGio
                FROM DATCHO dc
                JOIN KHACHHANG kh ON dc.MaKH = kh.MaKH
                WHERE kh.MaND = ?
                  AND dc.TrangThaiDatTruoc = 'Đã sử dụng'
                """;
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                gio = rs.getInt("TongGio");
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy tổng giờ sử dụng: " + e.getMessage());
        }
        return gio;
    }

    public int laySoUuDai(String maND) {
        int soUuDai = 0;
        String sql = """
                SELECT COUNT(*) AS SoUuDai
                FROM PHIEUGIAMGIA
                WHERE CURRENT_TIMESTAMP BETWEEN NgayBatDauApDung AND NgayKetThucApDung
                  AND NVL(SLDaDung, 0) < NVL(SLToiDa, 0)
                """;
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                soUuDai = rs.getInt("SoUuDai");
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy số ưu đãi: " + e.getMessage());
        }
        return soUuDai;
    }

    public List<Object[]> layLichSuDatCho(String maND) {
        List<Object[]> list = new ArrayList<>();
        String sql = """
                SELECT dc.MaDatCho, kg.TenKG, dc.ThoiGianDuKienToi, dc.TrangThaiDatTruoc
                FROM DATCHO dc
                JOIN KHACHHANG kh ON dc.MaKH = kh.MaKH
                JOIN KHONGGIAN kg ON dc.MaKG = kg.MaKG
                WHERE kh.MaND = ?
                ORDER BY dc.ThoiGianDat DESC
                FETCH FIRST 10 ROWS ONLY
                """;
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("MaDatCho"),
                    rs.getString("TenKG"),
                    rs.getTimestamp("ThoiGianDuKienToi"),
                    rs.getString("TrangThaiDatTruoc")
                });
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy lịch sử đặt chỗ: " + e.getMessage());
        }
        return list;
    }
}
