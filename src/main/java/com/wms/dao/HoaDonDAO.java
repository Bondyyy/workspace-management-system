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
import com.wms.model.ThanhToan_KhuyenMai.ThongTinHoaDonDTO;
import com.wms.model.ThanhToan_KhuyenMai.DichVuDaDungDTO;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

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

    // Hàm lấy chi tiết hóa đơn (JOIN nhiều bảng)
    public ThongTinHoaDonDTO layThongTinChiTietHoaDon(String maHoaDon) {
        ThongTinHoaDonDTO thongTin = null;
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) return null;

        // Query lấy thông tin chung của hóa đơn
        String sqlChung = "SELECT h.MaHoaDon, h.TongTien, p.MaPhien, " +
                          "p.ThoiGianBatDau, p.ThoiGianKetThuc, " +
                          "kh.HoTenKH, kg.TenKG " +
                          "FROM HOADON h " +
                          "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                          "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                          "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                          "WHERE h.MaHoaDon = ?";

        // Query lấy danh sách dịch vụ
        String sqlDichVu = "SELECT dv.TenDV, ct.SoLuong, dv.DonGia " +
                           "FROM CHITIETDICHVU ct " +
                           "JOIN DICHVU dv ON ct.MaDV = dv.MaDV " +
                           "WHERE ct.MaPhien = ?";

        try (PreparedStatement psChung = conn.prepareStatement(sqlChung)) {
            psChung.setString(1, maHoaDon);
            try (ResultSet rsChung = psChung.executeQuery()) {
                if (rsChung.next()) {
                    thongTin = new ThongTinHoaDonDTO();
                    thongTin.setMaHoaDon(rsChung.getString("MaHoaDon"));
                    thongTin.setHoTenKH(rsChung.getString("HoTenKH"));
                    thongTin.setTenKhongGian(rsChung.getString("TenKG"));
                    thongTin.setTongTien(rsChung.getDouble("TongTien"));

                    String maPhien = rsChung.getString("MaPhien");
                    Timestamp tBĐ = rsChung.getTimestamp("ThoiGianBatDau");
                    Timestamp tKT = rsChung.getTimestamp("ThoiGianKetThuc");

                    // Xử lý chuỗi thời gian và số giờ
                    if (tBĐ != null && tKT != null) {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        String chuoiThoiGian = timeFormat.format(tBĐ) + " - " + timeFormat.format(tKT) + " (" + dateFormat.format(tBĐ) + ")";
                        thongTin.setThoiGianSửDung(chuoiThoiGian);
                        
                        long diffMillis = tKT.getTime() - tBĐ.getTime();
                        double soGio = (double) diffMillis / (1000 * 60 * 60);
                        thongTin.setTongSoGio(Math.round(soGio * 10.0) / 10.0); // Làm tròn 1 chữ số thập phân
                    }

                    // Lấy danh sách dịch vụ
                    try (PreparedStatement psDV = conn.prepareStatement(sqlDichVu)) {
                        psDV.setString(1, maPhien);
                        try (ResultSet rsDV = psDV.executeQuery()) {
                            while (rsDV.next()) {
                                DichVuDaDungDTO dv = new DichVuDaDungDTO(
                                    rsDV.getString("TenDV"),
                                    rsDV.getInt("SoLuong"),
                                    rsDV.getDouble("DonGia")
                                );
                                thongTin.getDanhSachDichVu().add(dv);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[HoaDonDAO] Lỗi lấy chi tiết hóa đơn: " + e.getMessage());
        }
        return thongTin;
    }

    // Hàm xác nhận thanh toán
    public boolean xacNhanThanhToan(String maHoaDon, String phuongThucThanhToan, String maNV) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) return false;

        String sql = "UPDATE HOADON SET PhuongThucThanhToan = ?, MaNV = ? WHERE MaHoaDon = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phuongThucThanhToan);
            pstmt.setString(2, maNV);
            pstmt.setString(3, maHoaDon);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("[HoaDonDAO] Lỗi cập nhật thanh toán: " + e.getMessage());
            return false;
        }
    }
}
