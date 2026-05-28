package com.wms.dao.TrangChuQuanLy.QuanLyKhongGian;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyKhongGian.KhongGianDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhongGianDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<String> layDanhSachChiNhanhHoatDong() {
        List<String> danhSach = new ArrayList<>();
        String sql = """
                SELECT TenCN
                FROM CHINHANH
                WHERE TrangThai = 'Đang hoạt động'
                ORDER BY
                    CASE
                        WHEN REGEXP_LIKE(MaCN, '^CN[0-9]+$')
                        THEN TO_NUMBER(REGEXP_SUBSTR(MaCN, '[0-9]+$'))
                    END NULLS LAST,
                    MaCN
                """;
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                danhSach.add(rs.getString("TenCN"));
            }
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi lấy DS chi nhánh: " + e.getMessage());
        }
        return danhSach;
    }

    public List<String> layDanhSachLoaiKhongGian() {
        List<String> danhSach = new ArrayList<>();
        String sql = "SELECT TenLoaiKG FROM LOAIKHONGGIAN ORDER BY TenLoaiKG";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                danhSach.add(rs.getString("TenLoaiKG"));
            }
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi lấy danh sách loại: " + e.getMessage());
        }
        return danhSach;
    }

    public List<KhongGianDTO> layTheoChiNhanh(String maCN) {
        return timKiem(null, maCN, null);
    }

    public List<KhongGianDTO> layTheoChiNhanh(String maCN, java.sql.Timestamp batDau, java.sql.Timestamp ketThuc) {
        if (batDau == null || ketThuc == null || !ketThuc.after(batDau)) {
            return layTheoChiNhanh(maCN);
        }
        hetHanDatChoChoThanhToan();
        String sql = """
                SELECT kg.MaKG, kg.TenKG, kg.TrangThaiKG, kg.ViTri, kg.MaLoaiKG,
                       lkg.TenLoaiKG, lkg.TrangThai AS TrangThaiLoaiKG,
                       kg.MaCN, cn.TenCN, kg.ToaDoX, kg.ToaDoY, kg.ChieuDai, kg.ChieuRong,
                       lkg.DonGiaTheoGio,
                       CASE
                           WHEN kg.TrangThaiKG = 'Bảo trì' THEN 'Bảo trì'
                           WHEN kg.TrangThaiKG = 'Đang hoạt động' THEN 'Đang hoạt động'
                           WHEN EXISTS (
                               SELECT 1
                               FROM PHIENLAMVIEC p
                               WHERE p.MaKG = kg.MaKG
                                 AND p.TrangThaiPhien = 'Đang hoạt động'
                                 AND p.ThoiGianBatDau < ?
                                 AND p.ThoiGianDuKienKetThuc > ?
                           ) THEN 'Đang hoạt động'
                           WHEN EXISTS (
                               SELECT 1
                               FROM DATCHO dc
                               WHERE dc.MaKG = kg.MaKG
                                 AND dc.TrangThaiDatTruoc = 'Đang chờ thanh toán'
                                 AND dc.ThoiGianDat >= CAST(CURRENT_TIMESTAMP AS TIMESTAMP) - INTERVAL '10' MINUTE
                                 AND dc.ThoiGianDuKienToi < ?
                                 AND dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR') > ?
                           ) THEN 'Tạm khóa'
                           WHEN EXISTS (
                               SELECT 1
                               FROM DATCHO dc
                               WHERE dc.MaKG = kg.MaKG
                                 AND dc.TrangThaiDatTruoc IN ('Đã thanh toán thành công', 'Đã đặt trước')
                                 AND dc.ThoiGianDuKienToi < ?
                                 AND dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR') > ?
                           ) THEN 'Đã đặt trước'
                           ELSE 'Trống'
                       END AS TrangThaiHienThi
                FROM KHONGGIAN kg
                LEFT JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG
                LEFT JOIN CHINHANH cn ON kg.MaCN = cn.MaCN
                WHERE (? IS NULL OR kg.MaCN = ?)
                ORDER BY kg.MaKG ASC
                """;
        List<KhongGianDTO> list = new ArrayList<>();
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, ketThuc);
            ps.setTimestamp(2, batDau);
            ps.setTimestamp(3, ketThuc);
            ps.setTimestamp(4, batDau);
            ps.setTimestamp(5, ketThuc);
            ps.setTimestamp(6, batDau);
            ps.setString(7, maCN);
            ps.setString(8, maCN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhongGianDTO dto = mapRow(rs);
                    String hienThi = rs.getString("TrangThaiHienThi");
                    dto.setTrangThaiHienThi(hienThi);
                    dto.setCoTheDat("Trống".equals(hienThi));
                    dto.setLyDoKhongTheDat(dto.isCoTheDat() ? null : hienThi);
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi lấy sơ đồ theo khung giờ: " + e.getMessage());
        }
        return list;
    }

    private void hetHanDatChoChoThanhToan() {
        String sql = """
                UPDATE DATCHO
                SET TrangThaiDatTruoc = 'Thanh toán không thành công',
                    MaQR = NULL,
                    GhiChu = CASE
                        WHEN GhiChu LIKE '%[SYSTEM_PAYMENT_EXPIRED]%' THEN GhiChu
                        ELSE NVL(GhiChu, '') || ' | [SYSTEM_PAYMENT_EXPIRED] Hết hạn thanh toán sau 10 phút.'
                    END,
                    CapNhatLanCuoi = CURRENT_TIMESTAMP
                WHERE TrangThaiDatTruoc = 'Đang chờ thanh toán'
                  AND ThoiGianDat < CAST(CURRENT_TIMESTAMP AS TIMESTAMP) - INTERVAL '10' MINUTE
                """;
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi xử lý giữ chỗ hết hạn: " + e.getMessage());
        }
    }

    public List<KhongGianDTO> layTatCaKhongGian() {
        return timKiem(null, null, null);
    }

    public KhongGianDTO layTheoMa(String maKG) {
        String sql = "SELECT kg.MaKG, kg.TenKG, kg.TrangThaiKG, kg.ViTri, kg.MaLoaiKG, lkg.TenLoaiKG, lkg.TrangThai AS TrangThaiLoaiKG, " +
                "kg.MaCN, cn.TenCN, kg.ToaDoX, kg.ToaDoY, kg.ChieuDai, kg.ChieuRong, lkg.DonGiaTheoGio " +
                "FROM KHONGGIAN kg " +
                "LEFT JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG " +
                "LEFT JOIN CHINHANH cn ON kg.MaCN = cn.MaCN " +
                "WHERE kg.MaKG = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKG);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi lấy theo mã: " + e.getMessage());
        }
        return null;
    }

    public List<KhongGianDTO> timKiem(String tuKhoa, String maCN, String maLoaiKG) {
        StringBuilder sql = new StringBuilder(
            "SELECT kg.MaKG, kg.TenKG, kg.TrangThaiKG, kg.ViTri, kg.MaLoaiKG, lkg.TenLoaiKG, lkg.TrangThai AS TrangThaiLoaiKG, " +
            "kg.MaCN, cn.TenCN, kg.ToaDoX, kg.ToaDoY, kg.ChieuDai, kg.ChieuRong, lkg.DonGiaTheoGio " +
            "FROM KHONGGIAN kg " +
            "LEFT JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG " +
            "LEFT JOIN CHINHANH cn ON kg.MaCN = cn.MaCN " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            sql.append("AND (UPPER(kg.TenKG) LIKE UPPER(?) OR UPPER(kg.MaKG) LIKE UPPER(?)) ");
            params.add("%" + tuKhoa + "%");
            params.add("%" + tuKhoa + "%");
        }
        if (maCN != null && !maCN.isBlank()) {
            sql.append("AND kg.MaCN = ? ");
            params.add(maCN);
        }
        if (maLoaiKG != null && !maLoaiKG.isBlank()) {
            sql.append("AND kg.MaLoaiKG = ? ");
            params.add(maLoaiKG);
        }
        sql.append("ORDER BY kg.MaKG ASC");

        List<KhongGianDTO> list = new ArrayList<>();
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi tìm kiếm: " + e.getMessage());
        }
        return list;
    }

    public boolean them(KhongGianDTO dto) {
        String sql = "INSERT INTO KHONGGIAN (TenKG, TrangThaiKG, ViTri, MaLoaiKG, MaCN, ToaDoX, ToaDoY, ChieuDai, ChieuRong) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getTenKG());
            ps.setString(2, dto.getTrangThaiKG());
            ps.setString(3, dto.getViTri());
            ps.setString(4, dto.getMaLoaiKG());
            ps.setString(5, dto.getMaCN());
            if (dto.getToaDoX() != null) ps.setInt(6, dto.getToaDoX()); else ps.setNull(6, Types.NUMERIC);
            if (dto.getToaDoY() != null) ps.setInt(7, dto.getToaDoY()); else ps.setNull(7, Types.NUMERIC);
            ps.setInt(8, dto.getChieuDai());
            ps.setInt(9, dto.getChieuRong());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi thêm: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhat(KhongGianDTO dto) {
        String sql = "{call SP_CapNhatKhongGian(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = getConn();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, dto.getMaKG().trim());
            cs.setString(2, dto.getTenKG());
            cs.setString(3, dto.getViTri());
            cs.setString(4, dto.getMaLoaiKG());
            cs.setString(5, dto.getTrangThaiKG());
            if (dto.getToaDoX() != null) cs.setInt(6, dto.getToaDoX()); else cs.setNull(6, Types.NUMERIC);
            if (dto.getToaDoY() != null) cs.setInt(7, dto.getToaDoY()); else cs.setNull(7, Types.NUMERIC);
            cs.setInt(8, dto.getChieuDai());
            cs.setInt(9, dto.getChieuRong());
            cs.registerOutParameter(10, Types.VARCHAR);
            cs.execute();

            String message = cs.getString(10);
            return message != null && message.startsWith("Cập nhật không gian");
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi cập nhật: " + e.getMessage());
            return false;
        }
    }

    public boolean xoa(String maKG) {
        String sql = "DELETE FROM KHONGGIAN WHERE MaKG = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKG);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi xóa: " + e.getMessage());
            return false;
        }
    }

    public int demSoLuong() {
        String sql = "SELECT COUNT(*) FROM KHONGGIAN";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi đếm số lượng: " + e.getMessage());
        }
        return 0;
    }

    public String taoMaMoi() {
        return "";
    }

    public boolean kiemTraTinhTrangKhongGian(String tenLoaiKG, String ngayDat, String gioToi) {
        String sql = "SELECT COUNT(*) FROM KHONGGIAN kg " +
                "JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG " +
                "WHERE lkg.TenLoaiKG = ? AND (kg.TrangThaiKG = 'Trống' OR kg.TrangThaiKG = 'TRONG')";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenLoaiKG);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public java.time.LocalTime layGioDongCuaCuaChiNhanh(String tenChiNhanh) {
        java.time.LocalTime gioDongCuaDeFault = java.time.LocalTime.of(22, 0);
        String sql = "SELECT ThoiGianDongCua FROM CHINHANH WHERE TenCN = ?";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenChiNhanh.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String timeStr = rs.getString("ThoiGianDongCua");
                    if (timeStr != null && !timeStr.trim().isEmpty()) {
                        if (timeStr.length() > 5) timeStr = timeStr.substring(0, 5);
                        return com.wms.util.DateInputUtil.parseTime(timeStr, "Giờ đóng cửa");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gioDongCuaDeFault;
    }

    private KhongGianDTO mapRow(ResultSet rs) throws SQLException {
        KhongGianDTO dto = new KhongGianDTO();
        dto.setMaKG(rs.getString("MaKG"));
        dto.setTenKG(rs.getString("TenKG"));
        dto.setTrangThaiKG(rs.getString("TrangThaiKG"));
        dto.setViTri(rs.getString("ViTri"));
        dto.setMaLoaiKG(rs.getString("MaLoaiKG"));
        dto.setMaCN(rs.getString("MaCN"));
        dto.setTenLoaiKG(rs.getString("TenLoaiKG"));
        dto.setTrangThaiLoaiKG(rs.getString("TrangThaiLoaiKG"));
        dto.setTenCN(rs.getString("TenCN"));
        int x = rs.getInt("ToaDoX");
        dto.setToaDoX(rs.wasNull() ? null : x);
        int y = rs.getInt("ToaDoY");
        dto.setToaDoY(rs.wasNull() ? null : y);
        dto.setChieuDai(rs.getInt("ChieuDai"));
        dto.setChieuRong(rs.getInt("ChieuRong"));
        dto.setDonGia(rs.getDouble("DonGiaTheoGio"));
        return dto;
    }
}
