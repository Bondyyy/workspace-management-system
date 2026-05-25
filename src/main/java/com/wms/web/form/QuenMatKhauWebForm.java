package com.wms.web.form;

import jakarta.validation.constraints.NotBlank;

public class QuenMatKhauWebForm {
    @NotBlank(message = "Vui lòng nhập email hoặc tên tài khoản.")
    private String dinhDanh;

    public String getDinhDanh() {
        return dinhDanh;
    }

    public void setDinhDanh(String dinhDanh) {
        this.dinhDanh = dinhDanh;
    }
}
