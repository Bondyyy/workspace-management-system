package com.wms.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton quản lý kết nối Oracle Database.
 * Đọc thông tin từ file .env ở thư mục gốc project.
 * Cả nhóm dùng: DatabaseConnection.getInstance().getConnection()
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    // Constructor private — không ai new được, phải qua getInstance()
    private DatabaseConnection() {
        try {
            Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .ignoreIfMissing()
                .load();

            String url      = dotenv.get("DB_URL",      "jdbc:oracle:thin:@localhost:1521/FREEPDB1");
            String username = dotenv.get("DB_USERNAME",  "system");
            String password = dotenv.get("DB_PASSWORD",  "");

            Class.forName("oracle.jdbc.OracleDriver");
            this.connection = DriverManager.getConnection(url, username, password);

            System.out.println("[DB] Kết nối Oracle thành công!");

        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Không tìm thấy Oracle JDBC Driver!");
            throw new RuntimeException("Oracle Driver không tồn tại", e);
        } catch (SQLException e) {
            System.err.println("[DB] Kết nối thất bại: " + e.getMessage());
            throw new RuntimeException("Không thể kết nối Database", e);
        }
    }

    /** Lấy instance duy nhất (thread-safe) */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null || isConnectionClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /** Trả về Connection để DAO dùng */
    public Connection getConnection() {
        return connection;
    }

    /** Kiểm tra connection còn sống không */
    private static boolean isConnectionClosed() {
        try {
            return instance == null 
                || instance.connection == null 
                || instance.connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    // KHÔNG gọi connection.close() trong DAO
    // Singleton này sẽ giữ connection suốt vòng đời app
}