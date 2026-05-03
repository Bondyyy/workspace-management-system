package com.wms.model;

import java.sql.Timestamp;

public class PhienLamViecFullDTO extends PhienLamViecDTO {
    private String tenKhongGian;
    private String tenKhachHang;

    public PhienLamViecFullDTO() {
    }

    public String getTenKhongGian() {
        return tenKhongGian;
    }

    public void setTenKhongGian(String tenKhongGian) {
        this.tenKhongGian = tenKhongGian;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }
}
