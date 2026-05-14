package com.wms.model.TrangChuQuanLy.QuanLyDichVuDat;

public class DichVuDaDatDTO {
    private String maPhien;
    private String tenDichVu;
    private int soLuong;
    private String thoiGianDat;
    private String khachHang;
    private String tenKhongGian;
    private String ghiChu;
    private String tenLoaiDV;

    public DichVuDaDatDTO() {
    }

    public DichVuDaDatDTO(String maPhien, String tenDichVu, int soLuong, String thoiGianDat, String khachHang, String tenKhongGian, String ghiChu, String tenLoaiDV) {
        this.maPhien = maPhien;
        this.tenDichVu = tenDichVu;
        this.soLuong = soLuong;
        this.thoiGianDat = thoiGianDat;
        this.khachHang = khachHang;
        this.tenKhongGian = tenKhongGian;
        this.ghiChu = ghiChu;
        this.tenLoaiDV = tenLoaiDV;
    }

    public String getMaPhien() {
        return maPhien;
    }

    public void setMaPhien(String maPhien) {
        this.maPhien = maPhien;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getThoiGianDat() {
        return thoiGianDat;
    }

    public void setThoiGianDat(String thoiGianDat) {
        this.thoiGianDat = thoiGianDat;
    }

    public String getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(String khachHang) {
        this.khachHang = khachHang;
    }

    public String getTenKhongGian() {
        return tenKhongGian;
    }

    public void setTenKhongGian(String tenKhongGian) {
        this.tenKhongGian = tenKhongGian;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getTenLoaiDV() {
        return tenLoaiDV;
    }

    public void setTenLoaiDV(String tenLoaiDV) {
        this.tenLoaiDV = tenLoaiDV;
    }
}
