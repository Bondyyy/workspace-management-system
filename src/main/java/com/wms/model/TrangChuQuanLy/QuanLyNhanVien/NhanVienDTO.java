package com.wms.model.TrangChuQuanLy.QuanLyNhanVien;

public class NhanVienDTO {
    private String maNV;
    private String loaiNV;
    private java.sql.Date ngayVaoLam;
    private String trangThaiLamViec;
    private Double phuCap;
    private Double tienThuong;
    private String caLamViec;
    private Double luongCoBan;
    private String maNQL;
    private String maCN;
    private String maND;

    public NhanVienDTO() {}

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getLoaiNV() {
        return loaiNV;
    }

    public void setLoaiNV(String loaiNV) {
        this.loaiNV = loaiNV;
    }

    public java.sql.Date getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(java.sql.Date ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
    }

    public String getTrangThaiLamViec() {
        return trangThaiLamViec;
    }

    public void setTrangThaiLamViec(String trangThaiLamViec) {
        this.trangThaiLamViec = trangThaiLamViec;
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

    public String getCaLamViec() {
        return caLamViec;
    }

    public void setCaLamViec(String caLamViec) {
        this.caLamViec = caLamViec;
    }

    public Double getLuongCoBan() {
        return luongCoBan;
    }

    public void setLuongCoBan(Double luongCoBan) {
        this.luongCoBan = luongCoBan;
    }

    public String getMaNQL() {
        return maNQL;
    }

    public void setMaNQL(String maNQL) {
        this.maNQL = maNQL;
    }

    public String getMaCN() {
        return maCN;
    }

    public void setMaCN(String maCN) {
        this.maCN = maCN;
    }

    public String getMaND() {
        return maND;
    }

    public void setMaND(String maND) {
        this.maND = maND;
    }

}
