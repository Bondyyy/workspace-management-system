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
        String sql = "SELECT TenCN FROM CHINHANH WHERE TrangThai = 'Đang hoạt động'";
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

    public List<KhongGianDTO> layTatCaKhongGian() {
        return timKiem(null, null, null);
    }

    public KhongGianDTO layTheoMa(String maKG) {
        String sql = "SELECT kg.MaKG, kg.TenKG, kg.TrangThaiKG, kg.ViTri, kg.MaLoaiKG, lkg.TenLoaiKG, " +
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
            "SELECT kg.MaKG, kg.TenKG, kg.TrangThaiKG, kg.ViTri, kg.MaLoaiKG, lkg.TenLoaiKG, " +
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
        String sql = "INSERT INTO KHONGGIAN (MaKG, TenKG, TrangThaiKG, ViTri, MaLoaiKG, MaCN, ToaDoX, ToaDoY, ChieuDai, ChieuRong) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaKG());
            ps.setString(2, dto.getTenKG());
            ps.setString(3, dto.getTrangThaiKG());
            ps.setString(4, dto.getViTri());
            ps.setString(5, dto.getMaLoaiKG());
            ps.setString(6, dto.getMaCN());
            ps.setInt(7, dto.getToaDoX());
            ps.setInt(8, dto.getToaDoY());
            ps.setInt(9, dto.getChieuDai());
            ps.setInt(10, dto.getChieuRong());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi thêm: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhat(KhongGianDTO dto) {
        String sql = "UPDATE KHONGGIAN SET TenKG = ?, ViTri = ?, MaLoaiKG = ?, TrangThaiKG = ?, " +
                "ToaDoX = ?, ToaDoY = ?, ChieuDai = ?, ChieuRong = ? WHERE MaKG = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getTenKG());
            ps.setString(2, dto.getViTri());
            ps.setString(3, dto.getMaLoaiKG());
            ps.setString(4, dto.getTrangThaiKG());
            ps.setInt(5, dto.getToaDoX());
            ps.setInt(6, dto.getToaDoY());
            ps.setInt(7, dto.getChieuDai());
            ps.setInt(8, dto.getChieuRong());
            ps.setString(9, dto.getMaKG().trim());
            return ps.executeUpdate() > 0;
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
        String sql = "SELECT MAX(TO_NUMBER(SUBSTR(MaKG, 3))) FROM KHONGGIAN WHERE MaKG LIKE 'KG%'";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int maxNum = rs.getInt(1);
                return String.format("KG%03d", maxNum + 1);
            }
        } catch (Exception e) {
            System.err.println("[KhongGianDAO] Lỗi tạo mã mới: " + e.getMessage());
        }
        return "KG" + (System.currentTimeMillis() % 1000);
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
                        return java.time.LocalTime.parse(timeStr);
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
        dto.setTenCN(rs.getString("TenCN"));
        dto.setToaDoX(rs.getInt("ToaDoX"));
        dto.setToaDoY(rs.getInt("ToaDoY"));
        dto.setChieuDai(rs.getInt("ChieuDai"));
        dto.setChieuRong(rs.getInt("ChieuRong"));
        dto.setDonGia(rs.getDouble("DonGiaTheoGio"));
        return dto;
    }
}
