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
    private static final int MIN_IDLE_CONNECTIONS = 2;

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

            // Cau hinh pool gon cho desktop app, tranh login dau tien bi mo ket noi lanh.
            config.setMaximumPoolSize(6);
            config.setMinimumIdle(MIN_IDLE_CONNECTIONS);
            config.setIdleTimeout(300000);
            config.setMaxLifetime(1800000);
            config.setKeepaliveTime(120000);
            config.setConnectionTimeout(5000);
            config.setValidationTimeout(2000);
            config.setLeakDetectionThreshold(30000); // Bat leak detection 30s
            config.setPoolName("WMS-HikariPool");

            // Query kiem tra trang thai ket noi khoe manh
            config.setConnectionTestQuery("SELECT 1 FROM DUAL");
            config.setConnectionInitSql("ALTER SESSION SET TIME_ZONE = '+07:00'");

            this.dataSource = new HikariDataSource(config);
            System.out.println("[DB] Khoi tao Pool ket noi HikariCP thanh cong!");
            preWarmPoolAsync();
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
        long start = System.currentTimeMillis();
        try {
            if (dataSource == null || dataSource.isClosed()) {
                initConnectionPool();
            }
            Connection conn = dataSource.getConnection();
            if (conn == null) {
                throw new SQLException("DataSource returned a null connection.");
            }
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > 300) {
                System.out.println("[DB] Lay connection mat " + elapsed + " ms");
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

    private void preWarmPoolAsync() {
        Thread worker = new Thread(() -> {
            long start = System.currentTimeMillis();
            for (int i = 0; i < MIN_IDLE_CONNECTIONS; i++) {
                try (Connection ignored = dataSource.getConnection()) {
                    // Lay va tra connection de Hikari tao san idle connection.
                } catch (SQLException ex) {
                    System.err.println("[DB] Pre-warm connection that bai: " + ex.getMessage());
                    return;
                }
            }
            System.out.println("[DB] Pre-warm pool hoan tat trong " + (System.currentTimeMillis() - start) + " ms");
        }, "WMS-DB-Prewarm");
        worker.setDaemon(true);
        worker.start();
    }
}
