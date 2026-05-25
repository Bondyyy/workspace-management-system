package com.wms.web.util;

import com.wms.util.ErrorMessageUtil;

public final class WebErrorMessages {

    private WebErrorMessages() {
    }

    public static String thanThien(String fallback, RuntimeException ex) {
        if (ex == null || ex.getMessage() == null || ex.getMessage().isBlank()) {
            return fallback;
        }
        String message = ex.getMessage().trim();
        if (ErrorMessageUtil.containsTechnicalDetails(message)) {
            System.err.println("[Web] " + fallback + " Loi ky thuat: " + message);
            String mapped = ErrorMessageUtil.toUserMessage(ex);
            return mapped == null || mapped.isBlank() ? fallback : mapped;
        }
        return ErrorMessageUtil.toUserMessage(message);
    }
}
