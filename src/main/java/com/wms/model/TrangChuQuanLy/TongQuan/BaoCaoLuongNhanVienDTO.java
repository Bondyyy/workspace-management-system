package com.wms.model.TrangChuQuanLy.TongQuan;

import java.util.Date;

public class BaoCaoLuongNhanVienDTO {

    private String maNV;
    private String hoTen;
    private String loaiNV;
    private String tenChiNhanh;
    private Date ngayVaoLam;
    private Double luongCoBan;
    private Double phuCap;
    private Double tienThuong;
    private int soNgayTinhLuong;
    private Double tongLuong;

    public BaoCaoLuongNhanVienDTO() {
    }

    public BaoCaoLuongNhanVienDTO(String maNV, String hoTen, String loaiNV, String tenChiNhanh, Date ngayVaoLam,
                                  Double luongCoBan, Double phuCap, Double tienThuong, int soNgayTinhLuong, Double tongLuong) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.loaiNV = loaiNV;
        this.tenChiNhanh = tenChiNhanh;
        this.ngayVaoLam = ngayVaoLam;
        this.luongCoBan = luongCoBan;
        this.phuCap = phuCap;
        this.tienThuong = tienThuong;
        this.soNgayTinhLuong = soNgayTinhLuong;
        this.tongLuong = tongLuong;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getLoaiNV() {
        return loaiNV;
    }

    public void setLoaiNV(String loaiNV) {
        this.loaiNV = loaiNV;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
        this.tenChiNhanh = tenChiNhanh;
    }

    public Date getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(Date ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
    }

    public Double getLuongCoBan() {
        return luongCoBan;
    }

    public void setLuongCoBan(Double luongCoBan) {
        this.luongCoBan = luongCoBan;
    }

    public Double getPhuCap() {
        return phuCap;
    }

    public void setPhuCap(Double phuCap) {
        this.phuCap = phuCap;
    }

    public Double getTienThuong() {
        return tienThuong;
    }

    public void setTienThuong(Double tienThuong) {
        this.tienThuong = tienThuong;
    }

    public int getSoNgayTinhLuong() {
        return soNgayTinhLuong;
    }

    public void setSoNgayTinhLuong(int soNgayTinhLuong) {
        this.soNgayTinhLuong = soNgayTinhLuong;
    }

    public Double getTongLuong() {
        return tongLuong;
    }

    public void setTongLuong(Double tongLuong) {
        this.tongLuong = tongLuong;
    }
}
