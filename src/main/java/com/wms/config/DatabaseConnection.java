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
        connect();
    }

    private void connect() {
        try (FileInputStream fs = new FileInputStream("db.properties")) {
            Properties p = new Properties();
            p.load(fs);

            if (this.conn != null && !this.conn.isClosed()) {
                return;
            }

            this.conn = DriverManager.getConnection(
                    p.getProperty("url"),
                    p.getProperty("user"),
                    p.getProperty("pass"));

            System.out.println("[DB] Ket noi thanh cong!");
        } catch (Exception e) {
            System.err.println("[DB] Loi ket noi: " + e.getMessage());
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return conn;
    }
}