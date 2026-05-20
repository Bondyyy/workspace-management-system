package com.wms.model.TrangChuQuanLy.QuanLyKhongGian;

public class LoaiKhongGianDTO {
    private String maLoaiKG;
    private String tenLoaiKG;
    private Integer sucChua;
    private Double donGiaTheoGio;
    private String trangThai;

    public LoaiKhongGianDTO() {}

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMaLoaiKG() {
        return maLoaiKG;
    }

    public void setMaLoaiKG(String maLoaiKG) {
        this.maLoaiKG = maLoaiKG;
    }

    public String getTenLoaiKG() {
        return tenLoaiKG;
    }

    public void setTenLoaiKG(String tenLoaiKG) {
        this.tenLoaiKG = tenLoaiKG;
    }

    public Integer getSucChua() {
        return sucChua;
    }

    public void setSucChua(Integer sucChua) {
        this.sucChua = sucChua;
    }

    public Double getDonGiaTheoGio() {
        return donGiaTheoGio;
    }

    public void setDonGiaTheoGio(Double donGiaTheoGio) {
        this.donGiaTheoGio = donGiaTheoGio;
    }

}
