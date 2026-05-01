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
import com.wms.model.ThanhToan_KhuyenMai.HoaDonDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HoaDonDAO {
    
    // Hàm lưu hóa đơn đang chờ thanh toán vào Database
    public boolean taoHoaDonMoi(HoaDonDTO hd) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) return false;

        // Bỏ trống PhuongThucThanhToan, MaPhien, MaPGG, MaNV vì đây mới là bước Đặt Chỗ, chưa thanh toán xong
        String sql = "INSERT INTO HOADON (MaHoaDon, SoHD, TongTien, ThanhTien, NgayLapHoaDon, TrangThaiThanhToan) " +
                     "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hd.getMaHoaDon());
            pstmt.setString(2, hd.getSoHD());
            pstmt.setDouble(3, hd.getTongTien());
            pstmt.setDouble(4, hd.getThanhTien());
            pstmt.setString(5, hd.getTrangThaiThanhToan()); // Sẽ gán là 'Đang chờ thanh toán'

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.out.println("Lỗi khi lưu hóa đơn vào DB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
