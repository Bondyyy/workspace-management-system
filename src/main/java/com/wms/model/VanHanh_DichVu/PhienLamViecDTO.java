package com.wms.model.VanHanh_DichVu;

public class PhienLamViecDTO {
    private String maPhien;
    private java.sql.Timestamp thoiGianBatDau;
    private java.sql.Timestamp thoiGianDuKienKetThuc;
    private String trangThaiPhien;
    private java.sql.Timestamp thoiGianKetThuc;
    private java.sql.Timestamp capNhatLanCuoi;
    private String maKG;
    private String maKH;
    private String maDatCho;

    public PhienLamViecDTO() {}

    public String getMaPhien() {
        return maPhien;
    }

    public void setMaPhien(String maPhien) {
        this.maPhien = maPhien;
    }

    public java.sql.Timestamp getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(java.sql.Timestamp thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public java.sql.Timestamp getThoiGianDuKienKetThuc() {
        return thoiGianDuKienKetThuc;
    }

    public void setThoiGianDuKienKetThuc(java.sql.Timestamp thoiGianDuKienKetThuc) {
        this.thoiGianDuKienKetThuc = thoiGianDuKienKetThuc;
    }

    public String getTrangThaiPhien() {
        return trangThaiPhien;
    }

    public void setTrangThaiPhien(String trangThaiPhien) {
        this.trangThaiPhien = trangThaiPhien;
    }

    public java.sql.Timestamp getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(java.sql.Timestamp thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public java.sql.Timestamp getCapNhatLanCuoi() {
        return capNhatLanCuoi;
    }

    public void setCapNhatLanCuoi(java.sql.Timestamp capNhatLanCuoi) {
        this.capNhatLanCuoi = capNhatLanCuoi;
    }

    public String getMaKG() {
        return maKG;
    }

    public void setMaKG(String maKG) {
        this.maKG = maKG;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getMaDatCho() {
        return maDatCho;
    }

    public void setMaDatCho(String maDatCho) {
        this.maDatCho = maDatCho;
    }

}
