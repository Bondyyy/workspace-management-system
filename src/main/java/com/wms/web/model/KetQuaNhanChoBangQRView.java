package com.wms.web.model;

public class KetQuaNhanChoBangQRView {
    private boolean thanhCong;
    private String thongBao;
    private String maDatCho;
    private String maPhien;
    private String tenKhachHang;
    private String tenKhongGian;

    public KetQuaNhanChoBangQRView() {
    }

    public KetQuaNhanChoBangQRView(boolean thanhCong, String thongBao) {
        this.thanhCong = thanhCong;
        this.thongBao = thongBao;
    }

    public KetQuaNhanChoBangQRView(boolean thanhCong, String thongBao, String maDatCho,
                                   String maPhien, String tenKhachHang, String tenKhongGian) {
        this.thanhCong = thanhCong;
        this.thongBao = thongBao;
        this.maDatCho = maDatCho;
        this.maPhien = maPhien;
        this.tenKhachHang = tenKhachHang;
        this.tenKhongGian = tenKhongGian;
    }

    public static KetQuaNhanChoBangQRView thatBai(String thongBao) {
        return new KetQuaNhanChoBangQRView(false, thongBao);
    }

    public static KetQuaNhanChoBangQRView thanhCong(String thongBao, ThongTinNhanChoBangQR thongTin, String maPhien) {
        return new KetQuaNhanChoBangQRView(
                true,
                thongBao,
                thongTin.getMaDatCho(),
                maPhien,
                thongTin.getTenKhachHang(),
                thongTin.getTenKG()
        );
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public void setThanhCong(boolean thanhCong) {
        this.thanhCong = thanhCong;
    }

    public String getThongBao() {
        return thongBao;
    }

    public void setThongBao(String thongBao) {
        this.thongBao = thongBao;
    }

    public String getMaDatCho() {
        return maDatCho;
    }

    public void setMaDatCho(String maDatCho) {
        this.maDatCho = maDatCho;
    }

    public String getMaPhien() {
        return maPhien;
    }

    public void setMaPhien(String maPhien) {
        this.maPhien = maPhien;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getTenKhongGian() {
        return tenKhongGian;
    }

    public void setTenKhongGian(String tenKhongGian) {
        this.tenKhongGian = tenKhongGian;
    }
}
