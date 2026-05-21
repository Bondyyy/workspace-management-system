package com.wms.web.form;

import jakarta.validation.constraints.NotBlank;

public class DangNhapWebForm {
    @NotBlank(message = "Vui lòng nhập tài khoản, email hoặc số điện thoại.")
    private String username;

    @NotBlank(message = "Vui lòng nhập mật khẩu.")
    private String password;

    public String getTenTaiKhoan() {
        return username;
    }

    public void setTenTaiKhoan(String username) {
        this.username = username;
    }

    public String getMatKhau() {
        return password;
    }

    public void setMatKhau(String password) {
        this.password = password;
    }
}
