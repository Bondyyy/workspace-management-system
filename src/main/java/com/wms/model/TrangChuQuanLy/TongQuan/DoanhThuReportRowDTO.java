package com.wms.model.TrangChuQuanLy.TongQuan;

public class DoanhThuReportRowDTO {

    private String maHoaDon;
    private String ngayLap;
    private String tenKhachHang;
    private String tenChiNhanh;
    private String tenKhongGian;
    private String tongTien;
    private String thanhTien;
    private String phuongThucThanhToan;
    private String trangThaiThanhToan;

    public DoanhThuReportRowDTO() {
    }

    public DoanhThuReportRowDTO(String maHoaDon, String ngayLap, String tenKhachHang, String tenChiNhanh,
                                String tenKhongGian, String tongTien, String thanhTien,
                                String phuongThucThanhToan, String trangThaiThanhToan) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.tenKhachHang = tenKhachHang;
        this.tenChiNhanh = tenChiNhanh;
        this.tenKhongGian = tenKhongGian;
        this.tongTien = tongTien;
        this.thanhTien = thanhTien;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(String ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
        this.tenChiNhanh = tenChiNhanh;
    }

    public String getTenKhongGian() {
        return tenKhongGian;
    }

    public void setTenKhongGian(String tenKhongGian) {
        this.tenKhongGian = tenKhongGian;
    }

    public String getTongTien() {
        return tongTien;
    }

    public void setTongTien(String tongTien) {
        this.tongTien = tongTien;
    }

    public String getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(String thanhTien) {
        this.thanhTien = thanhTien;
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
}
