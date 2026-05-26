package com.wms.web.model;

public class NguoiDungPhien {
    private final String maND;
    private final String maKH;
    private final String maNV;
    private final String maCN;
    private final String tenCN;
    private final String hoTen;
    private final String tenTaiKhoan;
    private final boolean staff;

    public NguoiDungPhien(String maND, String maKH, String hoTen, String tenTaiKhoan, boolean staff) {
        this(maND, maKH, null, null, null, hoTen, tenTaiKhoan, staff);
    }

    public NguoiDungPhien(String maND, String maKH, String maNV, String maCN, String tenCN,
                          String hoTen, String tenTaiKhoan, boolean staff) {
        this.maND = maND;
        this.maKH = maKH;
        this.maNV = maNV;
        this.maCN = maCN;
        this.tenCN = tenCN;
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

    public String getMaNV() {
        return maNV;
    }

    public String getMaCN() {
        return maCN;
    }

    public String getTenCN() {
        return tenCN;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getTenTaiKhoan() {
        return tenTaiKhoan;
    }

    public boolean laNhanVien() {
        return staff;
    }

    public boolean isStaff() {
        return staff;
    }
}
