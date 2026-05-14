package com.wms.web.form;

import jakarta.validation.constraints.NotBlank;

public class LoginForm {
    @NotBlank(message = "Vui lòng nhập tài khoản, email hoặc số điện thoại.")
    private String username;

    @NotBlank(message = "Vui lòng nhập mật khẩu.")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
