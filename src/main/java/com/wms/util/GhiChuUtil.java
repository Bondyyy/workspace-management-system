package com.wms.util;

public final class GhiChuUtil {
    private static final String SYSTEM_SEPARATOR = " | ";

    private GhiChuUtil() {
    }

    public static String layGhiChuKhachHang(String ghiChuRaw) {
        if (ghiChuRaw == null || ghiChuRaw.isBlank()) {
            return "";
        }
        String[] parts = ghiChuRaw.split("\\s*\\|\\s*");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            String trimmed = part == null ? "" : part.trim();
            if (trimmed.isBlank() || laGhiChuHeThong(trimmed)) {
                continue;
            }
            if (result.length() > 0) {
                result.append(SYSTEM_SEPARATOR);
            }
            result.append(trimmed);
        }
        return result.toString();
    }

    public static String themGhiChuHeThong(String ghiChuRaw, String systemNote) {
        String note = systemNote == null ? "" : systemNote.trim();
        if (note.isBlank()) {
            return ghiChuRaw;
        }
        String current = ghiChuRaw == null ? "" : ghiChuRaw.trim();
        if (current.contains(note)) {
            return current;
        }
        return current.isBlank() ? note : current + SYSTEM_SEPARATOR + note;
    }

    private static boolean laGhiChuHeThong(String value) {
        String normalized = boDau(value).toLowerCase();
        return normalized.contains("webhook")
                || normalized.contains("[system")
                || normalized.contains("khach da nhan cho")
                || normalized.contains("qr bi vo hieu hoa")
                || normalized.contains("nhan vien xac nhan da nhan chuyen khoan")
                || normalized.contains("tu dong ket thuc")
                || normalized.contains("thanh toan qua webhook")
                || normalized.contains("thanh toan that bai");
    }

    private static String boDau(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
    }
}
