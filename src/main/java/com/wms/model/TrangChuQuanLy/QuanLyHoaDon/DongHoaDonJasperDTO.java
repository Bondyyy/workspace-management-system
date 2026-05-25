package com.wms.model.TrangChuQuanLy.QuanLyHoaDon;

public class DongHoaDonJasperDTO {
    private String stt;
    private String noiDung;
    private String soLuong;
    private String donGia;
    private String thanhTien;

    public DongHoaDonJasperDTO() {
    }

    public DongHoaDonJasperDTO(String stt, String noiDung, String soLuong, String donGia, String thanhTien) {
        this.stt = stt;
        this.noiDung = noiDung;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public String getStt() {
        return stt;
    }

    public void setStt(String stt) {
        this.stt = stt;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(String soLuong) {
        this.soLuong = soLuong;
    }

    public String getDonGia() {
        return donGia;
    }

    public void setDonGia(String donGia) {
        this.donGia = donGia;
    }

    public String getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(String thanhTien) {
        this.thanhTien = thanhTien;
    }
}
