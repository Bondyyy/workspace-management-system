package com.wms.util;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.util.Locale;

public final class ErrorMessageUtil {
    private static final String GENERIC_ERROR = "Đã có lỗi xảy ra, vui lòng thử lại.";

    private ErrorMessageUtil() {
    }

    public static String toUserMessage(Throwable ex) {
        if (ex == null) {
            return GENERIC_ERROR;
        }
        Throwable current = ex;
        while (current != null) {
            if (current instanceof SQLException sqlException) {
                return mapSqlException(sqlException);
            }
            current = current.getCause();
        }
        return toUserMessage(ex.getMessage());
    }

    public static String toUserMessage(String message) {
        if (message == null || message.isBlank()) {
            return GENERIC_ERROR;
        }
        String trimmed = message.trim();
        String procedureMessage = extractFriendlyProcedureMessage(trimmed);
        if (procedureMessage != null) {
            return procedureMessage;
        }
        if (!containsTechnicalDetails(trimmed)) {
            return trimmed;
        }
        return mapTechnicalMessage(trimmed);
    }

    public static boolean containsTechnicalDetails(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String upper = message.toUpperCase(Locale.ROOT);
        return upper.contains("ORA-")
                || upper.contains("SQL")
                || upper.contains("JDBC")
                || upper.contains("STACKTRACE")
                || upper.contains("EXCEPTION")
                || upper.contains("CONSTRAINT")
                || upper.contains("PL/SQL")
                || upper.contains("JAVA.")
                || upper.contains("NULLPOINTER")
                || upper.contains("DATABASE/SYSTEM ERROR");
    }

    private static String mapSqlException(SQLException ex) {
        String message = ex.getMessage() == null ? "" : ex.getMessage();
        int code = ex.getErrorCode();
        if (ex instanceof SQLIntegrityConstraintViolationException && code == 1) {
            return mapUniqueConstraint(message);
        }
        if (code == 0 && isConnectionError(message)) {
            return "Không thể kết nối cơ sở dữ liệu. Vui lòng kiểm tra kết nối và thử lại.";
        }
        return mapOracleCode(code, message);
    }

    private static String mapTechnicalMessage(String message) {
        String upper = message.toUpperCase(Locale.ROOT);
        String procedureMessage = extractFriendlyProcedureMessage(message);
        if (procedureMessage != null) {
            return procedureMessage;
        }
        if (isConnectionError(message)) {
            return "Không thể kết nối cơ sở dữ liệu. Vui lòng kiểm tra kết nối và thử lại.";
        }
        if (upper.contains("ORA-00001")) {
            return mapUniqueConstraint(message);
        }
        if (upper.contains("ORA-01400")) {
            return mapNullField(message);
        }
        if (upper.contains("ORA-02290")) {
            return mapCheckConstraint(message);
        }
        if (upper.contains("ORA-02291")) {
            return "Dữ liệu liên kết không hợp lệ hoặc đã bị xóa. Vui lòng tải lại danh sách và chọn lại.";
        }
        if (upper.contains("ORA-02292")) {
            return "Không thể xóa dữ liệu này vì đang được sử dụng ở dữ liệu khác.";
        }
        if (upper.contains("ORA-06502")) {
            return "Dữ liệu nhập không đúng định dạng hoặc vượt quá giới hạn cho phép.";
        }
        if (upper.contains("ORA-01422")) {
            return "Dữ liệu hệ thống đang bị trùng, vui lòng kiểm tra lại dữ liệu liên quan.";
        }
        if (upper.contains("ORA-12838")) {
            return "Giao dịch dữ liệu chưa hoàn tất. Vui lòng lưu lại hoặc tải lại màn hình rồi thử lại.";
        }
        if (upper.contains("ORA-12899")) {
            return "Dữ liệu bạn nhập quá dài so với giới hạn cho phép.";
        }
        return GENERIC_ERROR;
    }

    private static String mapOracleCode(int code, String message) {
        return switch (code) {
            case 1 -> mapUniqueConstraint(message);
            case 1400 -> mapNullField(message);
            case 2290 -> mapCheckConstraint(message);
            case 2291 -> "Dữ liệu liên kết không hợp lệ hoặc đã bị xóa. Vui lòng tải lại danh sách và chọn lại.";
            case 2292 -> "Không thể xóa dữ liệu này vì đang được sử dụng ở dữ liệu khác.";
            case 6502 -> "Dữ liệu nhập không đúng định dạng hoặc vượt quá giới hạn cho phép.";
            case 1422 -> "Dữ liệu hệ thống đang bị trùng, vui lòng kiểm tra lại dữ liệu liên quan.";
            case 12838 -> "Giao dịch dữ liệu chưa hoàn tất. Vui lòng lưu lại hoặc tải lại màn hình rồi thử lại.";
            case 12899 -> "Dữ liệu bạn nhập quá dài so với giới hạn cho phép.";
            default -> mapTechnicalMessage(message);
        };
    }

