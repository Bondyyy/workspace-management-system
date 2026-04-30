package com.wms.model.VanHanh_DichVu;

public class ChiTietDichVuDTO {
    private String maDV;
    private String maPhien;
    private Integer soLuong;
    private String ghiChu;

    public ChiTietDichVuDTO() {}

    public String getMaDV() {
        return maDV;
    }

    public void setMaDV(String maDV) {
        this.maDV = maDV;
    }

    public String getMaPhien() {
        return maPhien;
    }

    public void setMaPhien(String maPhien) {
        this.maPhien = maPhien;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

}
