package com.wms.util;

import com.wms.config.DatabaseConnection;
import java.sql.*;

public class DBUtils {

    // 1. Lấy Connection nhanh
    public static Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // 2. Chạy câu lệnh INSERT, UPDATE, DELETE (DML)
    public static int executeUpdate(String sql, Object... params) {
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Lỗi Update]: " + e.getMessage());
            return -1;
        }
    }

    // 3. Đóng tài nguyên an toàn (Dùng khi làm việc với ResultSet thủ công)
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            // Không đóng conn ở đây nếu bạn muốn dùng Singleton xuyên suốt
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
