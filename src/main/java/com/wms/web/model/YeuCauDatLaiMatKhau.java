package com.wms.web.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class YeuCauDatLaiMatKhau implements Serializable {
    private final String maND;
    private final String email;
    private final String otp;
    private final LocalDateTime expiresAt;

    public YeuCauDatLaiMatKhau(String maND, String email, String otp, LocalDateTime expiresAt) {
        this.maND = maND;
        this.email = email;
        this.otp = otp;
        this.expiresAt = expiresAt;
    }

    public String getMaND() {
        return maND;
    }

    public String getEmail() {
        return email;
    }

    public boolean daHetHan() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean khopMa(String submittedOtp) {
        return otp != null && otp.equals(submittedOtp);
    }
}
