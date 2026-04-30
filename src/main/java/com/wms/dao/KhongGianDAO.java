/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wms.dao;

import com.wms.config.DatabaseConnection; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author kyduy
 */

public class KhongGianDAO {
    
    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // [HÀM MỚI] Lấy danh sách các chi nhánh ĐANG HOẠT ĐỘNG để đổ vào ComboBox
    public List<String> layDanhSachChiNhanhHoatDong() {
        List<String> danhSach = new ArrayList<>();
        Connection conn = getConn();
        if (conn == null) return danhSach;

        // Chỉ lấy những chi nhánh có trạng thái 'Đang hoạt động' dựa trên Constraint của bạn
        String sql = "SELECT TenCN FROM CHINHANH WHERE TrangThai = 'Đang hoạt động'"; 

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                danhSach.add(rs.getString("TenCN"));
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi lấy danh sách chi nhánh!");
            e.printStackTrace(); 
        }
        return danhSach;
    }

    // Các hàm cũ giữ nguyên hoàn toàn (vì nó đã đúng 100%)
    public void kiemTraDanhSachKhongGian() {  
        Connection conn = getConn();
        if (conn == null) {
            System.out.println("Lỗi: Không lấy được kết nối tới Database!");
            return;
        }
        String sql = "SELECT MaKhongGian, TenKhongGian FROM KHONGGIAN";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("--- DANH SÁCH KHÔNG GIAN ---");
            while (rs.next()) {
                System.out.println("Mã: " + rs.getString("MaKhongGian") + " | Tên: " + rs.getString("TenKhongGian"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> layDanhSachLoaiKhongGian() {
        List<String> danhSach = new ArrayList<>();
        Connection conn = getConn();
        if (conn == null) return danhSach;
        String sql = "SELECT DISTINCT TenKhongGian FROM KHONGGIAN"; 
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                danhSach.add(rs.getString("TenKhongGian"));
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        return danhSach;
    }

    public boolean kiemTraTinhTrangKhongGian(String tenKhongGian, String ngayDat, String gioToi) {
        Connection conn = getConn();
        if (conn == null) return false;
        boolean isAvailable = false;
        String sql = "SELECT COUNT(*) AS SoLuongTrong FROM KHONGGIAN WHERE TenKhongGian = ? AND TrangThai = 'TRONG'"; 
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenKhongGian);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt("SoLuongTrong") > 0) isAvailable = true; 
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isAvailable;
    }

    public java.time.LocalTime layGioDongCuaCuaChiNhanh(String tenChiNhanh) {
        java.time.LocalTime gioDongCuaDeFault = java.time.LocalTime.of(22, 0); 
        Connection conn = getConn();
        if (conn == null) return gioDongCuaDeFault;

        String sql = "SELECT ThoiGianDongCua FROM CHINHANH WHERE TenCN = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenChiNhanh.trim()); 
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String timeStr = rs.getString("ThoiGianDongCua");
                    if (timeStr != null && !timeStr.trim().isEmpty()) {
                        if(timeStr.length() > 5) {
                            timeStr = timeStr.substring(0, 5); 
                        }
                        return java.time.LocalTime.parse(timeStr); 
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (java.time.format.DateTimeParseException e) {
            System.out.println("Lỗi parse định dạng thời gian.");
        }
        return gioDongCuaDeFault;
    }
}