package com.wms.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private HikariDataSource dataSource;

    private DatabaseConnection() {
        initConnectionPool();
    }

    private Properties loadProperties() throws Exception {
        try (FileInputStream fs = new FileInputStream("db.properties")) {
            Properties p = new Properties();
            p.load(fs);
            return p;
        }
    }

    private void initConnectionPool() {
        try {
            System.out.println("[DB] Dang khoi tao Pool ket noi HikariCP...");
            Properties p = loadProperties();

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(p.getProperty("url"));
            config.setUsername(p.getProperty("user"));
            config.setPassword(p.getProperty("pass"));

            // Driver Oracle thin
            config.setDriverClassName("oracle.jdbc.OracleDriver");

            // Cau hinh toi uu cho Pool ket noi (nhe, muot cho Desktop App)
            config.setMaximumPoolSize(10);        // So ket noi toi da duy tri
            config.setMinimumIdle(2);            // So ket noi toi thieu luon san sang
            config.setIdleTimeout(300000);       // Dong ket noi thua sau 5 phut
            config.setMaxLifetime(1800000);      // Reset ket noi vat ly sau 30 phut
            config.setConnectionTimeout(15000);  // Thoi gian cho ket noi toi da 15s
            config.setLeakDetectionThreshold(5000); // Bat leak detection 5s
            config.setPoolName("WMS-HikariPool");

            // Query kiem tra trang thai ket noi khoe manh
            config.setConnectionTestQuery("SELECT 1 FROM DUAL");
            config.setConnectionInitSql("ALTER SESSION SET TIME_ZONE = '+07:00'");

            this.dataSource = new HikariDataSource(config);
            System.out.println("[DB] Khoi tao Pool ket noi HikariCP thanh cong!");
        } catch (Exception e) {
            System.err.println("[DB] Loi khoi tao Pool ket noi HikariCP: " + e.getMessage());
            e.printStackTrace();
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
            if (dataSource == null || dataSource.isClosed()) {
                initConnectionPool();
            }
            Connection conn = dataSource.getConnection();
            if (conn == null) {
                throw new SQLException("DataSource returned a null connection.");
            }
            return conn;
        } catch (SQLException e) {
            System.err.println("[DB] Loi lay ket noi tu Pool: " + e.getMessage());
            throw new RuntimeException("[DB] Loi lay ket noi tu Pool: " + e.getMessage(), e);
        }
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("[DB] Da dong Pool ket noi.");
        }
    }
}
