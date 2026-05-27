package com.wms.dao.TrangChuQuanLy.QuanLyPhien;

import com.wms.model.TrangChuHoiVien.DatChoDTO;
import com.wms.config.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DatChoDAO {
    public boolean taoDatChoMoi(DatChoDTO dc) {
        String sql = """
                BEGIN
                    INSERT INTO DATCHO (
                        ThoiGianDat, ThoiGianDuKienToi, KhoangThoiGianSuDung,
                        TrangThaiDatTruoc, ThanhTien,
                        GhiChu, MaKH, MaKG, CapNhatLanCuoi
                    ) VALUES (
                        ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP
                    )
                    RETURNING MaDatCho INTO ?;
                END;
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement pstmt = conn.prepareCall(sql)) {
            pstmt.setTimestamp(1, dc.getThoiGianDat());
            pstmt.setTimestamp(2, dc.getThoiGianDuKienToi());
            if (dc.getKhoangThoiGianSuDung() != null)
                pstmt.setInt(3, dc.getKhoangThoiGianSuDung());
            else
                pstmt.setNull(3, java.sql.Types.INTEGER);
            pstmt.setString(4, dc.getTrangThaiDatTruoc());
            pstmt.setDouble(5, dc.getThanhTien());
            pstmt.setString(6, dc.getGhiChu());
            pstmt.setString(7, dc.getMaKH());
            pstmt.setString(8, dc.getMaKG());
            pstmt.registerOutParameter(9, Types.VARCHAR);

            pstmt.execute();
            dc.setMaDatCho(pstmt.getString(9));
            return true;
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
