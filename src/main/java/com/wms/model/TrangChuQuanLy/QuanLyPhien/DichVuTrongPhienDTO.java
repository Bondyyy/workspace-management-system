package com.wms.model.TrangChuQuanLy.QuanLyPhien;

public class DichVuTrongPhienDTO {
    private String tenDV;
    private int soLuong;
    private double donGia;
    private double thanhTien;

    public DichVuTrongPhienDTO() {
    }

    public DichVuTrongPhienDTO(String tenDV, int soLuong, double donGia, double thanhTien) {
        this.tenDV = tenDV;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public String getTenDV() {
        return tenDV;
    }

    public void setTenDV(String tenDV) {
        this.tenDV = tenDV;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }
}
