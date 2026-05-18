package com.wms.web.model;

import java.time.LocalDate;

public class AccountProfileView {
    private final String maND;
    private final String hoTen;
    private final String tenTaiKhoan;
    private final String email;
    private final String soDienThoai;
    private final LocalDate ngaySinh;
    private final String gioiTinh;
    private final String hangThanhVien;

    public AccountProfileView(String maND, String hoTen, String tenTaiKhoan, String email,
                              String soDienThoai, LocalDate ngaySinh, String gioiTinh,
                              String hangThanhVien) {
        this.maND = maND;
        this.hoTen = hoTen;
        this.tenTaiKhoan = tenTaiKhoan;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.hangThanhVien = hangThanhVien;
    }

    public String getMaND() {
        return maND;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getTenTaiKhoan() {
        return tenTaiKhoan;
    }

    public String getEmail() {
        return email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public String getHangThanhVien() {
        return hangThanhVien;
    }

    public boolean hasCompleteContactInfo() {
        return !isBlank(email) && !isBlank(soDienThoai);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
