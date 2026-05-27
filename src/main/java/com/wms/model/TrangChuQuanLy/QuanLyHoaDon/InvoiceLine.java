package com.wms.model.TrangChuQuanLy.QuanLyHoaDon;

public class InvoiceLine {
    private String noiDung;
    private int soLuong;
    private double donGia;
    private double thanhTien;
    private boolean datTruoc;

    public InvoiceLine() {
    }

    public InvoiceLine(String noiDung, int soLuong, double donGia, double thanhTien, boolean datTruoc) {
        this.noiDung = noiDung;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
        this.datTruoc = datTruoc;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
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

    public boolean isDatTruoc() {
        return datTruoc;
    }

    public void setDatTruoc(boolean datTruoc) {
        this.datTruoc = datTruoc;
    }
}
