package com.wms.dao.TrangChuQuanLy.TongQuan;

import com.wms.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThongKeDAO {

    private static final String ALL_BRANCHES = "Tất cả chi nhánh";
    private static final String PAID_STATUS_FILTER = "Đã thanh toán%";

    public List<Object[]> layRecentTransactions() {
        List<Object[]> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) {
            return list;
        }

        String sql = "SELECT h.MaHoaDon, nd.HoTen AS HoTenKH, h.ThanhTien, h.TrangThaiThanhToan " +
                "FROM HOADON h " +
                "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                "ORDER BY h.NgayLapHoaDon DESC " +
                "FETCH FIRST 5 ROWS ONLY";

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            DecimalFormat df = new DecimalFormat("#,###");
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("MaHoaDon"),
                        rs.getString("HoTenKH") == null ? "Khách vãng lai" : rs.getString("HoTenKH"),
                        df.format(rs.getDouble("ThanhTien")),
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
        if (conn == null) {
            return ketQua;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT SUM(h.ThanhTien) AS DoanhThuThuc, SUM(h.TongTien) AS TruocGiam " +
                        "FROM HOADON h " +
                        "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                        "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                        "WHERE h.TrangThaiThanhToan LIKE ? ");

        appendDateAndBranchFilters(sql, tuNgay, denNgay, maChiNhanh);

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindPaidDateAndBranch(ps, tuNgay, denNgay, maChiNhanh);
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

    public List<Double> layDoanhThu7NgayGanNhat(String maChiNhanh) {
        List<Double> data = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) {
            return data;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT TRUNC(h.NgayLapHoaDon) AS Ngay, SUM(h.ThanhTien) AS Tong " +
                        "FROM HOADON h " +
                        "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                        "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                        "WHERE h.TrangThaiThanhToan LIKE ? " +
                        "AND h.NgayLapHoaDon >= TRUNC(SYSDATE) - 6 ");

        if (hasBranchFilter(maChiNhanh)) {
            sql.append("AND kg.MaCN = ? ");
        }
        sql.append("GROUP BY TRUNC(h.NgayLapHoaDon) ORDER BY Ngay");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, PAID_STATUS_FILTER);
            if (hasBranchFilter(maChiNhanh)) {
                ps.setString(2, extractBranchCode(maChiNhanh));
            }

            Map<String, Double> map = new HashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getDate("Ngay").toString(), rs.getDouble("Tong"));
                }
            }

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
        if (conn == null) {
            return stats;
        }

        String sql = "SELECT PhuongThucThanhToan, COUNT(*) AS SoLuong FROM HOADON " +
                "WHERE TrangThaiThanhToan LIKE ? " +
                "GROUP BY PhuongThucThanhToan";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PAID_STATUS_FILTER);
            try (ResultSet rs = ps.executeQuery()) {
                int tong = 0;
                int ckCount = 0;
                int tmCount = 0;

                while (rs.next()) {
                    String pt = rs.getString("PhuongThucThanhToan");
                    int sl = rs.getInt("SoLuong");
                    if ("Chuyển khoản".equalsIgnoreCase(pt) || "Momo".equalsIgnoreCase(pt)) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }

    public List<Object[]> layDanhSachHoaDonTheoDieuKien(String tuNgay, String denNgay, String maChiNhanh, String loaiDT) {
        List<Object[]> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) {
            return list;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT h.MaHoaDon, nd.HoTen AS HoTenKH, h.NgayLapHoaDon, h.TongTien, " +
                        "h.ThanhTien, h.PhuongThucThanhToan, h.TrangThaiThanhToan " +
                        "FROM HOADON h " +
                        "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                        "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                        "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                        "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                        "WHERE h.TrangThaiThanhToan LIKE ? ");

        appendDateAndBranchFilters(sql, tuNgay, denNgay, maChiNhanh);
        sql.append("ORDER BY h.NgayLapHoaDon DESC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindPaidDateAndBranch(ps, tuNgay, denNgay, maChiNhanh);
            try (ResultSet rs = ps.executeQuery()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                DecimalFormat df = new DecimalFormat("#,###");
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getString("MaHoaDon"),
                            rs.getString("HoTenKH") == null ? "Khách vãng lai" : rs.getString("HoTenKH"),
                            rs.getTimestamp("NgayLapHoaDon") != null ? sdf.format(rs.getTimestamp("NgayLapHoaDon")) : "",
                            df.format(rs.getDouble("TongTien")),
                            df.format(rs.getDouble("ThanhTien")),
                            rs.getString("PhuongThucThanhToan"),
                            rs.getString("TrangThaiThanhToan")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void appendDateAndBranchFilters(StringBuilder sql, String tuNgay, String denNgay, String maChiNhanh) {
        if (tuNgay != null && !tuNgay.isBlank()) {
            sql.append("AND h.NgayLapHoaDon >= TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') ");
        }
        if (denNgay != null && !denNgay.isBlank()) {
            sql.append("AND h.NgayLapHoaDon <= TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') ");
        }
        if (hasBranchFilter(maChiNhanh)) {
            sql.append("AND kg.MaCN = ? ");
        }
    }

    private void bindPaidDateAndBranch(PreparedStatement ps, String tuNgay, String denNgay, String maChiNhanh) throws java.sql.SQLException {
        int idx = 1;
        ps.setString(idx++, PAID_STATUS_FILTER);
        if (tuNgay != null && !tuNgay.isBlank()) {
            ps.setString(idx++, convertFormat(tuNgay) + " 00:00:00");
        }
        if (denNgay != null && !denNgay.isBlank()) {
            ps.setString(idx++, convertFormat(denNgay) + " 23:59:59");
        }
        if (hasBranchFilter(maChiNhanh)) {
            ps.setString(idx, extractBranchCode(maChiNhanh));
        }
    }

    private boolean hasBranchFilter(String maChiNhanh) {
        return maChiNhanh != null && !maChiNhanh.isBlank() && !ALL_BRANCHES.equals(maChiNhanh);
    }

    private String extractBranchCode(String maChiNhanh) {
        return maChiNhanh.split(" - ")[0].trim();
    }

    private String convertFormat(String dateStr) {
        try {
            String[] p = dateStr.split("/");
            return p[2] + "-" + p[1] + "-" + p[0];
        } catch (Exception e) {
            return dateStr;
        }
    }
}
