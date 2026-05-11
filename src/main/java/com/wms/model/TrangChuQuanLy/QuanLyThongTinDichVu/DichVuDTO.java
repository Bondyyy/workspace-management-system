package com.wms.model.TrangChuQuanLy.QuanLyThongTinDichVu;

public class DichVuDTO {
    private String maDV;
    private String tenDV;
    private byte[] hinhAnh;
    private String trangThaiDV;
    private double donGia;
    private String maLoaiDV;
    
    private String tenLoaiDV; 
    
    private Integer soLuong; 
    private Double giaNhap;

    public DichVuDTO() {
    }

    public String getMaDV() { return maDV; }
    public void setMaDV(String maDV) { this.maDV = maDV; }

    public String getTenDV() { return tenDV; }
    public void setTenDV(String tenDV) { this.tenDV = tenDV; }

    public byte[] getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(byte[] hinhAnh) { this.hinhAnh = hinhAnh; }

    public String getTrangThaiDV() { return trangThaiDV; }
    public void setTrangThaiDV(String trangThaiDV) { this.trangThaiDV = trangThaiDV; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public String getMaLoaiDV() { return maLoaiDV; }
    public void setMaLoaiDV(String maLoaiDV) { this.maLoaiDV = maLoaiDV; }

    public String getTenLoaiDV() { return tenLoaiDV; }
    public void setTenLoaiDV(String tenLoaiDV) { this.tenLoaiDV = tenLoaiDV; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public Double getGiaNhap() { return giaNhap; }
    public void setGiaNhap(Double giaNhap) { this.giaNhap = giaNhap; }
}
