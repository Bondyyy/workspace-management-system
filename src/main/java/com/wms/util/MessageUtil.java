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
        showError(parent, message, null);
    }

    public static void showError(Component parent, String message, Throwable t) {
        if (t != null) {
            t.printStackTrace(System.err);
        }
        System.err.println("[DATABASE/SYSTEM ERROR]: " + message);
        
        if (message != null && message.contains("ORA-01400")) {
            System.err.println("\n=================== HƯỚNG DẪN KHẮC PHỤC LỖI DATABASE ===================");
            System.err.println("Lỗi ORA-01400 (Cannot insert NULL) xảy ra khi CSDL yêu cầu một trường giá trị bắt buộc");
            System.err.println("(như MaND, MaVaiTro, MaKH,...) nhưng giá trị được gửi lên CSDL lại bị NULL hoặc thiếu.");
            System.err.println("\nNguyên nhân chính:");
            System.err.println("1. Các Triggers và Sequences tự động sinh mã trong file 'Database/01_table/TaoMaTuDong.sql'");
            System.err.println("   (ví dụ: TRG_TAO_MA_VAITRO, TRG_TAO_MA_NGUOIDUNG) CHƯA được biên dịch hoặc bị DISABLE");
            System.err.println("   trên CSDL Oracle của bạn.");
            System.err.println("2. Khi Trigger không hoạt động, Oracle không tự sinh mã chính nên báo lỗi.");
            System.err.println("\nGiải pháp khắc phục:");
            System.err.println("-> Hãy copy toàn bộ mã nguồn trong file 'Database/01_table/TaoMaTuDong.sql'");
            System.err.println("-> Mở công cụ quản trị Oracle (SQL Developer, PL/SQL Developer, DBeaver,...)");
            System.err.println("-> Kết nối vào tài khoản CSDL của ứng dụng (ADMIN/...) và chạy toàn bộ mã lệnh.");
            System.err.println("=======================================================================\n");
        }
        
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
