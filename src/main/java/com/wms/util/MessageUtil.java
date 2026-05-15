package com.wms.util;

import javax.swing.*;
import java.awt.*;

public class MessageUtil {

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        String friendlyMessage = translateError(message);
        JOptionPane.showMessageDialog(parent, friendlyMessage, "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        int choice = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    private static String translateError(String technicalMessage) {
        if (technicalMessage == null)
            return "Đã có lỗi xảy ra, vui lòng thử lại!";

        if (technicalMessage.contains("ORA-00001")) {
            if (technicalMessage.contains("UNIQUE_ND_SDT"))
                return "Số điện thoại này đã được đăng ký bởi tài khoản khác!";
            if (technicalMessage.contains("UNIQUE_ND_Email"))
                return "Email này đã được sử dụng bởi tài khoản khác!";
            if (technicalMessage.contains("UNIQUE_ND_TenTaiKhoan"))
                return "Tên tài khoản này đã tồn tại trên hệ thống!";
            return "Dữ liệu bị trùng lặp! Vui lòng kiểm tra lại Mã hoặc các thông tin duy nhất.";
        }

        if (technicalMessage.contains("ORA-02290")) {
            return "Dữ liệu không hợp lệ theo quy định của hệ thống!";
        }

        if (technicalMessage.contains("ORA-02291")) {
            return "Thông tin liên kết không tồn tại trong hệ thống!";
        }

        if (technicalMessage.contains("ORA-02292")) {
            return "Không thể thực hiện do đang có dữ liệu liên quan khác sử dụng thông tin này!";
        }

        if (technicalMessage.contains("ORA-12899")) {
            return "Dữ liệu bạn nhập quá dài so với quy định!";
        }

        if (technicalMessage.contains("ORA-01400")) {
            return "Vui lòng điền đầy đủ các thông tin bắt buộc!";
        }

        if (technicalMessage.length() > 200) {
            return "Lỗi hệ thống: " + technicalMessage.substring(0, 150) + "...";
        }

        return technicalMessage;
    }
}
