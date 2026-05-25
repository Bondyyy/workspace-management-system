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

    public static void showError(Component parent, Throwable t) {
        showError(parent, null, t);
    }

    public static void showError(Component parent, String message, Throwable t) {
        if (t != null) {
            t.printStackTrace(System.err);
        } else if (message != null && !message.isBlank()) {
            System.err.println("[DATABASE/SYSTEM ERROR]: " + message);
        }

        String friendlyMessage = ErrorMessageUtil.toUserMessage(message);
        if ((message == null || message.isBlank() || ErrorMessageUtil.containsTechnicalDetails(message)) && t != null) {
            friendlyMessage = ErrorMessageUtil.toUserMessage(t);
        }
        JOptionPane.showMessageDialog(parent, friendlyMessage, "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        int choice = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

}
