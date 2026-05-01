package com.wms.model.VanHanh_DichVu;

import java.sql.Timestamp;

public class DatChoDTO {
    private String maDatCho;
    private Timestamp thoiGianDat;
    private Timestamp thoiGianDuKienToi;
    private Integer khoangThoiGianSuDung;
    private String trangThaiDatTruoc;
    private Double thanhTien;
    private String ghiChu;
    private String maQR;
    private Timestamp capNhatLanCuoi;
    private String maKH;
    private String maKG;

    public DatChoDTO() {}

    public String getMaDatCho() {
        return maDatCho;
    }

    public void setMaDatCho(String maDatCho) {
        this.maDatCho = maDatCho;
    }

    public Timestamp getThoiGianDat() {
        return thoiGianDat;
    }

    public void setThoiGianDat(Timestamp thoiGianDat) {
        this.thoiGianDat = thoiGianDat;
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

    public Double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(Double thanhTien) {
        this.thanhTien = thanhTien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getMaQR() {
        return maQR;
    }

    public void setMaQR(String maQR) {
        this.maQR = maQR;
    }

    public Timestamp getCapNhatLanCuoi() {
        return capNhatLanCuoi;
    }

    public void setCapNhatLanCuoi(Timestamp capNhatLanCuoi) {
        this.capNhatLanCuoi = capNhatLanCuoi;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getMaKG() {
        return maKG;
    }

    public void setMaKG(String maKG) {
        this.maKG = maKG;
    }

}