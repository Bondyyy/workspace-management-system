package com.wms.model.TrangChuQuanLy.QuanLyHoaDon;

public class DichVuDaDungDTO {
    private String tenDichVu;
    private int soLuong;
    private double donGia;
    private double thanhTien;

    public DichVuDaDungDTO() {}

    public DichVuDaDungDTO(String tenDichVu, int soLuong, double donGia) {
        this.tenDichVu = tenDichVu;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = soLuong * donGia;
    }

    public DichVuDaDungDTO(String tenDichVu, int soLuong, double donGia, double thanhTien) {
        this.tenDichVu = tenDichVu;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; this.thanhTien = this.soLuong * this.donGia;}
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; this.thanhTien = this.soLuong * this.donGia;}
    public double getThanhTien() { return thanhTien; }
}
