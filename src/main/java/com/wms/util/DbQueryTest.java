package com.wms.util;

import com.wms.config.DatabaseConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
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
            System.out.println("[DbQueryTest] Cập nhật thành công SP_ThanhToanVoiPhieuGiamGia trong Oracle Database!");
            
        } catch (Exception e) {
            System.err.println("[DbQueryTest] Lỗi thực thi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
