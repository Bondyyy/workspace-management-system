package com.wms.model.TrangChuQuanLy.QuanLyPhien;

public class KetQuaNhanChoDTO {
    private final boolean thanhCong;
    private final String thongBao;
    private final String maDatCho;
    private final String maPhien;
    private final String tenKhachHang;
    private final String tenChiNhanh;
    private final String tenKhongGian;
    private final java.sql.Timestamp thoiGianBatDau;
    private final java.sql.Timestamp thoiGianDuKienKetThuc;

    public KetQuaNhanChoDTO(boolean thanhCong, String thongBao) {
        this(thanhCong, thongBao, null, null);
    }

    public KetQuaNhanChoDTO(boolean thanhCong, String thongBao, String maDatCho, String maPhien) {
        this(thanhCong, thongBao, maDatCho, maPhien, null, null, null, null, null);
    }

    public KetQuaNhanChoDTO(boolean thanhCong, String thongBao, String maDatCho, String maPhien,
                            String tenKhachHang, String tenChiNhanh, String tenKhongGian,
                            java.sql.Timestamp thoiGianBatDau, java.sql.Timestamp thoiGianDuKienKetThuc) {
        this.thanhCong = thanhCong;
        this.thongBao = thongBao;
        this.maDatCho = maDatCho;
        this.maPhien = maPhien;
        this.tenKhachHang = tenKhachHang;
        this.tenChiNhanh = tenChiNhanh;
        this.tenKhongGian = tenKhongGian;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianDuKienKetThuc = thoiGianDuKienKetThuc;
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public String getThongBao() {
        return thongBao;
    }

    public String getMaDatCho() {
        return maDatCho;
    }

    public String getMaPhien() {
        return maPhien;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public String getTenKhongGian() {
        return tenKhongGian;
    }

    public java.sql.Timestamp getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public java.sql.Timestamp getThoiGianDuKienKetThuc() {
        return thoiGianDuKienKetThuc;
    }
}
