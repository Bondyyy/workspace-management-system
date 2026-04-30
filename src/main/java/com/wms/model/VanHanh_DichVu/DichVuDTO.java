package com.wms.model.VanHanh_DichVu;

public class DichVuDTO {
    private String maDV;
    private String tenDV;
    private String hinhAnh;
    private String trangThaiDV;
    private Double donGia;
    private String maLoaiDV;

    public DichVuDTO() {}

    public String getMaDV() {
        return maDV;
    }

    public void setMaDV(String maDV) {
        this.maDV = maDV;
    }

    public String getTenDV() {
        return tenDV;
    }

    public void setTenDV(String tenDV) {
        this.tenDV = tenDV;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getTrangThaiDV() {
        return trangThaiDV;
    }

    public void setTrangThaiDV(String trangThaiDV) {
        this.trangThaiDV = trangThaiDV;
    }

    public Double getDonGia() {
        return donGia;
    }

    public void setDonGia(Double donGia) {
        this.donGia = donGia;
    }

    public String getMaLoaiDV() {
        return maLoaiDV;
    }

    public void setMaLoaiDV(String maLoaiDV) {
        this.maLoaiDV = maLoaiDV;
    }

}
