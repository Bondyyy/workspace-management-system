/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wms.dao;

/**
 *
 * @author kyduy
 */
import com.wms.config.DatabaseConnection;
import com.wms.model.ThanhToan_KhuyenMai.PhieuGiamGiaDTO; // Import đúng chuẩn DTO của bạn
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PhieuGiamGiaDAO {
    
    // Hàm lấy thông tin mã giảm giá từ DB
    public PhieuGiamGiaDTO layThongTinVoucher(String maVoucher) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) return null;

        String sql = "SELECT MaPGG, GiaTriGiamGia, GiaTriApDungToiThieu, NgayBatDauApDung, NgayKetThucApDung, SLDaDung, SLToiDa " +
                     "FROM PHIEUGIAMGIA WHERE MaPGG = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Trim để tránh trường hợp người dùng copy dư dấu cách
            pstmt.setString(1, maVoucher.trim()); 
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    PhieuGiamGiaDTO dto = new PhieuGiamGiaDTO();
                    dto.setMaPGG(rs.getString("MaPGG"));
                    dto.setGiaTriGiamGia(rs.getDouble("GiaTriGiamGia"));
                    dto.setGiaTriApDungToiThieu(rs.getDouble("GiaTriApDungToiThieu"));
                    dto.setNgayBatDauApDung(rs.getTimestamp("NgayBatDauApDung"));
                    dto.setNgayKetThucApDung(rs.getTimestamp("NgayKetThucApDung"));
                    
                    // Dùng đúng setter theo DTO của bạn
                    dto.setSLDaDung(rs.getInt("SLDaDung")); 
                    dto.setSLToiDa(rs.getInt("SLToiDa"));
                    
                    return dto;
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi truy xuất Voucher: " + e.getMessage());
        }
        return null;
    }
}