package com.wms.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OtpForm {
    @NotBlank(message = "Vui lòng nhập mã OTP.")
    @Pattern(regexp = "\\d{6}", message = "Mã OTP gồm 6 chữ số.")
    private String otp;

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
