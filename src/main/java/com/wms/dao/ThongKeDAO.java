package com.wms.dao;

import com.wms.config.DatabaseConnection;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThongKeDAO {

    public List<Object[]> layRecentTransactions() {
        List<Object[]> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return list;

        String sql = "SELECT h.MaHoaDon, kh.HoTenKH, h.ThanhTien, h.TrangThaiThanhToan " +
                "FROM HOADON h " +
                "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                "ORDER BY h.NgayLapHoaDon DESC " +
                "FETCH FIRST 5 ROWS ONLY";

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[] {
                        rs.getString("MaHoaDon"),
                        rs.getString("HoTenKH") == null ? "Khách vãng lai" : rs.getString("HoTenKH"),
                        new DecimalFormat("#,###").format(rs.getDouble("ThanhTien")),
                        rs.getString("TrangThaiThanhToan")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<String, Double> layDoanhThuTongHop(String tuNgay, String denNgay, String maChiNhanh, String loaiDT) {
        Map<String, Double> ketQua = new HashMap<>();
        ketQua.put("doanhThuThuc", 0.0);
        ketQua.put("truocGiam", 0.0);
        ketQua.put("chietKhau", 0.0);

        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return ketQua;

        StringBuilder sql = new StringBuilder(
                "SELECT SUM(h.ThanhTien) as DoanhThuThuc, SUM(h.TongTien) as TruocGiam " +
                        "FROM HOADON h " +
                        "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                        "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                        "WHERE h.TrangThaiThanhToan = 'Đã thanh toán' ");

        if (tuNgay != null && !tuNgay.isEmpty()) {
            sql.append("AND h.NgayLapHoaDon >= ? ");
        }
        if (denNgay != null && !denNgay.isEmpty()) {
            sql.append("AND h.NgayLapHoaDon <= ? ");
        }
        if (maChiNhanh != null && !maChiNhanh.equals("Tất cả chi nhánh")) {
            sql.append("AND kg.MaChiNhanh = ? ");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (tuNgay != null && !tuNgay.isEmpty())
                ps.setString(idx++, convertFormat(tuNgay) + " 00:00:00");
            if (denNgay != null && !denNgay.isEmpty())
                ps.setString(idx++, convertFormat(denNgay) + " 23:59:59");
            if (maChiNhanh != null && !maChiNhanh.equals("Tất cả chi nhánh")) {
                String ma = maChiNhanh.split(" - ")[0];
                ps.setString(idx++, ma);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double thuc = rs.getDouble("DoanhThuThuc");
                    double truoc = rs.getDouble("TruocGiam");
                    ketQua.put("doanhThuThuc", thuc);
                    ketQua.put("truocGiam", truoc);
                    ketQua.put("chietKhau", Math.max(0, truoc - thuc));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ketQua;
    }

    public List<Double> layDoanhThu7NgayGầnNhất(String maChiNhanh) {
        List<Double> data = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return data;

        // Lấy doanh thu của 7 ngày gần nhất (bao gồm cả hôm nay)
        String sql = "SELECT TRUNC(NgayLapHoaDon) as Ngay, SUM(ThanhTien) as Tong " +
                "FROM HOADON h " +
                "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                "WHERE h.TrangThaiThanhToan = 'Đã thanh toán' " +
                "AND NgayLapHoaDon >= TRUNC(SYSDATE) - 6 ";

        if (maChiNhanh != null && !maChiNhanh.equals("Tất cả chi nhánh")) {
            sql += "AND kg.MaChiNhanh = ? ";
        }

        sql += "GROUP BY TRUNC(NgayLapHoaDon) ORDER BY Ngay";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (maChiNhanh != null && !maChiNhanh.equals("Tất cả chi nhánh")) {
                ps.setString(1, maChiNhanh.split(" - ")[0]);
            }

            // Map để lưu kết quả tạm thời
            Map<String, Double> map = new HashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getDate("Ngay").toString(), rs.getDouble("Tong"));
                }
            }

            // Đảm bảo đủ 7 ngày (nếu ngày nào không có thì là 0)
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_YEAR, -6);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            for (int i = 0; i < 7; i++) {
                String d = sdf.format(cal.getTime());
                data.add(map.getOrDefault(d, 0.0));
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public Map<String, Integer> layCoCauThanhToan() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("CK", 0);
        stats.put("TM", 0);

        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return stats;

        String sql = "SELECT PhuongThucThanhToan, COUNT(*) as SoLuong FROM HOADON " +
                "WHERE TrangThaiThanhToan = 'Đã thanh toán' " +
                "GROUP BY PhuongThucThanhToan";

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            int tong = 0;
            int ckCount = 0;
            int tmCount = 0;

            while (rs.next()) {
                String pt = rs.getString("PhuongThucThanhToan");
                int sl = rs.getInt("SoLuong");
                if ("Chuyển khoản".equals(pt) || "Momo".equals(pt)) {
                    ckCount += sl;
                } else {
                    tmCount += sl;
                }
                tong += sl;
            }

            if (tong > 0) {
                stats.put("CK", (ckCount * 100) / tong);
                stats.put("TM", 100 - stats.get("CK"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }

    private String convertFormat(String dateStr) {
        // dd/MM/yyyy -> yyyy-MM-dd
        try {
            String[] p = dateStr.split("/");
            return p[2] + "-" + p[1] + "-" + p[0];
        } catch (Exception e) {
            return dateStr;
        }
    }
}
