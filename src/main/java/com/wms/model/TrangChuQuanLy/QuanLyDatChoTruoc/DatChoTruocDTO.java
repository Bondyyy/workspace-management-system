package com.wms.model.TrangChuQuanLy.QuanLyDatChoTruoc;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class DatChoTruocDTO {
    private String maDatCho;
    private String maKH;
    private String hoTenKhachHang;
    private String maKG;
    private String tenKhongGian;
    private String tenChiNhanh;
    private String tenLoaiKhongGian;
    private String emailKhachHang;
    private String soDienThoaiKhachHang;
    private String maQR;
    private Timestamp thoiGianDuKienToi;
    private Integer khoangThoiGianSuDung;
    private String trangThaiDatTruoc;
    private BigDecimal thanhTien;
    private String ghiChu;

    public String getMaDatCho() {
        return maDatCho;
    }

    public void setMaDatCho(String maDatCho) {
        this.maDatCho = maDatCho;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getHoTenKhachHang() {
        return hoTenKhachHang;
    }

    public void setHoTenKhachHang(String hoTenKhachHang) {
        this.hoTenKhachHang = hoTenKhachHang;
    }

    public String getMaKG() {
        return maKG;
    }

    public void setMaKG(String maKG) {
        this.maKG = maKG;
    }

    public String getTenKhongGian() {
        return tenKhongGian;
    }

    public void setTenKhongGian(String tenKhongGian) {
        this.tenKhongGian = tenKhongGian;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
        this.tenChiNhanh = tenChiNhanh;
    }

    public String getTenLoaiKhongGian() {
        return tenLoaiKhongGian;
    }

    public void setTenLoaiKhongGian(String tenLoaiKhongGian) {
        this.tenLoaiKhongGian = tenLoaiKhongGian;
    }

    public String getEmailKhachHang() {
        return emailKhachHang;
    }

    public void setEmailKhachHang(String emailKhachHang) {
        this.emailKhachHang = emailKhachHang;
    }

    public String getSoDienThoaiKhachHang() {
        return soDienThoaiKhachHang;
    }

    public void setSoDienThoaiKhachHang(String soDienThoaiKhachHang) {
        this.soDienThoaiKhachHang = soDienThoaiKhachHang;
    }

    public String getMaQR() {
        return maQR;
    }

    public void setMaQR(String maQR) {
        this.maQR = maQR;
    }

    public Timestamp getThoiGianDuKienToi() {
        return thoiGianDuKienToi;
    }

    public void setThoiGianDuKienToi(Timestamp thoiGianDuKienToi) {
        this.thoiGianDuKienToi = thoiGianDuKienToi;
    }

    public Integer getKhoangThoiGianSuDung() {
        return khoangThoiGianSuDung;
    }

    public void setKhoangThoiGianSuDung(Integer khoangThoiGianSuDung) {
        this.khoangThoiGianSuDung = khoangThoiGianSuDung;
    }

    public String getTrangThaiDatTruoc() {
        return trangThaiDatTruoc;
    }

    public void setTrangThaiDatTruoc(String trangThaiDatTruoc) {
        this.trangThaiDatTruoc = trangThaiDatTruoc;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
