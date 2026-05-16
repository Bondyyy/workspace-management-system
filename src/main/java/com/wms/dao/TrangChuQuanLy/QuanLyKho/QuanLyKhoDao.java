package com.wms.dao.TrangChuQuanLy.QuanLyKho;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyThongTinDichVu.DichVuDTO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import oracle.jdbc.OracleTypes;

public class QuanLyKhoDao {
    
    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }
    
    public List<DichVuDTO> layDanhSachKho(String keyword) {
        List<DichVuDTO> list = new ArrayList<>();
        String sql = "{CALL SP_LayDanhSachKho(?, ?)}";
        Connection conn = getConn();
        if (conn == null) return list;
        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            if (keyword == null || keyword.trim().isEmpty()) cstmt.setNull(1, Types.VARCHAR);
            else cstmt.setString(1, keyword.trim());
            cstmt.registerOutParameter(2, OracleTypes.CURSOR);
            cstmt.execute();
            try (ResultSet rs = (ResultSet) cstmt.getObject(2)) {
                while (rs.next()) {
                    try {
                        DichVuDTO dto = new DichVuDTO();
                        dto.setMaDV(rs.getString("MADV"));
                        dto.setTenDV(rs.getString("TENDV"));
                        dto.setTenLoaiDV(rs.getString("LOAIDV")); 
                        dto.setDonGia(rs.getDouble("DONGIA"));
                        dto.setGiaNhap(rs.getDouble("GIANHAP"));
                        dto.setTrangThaiDV(rs.getString("TRANGTHAIDV"));
                        dto.setHinhAnh(rs.getBytes("HINHANH"));
                        
                        Object soLuongObj = rs.getObject("SOLUONG");
                        if (soLuongObj != null) {
                            dto.setSoLuong(Integer.parseInt(soLuongObj.toString()));
                        } else {
                            dto.setSoLuong(null);
                        }
                        list.add(dto);
                    } catch (SQLException ex) {
                        System.err.println("[QuanLyKhoDao] Loi doc dong du lieu: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean nhapKhoDichVu(String maDV, String tenNV, String tenLoaiDV, String tenDV, int soLuong, String tenFile, double giaNhap, byte[] fileData) {
        String sql = "{CALL SP_NhapKhoDichVu(?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = getConn();
        if (conn == null) {
            System.err.println("[QuanLyKhoDao] Khong co ket noi database!");
            return false;
        }
        System.out.println("[QuanLyKhoDao] Bat dau nhapKhoDichVu: maDV=" + maDV + ", tenDV=" + tenDV + ", tenLoaiDV=" + tenLoaiDV + ", soLuong=" + soLuong + ", giaNhap=" + giaNhap);
        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            if (maDV == null || maDV.trim().isEmpty()) cstmt.setNull(1, Types.VARCHAR);
            else cstmt.setString(1, maDV.trim());
            cstmt.setString(2, tenNV);
            cstmt.setString(3, tenLoaiDV);
            cstmt.setString(4, tenDV);
            cstmt.setInt(5, soLuong);
            if (tenFile == null || tenFile.trim().isEmpty()) cstmt.setNull(6, Types.VARCHAR);
            else cstmt.setString(6, tenFile);
            cstmt.setDouble(7, giaNhap);
            if (fileData != null) cstmt.setBytes(8, fileData);
            else cstmt.setNull(8, Types.BLOB);
            cstmt.execute();
            System.out.println("[QuanLyKhoDao] nhapKhoDichVu thanh cong cho maDV=" + maDV);
            return true;
        } catch (SQLException e) {
            System.err.println("[QuanLyKhoDao] SQL Error nhapKhoDichVu - Code: " + e.getErrorCode() + ", Message: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("[QuanLyKhoDao] Error nhapKhoDichVu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<String> layDSNhanVien() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT nd.HoTen FROM NHANVIEN nv JOIN NGUOIDUNG nd ON nv.MaND = nd.MaND";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString("HoTen"));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<String> layDSLoaiDichVu() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT TenLoaiDV FROM LOAIDICHVU";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString("TenLoaiDV"));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<String> layDSTenDichVuTheoLoai(String tenLoaiDV) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT d.TenDV FROM DICHVU d JOIN LOAIDICHVU l ON d.MaLoaiDV = l.MaLoaiDV WHERE LOWER(l.TenLoaiDV) = LOWER(?)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenLoaiDV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString("TenDV"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public double layDonGiaDichVu(String tenDV) {
        String sql = "SELECT DonGia FROM DICHVU WHERE LOWER(TenDV) = LOWER(?)";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("DonGia");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    /**
     * Lấy thông tin hóa đơn (file + tên file) mới nhất theo MaDV từ CHUNGTUNHAPKHO.
     * Trả về Object[]{byte[] fileData, String tenFile} hoặc null nếu không có.
     */
    public Object[] layHoaDonMoiNhat(String maDV) {
        String sql = "SELECT TenFile, NoiDungFile FROM CHUNGTUNHAPKHO " +
                     "WHERE MaDV = ? AND NoiDungFile IS NOT NULL " +
                     "ORDER BY NgayNhap DESC FETCH FIRST 1 ROWS ONLY";
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tenFile = rs.getString("TenFile");
                    byte[] fileData = rs.getBytes("NoiDungFile");
                    if (fileData != null && fileData.length > 0) {
                        return new Object[]{fileData, tenFile != null ? tenFile : "hoadon"};
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[QuanLyKhoDao] Loi layHoaDonMoiNhat: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}