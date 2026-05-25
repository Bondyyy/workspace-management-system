package com.wms.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DatLaiMatKhauWebForm {
    @NotBlank(message = "Vui lòng nhập mã OTP.")
    @Pattern(regexp = "\\d{6}", message = "Mã OTP gồm 6 chữ số.")
    private String otp;

    @NotBlank(message = "Vui lòng nhập mật khẩu mới.")
    @Size(min = 6, message = "Mật khẩu cần ít nhất 6 ký tự.")
    private String matKhauMoi;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu mới.")
    private String xacNhanMatKhauMoi;

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getMatKhauMoi() {
        return matKhauMoi;
    }

    public void setMatKhauMoi(String matKhauMoi) {
        this.matKhauMoi = matKhauMoi;
    }

    public String getXacNhanMatKhauMoi() {
        return xacNhanMatKhauMoi;
    }

    public void setXacNhanMatKhauMoi(String xacNhanMatKhauMoi) {
        this.xacNhanMatKhauMoi = xacNhanMatKhauMoi;
    }
}
