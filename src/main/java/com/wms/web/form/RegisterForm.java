package com.wms.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterForm {
    @NotBlank(message = "Vui lòng nhập họ tên.")
    private String fullName;

    @NotBlank(message = "Vui lòng nhập tên tài khoản.")
    private String username;

    @Email(message = "Email không hợp lệ.")
    @NotBlank(message = "Vui lòng nhập email.")
    private String email;

    @NotBlank(message = "Vui lòng nhập mật khẩu.")
    @Size(min = 6, message = "Mật khẩu cần ít nhất 6 ký tự.")
    private String password;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu.")
    private String confirmPassword;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
