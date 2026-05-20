package com.wms.model.TrangChuQuanLy.QuanLyPhien;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ThongTinXacNhanDatChoDTO {
    private String maPhien;
    private String maDatCho;
    private String maQR;
    private String hoTen;
    private String email;
    private String tenKhongGian;
    private String tenChiNhanh;
    private Timestamp thoiGianBatDau;
    private Timestamp thoiGianDuKienKetThuc;
    private BigDecimal thanhTien;

    public String getMaPhien() {
        return maPhien;
    }

    public void setMaPhien(String maPhien) {
        this.maPhien = maPhien;
    }

    public String getMaDatCho() {
        return maDatCho;
    }

    public void setMaDatCho(String maDatCho) {
        this.maDatCho = maDatCho;
    }

    public String getMaQR() {
        return maQR;
    }

    public void setMaQR(String maQR) {
        this.maQR = maQR;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTenKhongGian() {
        return tenKhongGian;
    }

    public void setTenKhongGian(String tenKhongGian) {
        this.tenKhongGian = tenKhongGian;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
        this.tenChiNhanh = tenChiNhanh;
    }

    public Timestamp getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(Timestamp thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public Timestamp getThoiGianDuKienKetThuc() {
        return thoiGianDuKienKetThuc;
    }

    public void setThoiGianDuKienKetThuc(Timestamp thoiGianDuKienKetThuc) {
        this.thoiGianDuKienKetThuc = thoiGianDuKienKetThuc;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }
}
