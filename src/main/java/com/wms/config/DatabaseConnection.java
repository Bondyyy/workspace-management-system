package com.wms.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        this.connection = connect();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || !connection.isValid(2)) {
                System.out.println("[DB] Đang reconnect...");
                connection = connect();
            }
        } catch (SQLException e) {
            connection = connect();
        }
        return connection;
    }

    private Connection connect() {
        Properties props = loadEnv();
        String url      = props.getProperty("DB_URL");
        String username = props.getProperty("DB_USERNAME");
        String password = props.getProperty("DB_PASSWORD");

        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("[DB] Kết nối Oracle thành công!");
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException("[DB] Kết nối thất bại: " + e.getMessage(), e);
        }
    }

    private Properties loadEnv() {
        Properties props = new Properties();
        String envPath = System.getProperty("user.dir") + "/.env";

        try (FileInputStream fis = new FileInputStream(envPath)) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Không tìm thấy file .env tại: " + envPath, e);
        }

        return props;
    }
}