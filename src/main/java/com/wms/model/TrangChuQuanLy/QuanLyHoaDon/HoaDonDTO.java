package com.wms.model.TrangChuQuanLy.QuanLyHoaDon;

public class HoaDonDTO {
    private String maHoaDon;
    private String soHD;
    private Double tongTien;
    private Double thanhTien;
    private java.sql.Timestamp ngayLapHoaDon;
    private String phuongThucThanhToan;
    private String trangThaiThanhToan;
    private String maPhien;
    private String maPGG;
    private String maNV;
    private String hoTenKH;
    private String maDatCho;
    private String trangThaiPhien;
    private java.sql.Timestamp thoiGianBatDauPhien;
    private java.sql.Timestamp thoiGianKetThucPhien;
    private java.sql.Timestamp thoiGianDuKienKetThucPhien;
    private double soTienDaTraTruoc;

    public HoaDonDTO() {
    }

    public java.sql.Timestamp getThoiGianDuKienKetThucPhien() {
        return thoiGianDuKienKetThucPhien;
    }

    public void setThoiGianDuKienKetThucPhien(java.sql.Timestamp thoiGianDuKienKetThucPhien) {
        this.thoiGianDuKienKetThucPhien = thoiGianDuKienKetThucPhien;
    }

    public java.sql.Timestamp getThoiGianKetThucPhien() {
        return thoiGianKetThucPhien;
    }

    public void setThoiGianKetThucPhien(java.sql.Timestamp thoiGianKetThucPhien) {
        this.thoiGianKetThucPhien = thoiGianKetThucPhien;
    }

    public String getMaDatCho() {
        return maDatCho;
    }

    public void setMaDatCho(String maDatCho) {
        this.maDatCho = maDatCho;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getSoHD() {
        return soHD;
    }

    public void setSoHD(String soHD) {
        this.soHD = soHD;
    }

    public Double getTongTien() {
        return tongTien;
    }

    public void setTongTien(Double tongTien) {
        this.tongTien = tongTien;
    }

    public Double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(Double thanhTien) {
        this.thanhTien = thanhTien;
    }

    public java.sql.Timestamp getNgayLapHoaDon() {
        return ngayLapHoaDon;
    }

    public void setNgayLapHoaDon(java.sql.Timestamp ngayLapHoaDon) {
        this.ngayLapHoaDon = ngayLapHoaDon;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public void setTrangThaiThanhToan(String trangThaiThanhToan) {
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public String getMaPhien() {
        return maPhien;
    }

    public void setMaPhien(String maPhien) {
        this.maPhien = maPhien;
    }

    public String getMaPGG() {
        return maPGG;
    }

    public void setMaPGG(String maPGG) {
        this.maPGG = maPGG;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getHoTenKH() {
        return hoTenKH;
    }

    public void setHoTenKH(String hoTenKH) {
        this.hoTenKH = hoTenKH;
    }

    public String getTrangThaiPhien() {
        return trangThaiPhien;
    }

    public void setTrangThaiPhien(String trangThaiPhien) {
        this.trangThaiPhien = trangThaiPhien;
    }

    public java.sql.Timestamp getThoiGianBatDauPhien() {
        return thoiGianBatDauPhien;
    }

    public void setThoiGianBatDauPhien(java.sql.Timestamp thoiGianBatDauPhien) {
        this.thoiGianBatDauPhien = thoiGianBatDauPhien;
    }

    public double getSoTienDaTraTruoc() {
        return soTienDaTraTruoc;
    }

    public void setSoTienDaTraTruoc(double soTienDaTraTruoc) {
        this.soTienDaTraTruoc = soTienDaTraTruoc;
    }
}
