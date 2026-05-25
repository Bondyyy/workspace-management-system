package com.wms.web.util;

public final class WebErrorMessages {

    private WebErrorMessages() {
    }

    public static String thanThien(String fallback, RuntimeException ex) {
        if (ex == null || ex.getMessage() == null || ex.getMessage().isBlank()) {
            return fallback;
        }
        String message = ex.getMessage().trim();
        if (laThongBaoKyThuat(message)) {
            System.err.println("[Web] " + fallback + " Loi ky thuat: " + message);
            return fallback;
        }
        return message;
    }

    private static boolean laThongBaoKyThuat(String message) {
        String upper = message.toUpperCase();
        return upper.contains("ORA-")
                || upper.contains("SQL")
                || upper.contains("JDBC")
                || upper.contains("STACKTRACE")
                || upper.contains("EXCEPTION")
                || upper.contains("CONSTRAINT")
                || upper.contains("NULLPOINTER")
                || upper.contains("PL/SQL");
    }
}
