package com.wms.model.NhanSu_KhachHang;

public class HangThanhVienDTO {
    private String maHangThanhVien;
    private String tenHangThanhVien;
    private Double phanTramTienGiam;
    private Double tongChiTieuToiThieu;

    public HangThanhVienDTO() {}

    public String getMaHangThanhVien() {
        return maHangThanhVien;
    }

    public void setMaHangThanhVien(String maHangThanhVien) {
        this.maHangThanhVien = maHangThanhVien;
    }

    public String getTenHangThanhVien() {
        return tenHangThanhVien;
    }

    public void setTenHangThanhVien(String tenHangThanhVien) {
        this.tenHangThanhVien = tenHangThanhVien;
    }

    public Double getPhanTramTienGiam() {
        return phanTramTienGiam;
    }

    public void setPhanTramTienGiam(Double phanTramTienGiam) {
        this.phanTramTienGiam = phanTramTienGiam;
    }

    public Double getTongChiTieuToiThieu() {
        return tongChiTieuToiThieu;
    }

    public void setTongChiTieuToiThieu(Double tongChiTieuToiThieu) {
        this.tongChiTieuToiThieu = tongChiTieuToiThieu;
    }

}
