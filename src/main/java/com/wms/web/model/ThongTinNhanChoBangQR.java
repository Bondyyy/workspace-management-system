package com.wms.web.model;

import java.time.LocalDateTime;

public class ThongTinNhanChoBangQR {
    private final String maDatCho;
    private final String maQR;
    private final String trangThaiDatTruoc;
    private final LocalDateTime thoiGianDuKienToi;
    private final Integer khoangThoiGianSuDung;
    private final String maKH;
    private final String maKG;
    private final String tenKG;
    private final String maCN;
    private final String tenCN;
    private final String tenKhachHang;

    public ThongTinNhanChoBangQR(String maDatCho, String maQR, String trangThaiDatTruoc,
                                 LocalDateTime thoiGianDuKienToi, Integer khoangThoiGianSuDung,
                                 String maKH, String maKG, String tenKG, String maCN,
                                 String tenCN, String tenKhachHang) {
        this.maDatCho = maDatCho;
        this.maQR = maQR;
        this.trangThaiDatTruoc = trangThaiDatTruoc;
        this.thoiGianDuKienToi = thoiGianDuKienToi;
        this.khoangThoiGianSuDung = khoangThoiGianSuDung;
        this.maKH = maKH;
        this.maKG = maKG;
        this.tenKG = tenKG;
        this.maCN = maCN;
        this.tenCN = tenCN;
        this.tenKhachHang = tenKhachHang;
    }

    public String getMaDatCho() {
        return maDatCho;
    }

    public String getMaQR() {
        return maQR;
    }

    public String getTrangThaiDatTruoc() {
        return trangThaiDatTruoc;
    }

    public LocalDateTime getThoiGianDuKienToi() {
        return thoiGianDuKienToi;
    }

    public Integer getKhoangThoiGianSuDung() {
        return khoangThoiGianSuDung;
    }

    public int laySoGioSuDungAnToan() {
        return khoangThoiGianSuDung == null || khoangThoiGianSuDung < 1 ? 1 : khoangThoiGianSuDung;
    }

    public String getMaKH() {
        return maKH;
    }

    public String getMaKG() {
        return maKG;
    }

    public String getTenKG() {
        return tenKG;
    }

    public String getMaCN() {
        return maCN;
    }

    public String getTenCN() {
        return tenCN;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }
}
