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
            try {
                logger.info("Đang khởi động hệ thống WMS...");
                SuperAdminCreator.initialize();

                TrangGioiThieuForm introForm = new TrangGioiThieuForm();
                introForm.setVisible(true);

                logger.info("Khởi động hệ thống thành công!");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Lỗi nghiêm trọng khi khởi động hệ thống", e);
            }
        });
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
