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
        String sql = "SELECT DiemTichLuy FROM KHACHHANG WHERE MaND = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                diem = rs.getInt("DiemTichLuy");
            }
        } catch (Exception e) {
            // Có thể cột chưa tồn tại trong CSDL, trả về 0
            System.err.println("Lỗi lấy điểm tích lũy: " + e.getMessage());
        }
        return diem;
    }

    public String layHangHienTai(String maND) {
        String hang = "Thành viên Mới";
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
        String sql = "SELECT SUM(SoGio) as TongGio FROM PHIEUDATCHO p " +
                     "JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                     "WHERE kh.MaND = ? AND p.TrangThai = N'Đã hoàn thành'";
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
        String sql = "SELECT COUNT(*) as SoUuDai FROM UUDAI_KHACHHANG uk " +
                     "JOIN KHACHHANG kh ON uk.MaKH = kh.MaKH " +
                     "WHERE kh.MaND = ? AND uk.TrangThai = N'Chưa sử dụng'";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
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
        String sql = "SELECT TOP 10 p.MaPhieu, k.TenKhongGian, p.ThoiGianBatDau, p.TrangThai " +
                     "FROM PHIEUDATCHO p " +
                     "JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                     "JOIN KHONGGIAN k ON p.MaKhongGian = k.MaKhongGian " +
                     "WHERE kh.MaND = ? " +
                     "ORDER BY p.ThoiGianTao DESC";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("MaPhieu"),
                    rs.getString("TenKhongGian"),
                    rs.getString("ThoiGianBatDau"),
                    rs.getString("TrangThai")
                });
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy lịch sử đặt chỗ: " + e.getMessage());
        }
        return list;
    }
}
