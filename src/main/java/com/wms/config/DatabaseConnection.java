package com.wms.config;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection conn;

    private DatabaseConnection() {
        try (FileInputStream fs = new FileInputStream("db.properties")) {
            Properties p = new Properties();
            p.load(fs);

            this.conn = DriverManager.getConnection(
                    p.getProperty("url"),
                    p.getProperty("user"),
                    p.getProperty("pass"));
            
            System.out.println("[DB] Kết nối thành công!");
        } catch (Exception e) {
            System.err.println("[DB] Lỗi kết nối: " + e.getMessage());
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        try {
            if (instance == null || instance.conn == null || instance.conn.isClosed()) {
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Kết nối cũ lỗi, đang tạo lại...");
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return conn;
    }
}