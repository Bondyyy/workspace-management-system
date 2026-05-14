package com.wms.web.model;

public class SessionUser {
    private final String maND;
    private final String maKH;
    private final String hoTen;
    private final String tenTaiKhoan;
    private final boolean staff;

    public SessionUser(String maND, String maKH, String hoTen, String tenTaiKhoan, boolean staff) {
        this.maND = maND;
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.tenTaiKhoan = tenTaiKhoan;
        this.staff = staff;
    }

    public String getMaND() {
        return maND;
    }

    public String getMaKH() {
        return maKH;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getTenTaiKhoan() {
        return tenTaiKhoan;
    }

    public boolean isStaff() {
        return staff;
    }
}
