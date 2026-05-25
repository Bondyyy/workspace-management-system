package com.wms.web.util;

import com.wms.util.ErrorMessageUtil;

public final class WebErrorMessages {
    public static final String LOGIN_FAILED_MESSAGE = "Tên đăng nhập hoặc mật khẩu không đúng.";
    public static final String LOGIN_INACTIVE_MESSAGE = "Tài khoản hiện không hoạt động.";
    public static final String LOGIN_SYSTEM_ERROR_MESSAGE = "Không thể đăng nhập lúc này. Vui lòng thử lại sau.";

    private WebErrorMessages() {
    }

    public static String dangNhap(RuntimeException ex) {
        if (ex == null || ex.getMessage() == null || ex.getMessage().isBlank()) {
            return LOGIN_SYSTEM_ERROR_MESSAGE;
        }
        String message = ex.getMessage().trim();
        if (LOGIN_INACTIVE_MESSAGE.equals(message)) {
            return LOGIN_INACTIVE_MESSAGE;
        }
        if (LOGIN_FAILED_MESSAGE.equals(message)
                || "Không tìm thấy tài khoản.".equals(message)
                || "Mật khẩu không đúng.".equals(message)) {
            return LOGIN_FAILED_MESSAGE;
        }
        if (ErrorMessageUtil.containsTechnicalDetails(message)) {
            System.err.println("[Web] " + LOGIN_SYSTEM_ERROR_MESSAGE + " Loi ky thuat: " + sanitizeForLog(message));
        }
        return LOGIN_SYSTEM_ERROR_MESSAGE;
    }

    public static String thanThien(String fallback, RuntimeException ex) {
        if (ex == null || ex.getMessage() == null || ex.getMessage().isBlank()) {
            return fallback;
        }
        String message = ex.getMessage().trim();
        if (ErrorMessageUtil.containsTechnicalDetails(message)) {
            System.err.println("[Web] " + fallback + " Loi ky thuat: " + sanitizeForLog(message));
            String mapped = ErrorMessageUtil.toUserMessage(ex);
            return mapped == null || mapped.isBlank() ? fallback : mapped;
        }
        return ErrorMessageUtil.toUserMessage(message);
    }

    private static String sanitizeForLog(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }
        return message
                .replaceAll("(?i)(password|matkhau|mat_khau|matkhaumahoa|token|secret|hash)\\s*[:=]\\s*\\S+",
                        "$1=[REDACTED]")
                .replaceAll("(?i)(mật\\s*khẩu)\\s*[:=]\\s*\\S+", "$1=[REDACTED]");
    }
}
