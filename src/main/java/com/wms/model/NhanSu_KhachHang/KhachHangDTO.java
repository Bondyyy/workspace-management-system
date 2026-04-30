package com.wms.model.NhanSu_KhachHang;

public class KhachHangDTO {
    private String maKH;
    private String hoTenKH;
    private String loaiKH;
    private Double tongChiTieu;
    private java.sql.Timestamp capNhatLanCuoi;
    private String maHangThanhVien;
    private String maND;

    public KhachHangDTO() {}

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getHoTenKH() {
        return hoTenKH;
    }

    public void setHoTenKH(String hoTenKH) {
        this.hoTenKH = hoTenKH;
    }

    public String getLoaiKH() {
        return loaiKH;
    }

    public void setLoaiKH(String loaiKH) {
        this.loaiKH = loaiKH;
    }

    public Double getTongChiTieu() {
        return tongChiTieu;
    }

    public void setTongChiTieu(Double tongChiTieu) {
        this.tongChiTieu = tongChiTieu;
    }

    public java.sql.Timestamp getCapNhatLanCuoi() {
        return capNhatLanCuoi;
    }

    public void setCapNhatLanCuoi(java.sql.Timestamp capNhatLanCuoi) {
        this.capNhatLanCuoi = capNhatLanCuoi;
    }

    public String getMaHangThanhVien() {
        return maHangThanhVien;
    }

    public void setMaHangThanhVien(String maHangThanhVien) {
        this.maHangThanhVien = maHangThanhVien;
    }

    public String getMaND() {
        return maND;
    }

    public void setMaND(String maND) {
        this.maND = maND;
    }

}
