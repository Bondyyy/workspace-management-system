package com.wms;

import com.wms.config.DatabaseConnection;
import com.wms.util.SuperAdminCreator;
import com.wms.view.TrangChuGioiThieu.TrangGioiThieuForm;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lớp khởi chạy chính của hệ thống quản lý không gian (Workspace Management System)
 */
public class MainApp {

    private static final Logger logger = Logger.getLogger(MainApp.class.getName());

    public static void main(String[] args) {
        // 1. Cấu hình giao diện (Look and Feel)
        setupLookAndFeel();

        // 2. Khởi tạo các thành phần hệ thống cần thiết và hiển thị giao diện chính
        SwingUtilities.invokeLater(() -> {
            try {
                logger.info("Đang khởi động hệ thống WMS...");
                
                // (Tùy chọn) Kiểm tra kết nối cơ sở dữ liệu trước khi khởi chạy UI
                // DatabaseConnection.getInstance();

                // Kiểm tra và khởi tạo tài khoản Super Admin và các quyền cơ bản (nếu chưa có)
                SuperAdminCreator.initialize();

                // Khởi tạo và hiển thị màn hình trang chủ / đăng nhập
                TrangGioiThieuForm introForm = new TrangGioiThieuForm();
                introForm.setVisible(true);
                
                logger.info("Khởi động hệ thống thành công!");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Lỗi nghiêm trọng khi khởi động hệ thống", e);
            }
        });
    }

    /**
     * Thiết lập Look and Feel cho ứng dụng.
     * Mặc định sử dụng Nimbus theo thiết kế của dự án.
     */
    private static void setupLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            logger.log(Level.WARNING, "Không thể thiết lập Look and Feel Nimbus. Sử dụng mặc định của hệ thống.", ex);
        }
    }
}
