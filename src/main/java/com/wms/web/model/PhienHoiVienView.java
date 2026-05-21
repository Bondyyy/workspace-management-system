package com.wms.web.model;

import java.time.LocalDateTime;

public class PhienHoiVienView {
    private final String maPhien;
    private final String maKG;
    private final String tenKhongGian;
    private final String tenChiNhanh;
    private final LocalDateTime thoiGianBatDau;
    private final LocalDateTime thoiGianDuKienKetThuc;
    private final LocalDateTime thoiGianKetThuc;
    private final String trangThaiPhien;
    private final String thoiGianDongCua;

    public PhienHoiVienView(String maPhien, String maKG, String tenKhongGian, String tenChiNhanh,
                             LocalDateTime thoiGianBatDau, LocalDateTime thoiGianDuKienKetThuc,
                             LocalDateTime thoiGianKetThuc, String trangThaiPhien, String thoiGianDongCua) {
        this.maPhien = maPhien;
        this.maKG = maKG;
        this.tenKhongGian = tenKhongGian;
        this.tenChiNhanh = tenChiNhanh;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianDuKienKetThuc = thoiGianDuKienKetThuc;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.trangThaiPhien = trangThaiPhien;
        this.thoiGianDongCua = thoiGianDongCua;
    }

    public String getMaPhien() {
        return maPhien;
    }

    public String getMaKG() {
        return maKG;
    }

    public String getTenKhongGian() {
        return tenKhongGian;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public LocalDateTime getThoiGianDuKienKetThuc() {
        return thoiGianDuKienKetThuc;
    }

    public LocalDateTime getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public String getTrangThaiPhien() {
        return trangThaiPhien;
    }

    public String getThoiGianDongCua() {
        return thoiGianDongCua;
    }
}
