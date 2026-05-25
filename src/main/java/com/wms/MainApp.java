package com.wms;

import com.wms.config.DatabaseConnection;
import com.wms.util.SuperAdminCreator;
import com.wms.view.TrangChuGioiThieu.TrangGioiThieuForm;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp {

    private static final Logger logger = Logger.getLogger(MainApp.class.getName());

    public static void main(String[] args) {
        setupLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            logger.info("Đang khởi động hệ thống WMS...");
            TrangGioiThieuForm introForm = new TrangGioiThieuForm();
            introForm.setVisible(true);
            logger.info("Giao diện khởi động đã sẵn sàng.");
        });

        Thread initThread = new Thread(() -> {
            long start = System.currentTimeMillis();
            try {
                SuperAdminCreator.initialize();
                DatabaseConnection.getInstance();
                logger.info("Khởi tạo nền hoàn tất trong " + (System.currentTimeMillis() - start) + " ms");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Lỗi nghiêm trọng khi khởi tạo nền hệ thống", e);
            }
        }, "WMS-Startup-Init");
        initThread.setDaemon(true);
        initThread.start();
    }

    private static void setupLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException ex) {
            logger.log(Level.WARNING, "Không thể thiết lập Look and Feel Nimbus. Sử dụng mặc định của hệ thống.", ex);
        }
    }
}
