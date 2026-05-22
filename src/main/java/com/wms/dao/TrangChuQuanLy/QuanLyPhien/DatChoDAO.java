package com.wms.dao.TrangChuQuanLy.QuanLyPhien;

import com.wms.model.TrangChuHoiVien.DatChoDTO;
import com.wms.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatChoDAO {
    public boolean taoDatChoMoi(DatChoDTO dc) {
        String sql = "INSERT INTO DATCHO (MaDatCho, ThoiGianDat, ThoiGianDuKienToi, KhoangThoiGianSuDung, " +
                "TrangThaiDatTruoc, ThanhTien, GhiChu, MaKH, MaKG) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dc.getMaDatCho());
            pstmt.setTimestamp(2, dc.getThoiGianDat());
            pstmt.setTimestamp(3, dc.getThoiGianDuKienToi());
            if (dc.getKhoangThoiGianSuDung() != null)
                pstmt.setInt(4, dc.getKhoangThoiGianSuDung());
            else
                pstmt.setNull(4, java.sql.Types.INTEGER);
            pstmt.setString(5, dc.getTrangThaiDatTruoc());
            pstmt.setDouble(6, dc.getThanhTien());
            pstmt.setString(7, dc.getGhiChu());
            pstmt.setString(8, dc.getMaKH());
            pstmt.setString(9, dc.getMaKG());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DatChoDAO] Lỗi khi tạo đơn đặt chỗ: " + e.getMessage());
            return false;
        }
    }

    public boolean xacNhanThanhToan(String maDatCho) {
        String sql = "UPDATE DATCHO SET TrangThaiDatTruoc = 'Đã thanh toán thành công', CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaDatCho = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maDatCho);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DatChoDAO] Lỗi khi xác nhận thanh toán đơn đặt: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatMaQR(String maDatCho, String maQR) {
        String sql = "UPDATE DATCHO SET MaQR = ?, CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaDatCho = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maQR);
            pstmt.setString(2, maDatCho);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DatChoDAO] Lỗi khi cập nhật mã QR đặt chỗ: " + e.getMessage());
            return false;
        }
    }
}
