package com.wms.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DangKyWebForm {
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

    public String getHoTen() {
        return fullName;
    }

    public void setHoTen(String fullName) {
        this.fullName = fullName;
    }

    public String getTenTaiKhoan() {
        return username;
    }

    public void setTenTaiKhoan(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatKhau() {
        return password;
    }

    public void setMatKhau(String password) {
        this.password = password;
    }

    public String getXacNhanMatKhau() {
        return confirmPassword;
    }

    public void setXacNhanMatKhau(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