    private static String mapUniqueConstraint(String message) {
        String upper = message == null ? "" : message.toUpperCase(Locale.ROOT);
        if (upper.contains("UNIQUE_ND_SDT") || upper.contains("SDT")) {
            return "Số điện thoại này đã được sử dụng.";
        }
        if (upper.contains("UNIQUE_ND_EMAIL") || upper.contains("EMAIL")) {
            return "Email này đã được sử dụng.";
        }
        if (upper.contains("UNIQUE_ND_TENTAIKHOAN") || upper.contains("TENTAIKHOAN")) {
            return "Tên tài khoản này đã tồn tại.";
        }
        if (upper.contains("UK_KHACHHANG_MAND")) {
            return "Người dùng này đã có hồ sơ khách hàng.";
        }
        if (upper.contains("UK_NHANVIEN_MAND")) {
            return "Người dùng này đã có hồ sơ nhân viên.";
        }
        if (upper.contains("UQ_HOADON_MAPHIEN")) {
            return "Phiên này đã có hóa đơn, không thể tạo thêm.";
        }
        return "Dữ liệu này đã tồn tại. Vui lòng kiểm tra lại thông tin.";
    }

    private static String mapNullField(String message) {
        String upper = message == null ? "" : message.toUpperCase(Locale.ROOT);
        if (upper.contains("LUONGCOBAN") || upper.contains("LUONG")) {
            return "Vui lòng nhập tiền lương.";
        }
        if (upper.contains("DONGIA") || upper.contains("GIANHAP")) {
            return "Vui lòng nhập đơn giá.";
        }
        if (upper.contains("SOLUONG") || upper.contains("SLTOIDA")) {
            return "Vui lòng nhập số lượng.";
        }
        if (upper.contains("MACN")) {
            return "Vui lòng chọn chi nhánh.";
        }
        if (upper.contains("MALOAIDV")) {
            return "Vui lòng chọn loại dịch vụ.";
        }
        if (upper.contains("MALOAIKG")) {
            return "Vui lòng chọn loại không gian.";
        }
        if (upper.contains("TENDV")) {
            return "Vui lòng nhập tên dịch vụ.";
        }
        if (upper.contains("TENKG")) {
            return "Vui lòng nhập tên không gian.";
        }
        if (upper.contains("TENCN")) {
            return "Vui lòng nhập tên chi nhánh.";
        }
        return "Vui lòng nhập đầy đủ thông tin bắt buộc.";
    }

    private static String mapCheckConstraint(String message) {
        String upper = message == null ? "" : message.toUpperCase(Locale.ROOT);
        if (upper.contains("LUONG")) {
            return "Tiền lương không được âm.";
        }
        if (upper.contains("DONGIA") || upper.contains("GIA")) {
            return "Đơn giá không được âm.";
        }
        if (upper.contains("SOLUONG") || upper.contains("SL")) {
            return "Số lượng nhập không hợp lệ.";
        }
        if (upper.contains("TRANGTHAI")) {
            return "Trạng thái không hợp lệ. Vui lòng chọn lại.";
        }
        return "Dữ liệu nhập không hợp lệ. Vui lòng kiểm tra lại các trường trạng thái/số tiền/số lượng.";
    }

    private static boolean isConnectionError(String message) {
        String upper = message == null ? "" : message.toUpperCase(Locale.ROOT);
        return upper.contains("IO ERROR")
                || upper.contains("NETWORK ADAPTER")
                || upper.contains("CONNECTION")
                || upper.contains("CONNECTION REFUSED")
                || upper.contains("SOCKET")
                || upper.contains("ORA-01017")
                || upper.contains("ORA-125")
                || upper.contains("HIKARI");
    }

    private static String extractFriendlyProcedureMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String[] lines = message.split("\\R");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches("(?i)^ORA-20\\d{3}:.*")) {
                String body = trimmed.replaceFirst("(?i)^ORA-20\\d{3}:\\s*", "").trim();
                if (!body.isBlank() && !containsProcedureTechnicalDetails(body)) {
                    return body;
                }
            }
        }
        return null;
    }

    private static boolean containsProcedureTechnicalDetails(String message) {
        String upper = message.toUpperCase(Locale.ROOT);
        return upper.contains("ORA-")
                || upper.contains("SQL")
                || upper.contains("CONSTRAINT")
                || upper.contains("TRIGGER")
                || upper.contains("PROCEDURE")
                || upper.contains("TABLE")
                || upper.contains("COLUMN")
                || upper.contains("STACK");
    }
}
