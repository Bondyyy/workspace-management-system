package com.wms.web.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PendingRegistration implements Serializable {
    private final String fullName;
    private final String username;
    private final String email;
    private final String hashedPassword;
    private final String otp;
    private final LocalDateTime expiresAt;

    public PendingRegistration(String fullName, String username, String email,
                               String hashedPassword, String otp, LocalDateTime expiresAt) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.otp = otp;
        this.expiresAt = expiresAt;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean matches(String submittedOtp) {
        return otp.equals(submittedOtp);
    }
}
