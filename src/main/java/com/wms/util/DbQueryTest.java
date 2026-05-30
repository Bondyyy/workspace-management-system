package com.wms.util;

import com.wms.config.DatabaseConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbQueryTest {
    public static void main(String[] args) {
        System.out.println("[DbQueryTest] Bắt đầu cập nhật Stored Procedure...");
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            Path sqlPath = Path.of("Database/04_procedures/QuanLyPGG/sp_ThanhToanVoiPGG.sql");
            String sqlContent = Files.readString(sqlPath);
            
            // Tách phần ký tự '/' ở cuối file SQL của Oracle nếu có
            if (sqlContent.trim().endsWith("/")) {
                sqlContent = sqlContent.substring(0, sqlContent.lastIndexOf("/"));
            }
            
            System.out.println("[DbQueryTest] Đang thực thi lệnh SQL...");
            stmt.execute(sqlContent);
            kiemTraTrangThaiProcedure(conn);
            System.out.println("[DbQueryTest] Cập nhật thành công SP_ThanhToanVoiPhieuGiamGia trong Oracle Database!");
            
        } catch (Exception e) {
            System.err.println("[DbQueryTest] Lỗi thực thi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void kiemTraTrangThaiProcedure(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("""
                     SELECT STATUS
                     FROM USER_OBJECTS
                     WHERE OBJECT_NAME = 'SP_THANHTOANVOIPHIEUGIAMGIA'
                       AND OBJECT_TYPE = 'PROCEDURE'
                     """)) {
            if (!rs.next()) {
                throw new IllegalStateException("Không tìm thấy SP_ThanhToanVoiPhieuGiamGia trong USER_OBJECTS.");
            }

            String status = rs.getString("STATUS");
            System.out.println("[DbQueryTest] Trạng thái SP_ThanhToanVoiPhieuGiamGia: " + status);
            if (!"VALID".equalsIgnoreCase(status)) {
                inLoiBienDich(conn);
                throw new IllegalStateException("SP_ThanhToanVoiPhieuGiamGia compile chưa thành công.");
            }
        }
    }

    private static void inLoiBienDich(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("""
                     SELECT LINE, POSITION, TEXT
                     FROM USER_ERRORS
                     WHERE NAME = 'SP_THANHTOANVOIPHIEUGIAMGIA'
                     ORDER BY SEQUENCE
                     """)) {
            while (rs.next()) {
                System.err.println("[DbQueryTest] Compile error line "
                        + rs.getInt("LINE") + ", position " + rs.getInt("POSITION")
                        + ": " + rs.getString("TEXT"));
            }
        }
    }
}
