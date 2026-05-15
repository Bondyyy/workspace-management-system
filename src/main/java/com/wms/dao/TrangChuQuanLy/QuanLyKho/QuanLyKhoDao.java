package com.wms.dao.TrangChuQuanLy.QuanLyKho;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyThongTinDichVu.DichVuDTO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
                    DichVuDTO dto = new DichVuDTO();
                    dto.setMaDV(rs.getString("MaDV"));
                    dto.setTenDV(rs.getString("TenDV"));
                    dto.setTenLoaiDV(rs.getString("LoaiDV")); 
                    dto.setDonGia(rs.getDouble("DonGia"));
                    dto.setGiaNhap(rs.getDouble("GiaNhap"));
                    dto.setTrangThaiDV(rs.getString("TrangThaiDV"));
                    dto.setHinhAnh(rs.getBytes("HinhAnh"));
                    Object soLuongObj = rs.getObject("SoLuong");
                    if (soLuongObj != null) dto.setSoLuong(Integer.parseInt(soLuongObj.toString()));
                    else dto.setSoLuong(null);
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean nhapKhoDichVu(String tenNV, String tenLoaiDV, String tenDV, int soLuong, String tenFile, double giaNhap, byte[] fileData) {
        String sql = "{CALL SP_NhapKhoDichVu(?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = getConn();
        if (conn == null) return false;
        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, tenNV);
            cstmt.setString(2, tenLoaiDV);
            cstmt.setString(3, tenDV);
            cstmt.setInt(4, soLuong);
            if (tenFile == null || tenFile.trim().isEmpty()) cstmt.setNull(5, Types.VARCHAR);
            else cstmt.setString(5, tenFile);
            cstmt.setDouble(6, giaNhap);
            if (fileData != null) cstmt.setBytes(7, fileData);
            else cstmt.setNull(7, Types.BLOB);
            cstmt.execute();
            return true;
        } catch (Exception e) {
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
}