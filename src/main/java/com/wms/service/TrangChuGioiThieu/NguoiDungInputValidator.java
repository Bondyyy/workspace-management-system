package com.wms.service.TrangChuGioiThieu;

import java.util.regex.Pattern;

final class NguoiDungInputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private NguoiDungInputValidator() {
    }

    static boolean isRegistrationValid(String tenTaiKhoan, String hoTen, String email, String matKhau) {
        return !isBlank(tenTaiKhoan)
                && !isBlank(hoTen)
                && !isBlank(email)
                && !isBlank(matKhau)
                && isEmailValid(email);
    }

    static boolean isOtpRequestValid(String tenTaiKhoan, String email) {
        return !isBlank(tenTaiKhoan) && !isBlank(email) && isEmailValid(email);
    }

    static boolean isEmailOnlyRequestValid(String email) {
        return !isBlank(email) && isEmailValid(email);
    }

    static boolean isResetPasswordValid(String email, String matKhauMoi) {
        return !isBlank(email) && !isBlank(matKhauMoi) && isEmailValid(email);
    }

    static boolean isEmailValid(String email) {
        return !isBlank(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
