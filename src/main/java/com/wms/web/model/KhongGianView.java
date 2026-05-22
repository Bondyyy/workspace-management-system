package com.wms.web.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class KhongGianView {
    private final String maKG;
    private final String tenKG;
    private final String tenLoaiKG;
    private final String viTri;
    private final String trangThaiKG;
    private final String maCN;
    private final String tenCN;
    private final String thoiGianMoCua;
    private final String thoiGianDongCua;
    private final BigDecimal donGiaTheoGio;
    private final int toaDoX;
    private final int toaDoY;
    private final int chieuDai;
    private final int chieuRong;
    private final boolean available;
    private final LocalDateTime busyUntil;

    public KhongGianView(String maKG, String tenKG, String tenLoaiKG, String viTri,
                     String trangThaiKG, String tenCN, BigDecimal donGiaTheoGio) {
        this(maKG, tenKG, tenLoaiKG, viTri, trangThaiKG, tenCN, donGiaTheoGio, 0, 0, 1, 1, true, null);
    }

    public KhongGianView(String maKG, String tenKG, String tenLoaiKG, String viTri,
                     String trangThaiKG, String tenCN, BigDecimal donGiaTheoGio,
                     int toaDoX, int toaDoY, int chieuDai, int chieuRong) {
        this(maKG, tenKG, tenLoaiKG, viTri, trangThaiKG, tenCN, donGiaTheoGio,
                toaDoX, toaDoY, chieuDai, chieuRong, true, null);
    }

    public KhongGianView(String maKG, String tenKG, String tenLoaiKG, String viTri,
                     String trangThaiKG, String tenCN, String thoiGianMoCua, String thoiGianDongCua,
                     BigDecimal donGiaTheoGio, int toaDoX, int toaDoY, int chieuDai, int chieuRong) {
        this(maKG, tenKG, tenLoaiKG, viTri, trangThaiKG, tenCN, thoiGianMoCua, thoiGianDongCua,
                donGiaTheoGio, toaDoX, toaDoY, chieuDai, chieuRong, true, null);
    }

    public KhongGianView(String maKG, String tenKG, String tenLoaiKG, String viTri,
                     String trangThaiKG, String maCN, String tenCN, String thoiGianMoCua, String thoiGianDongCua,
                     BigDecimal donGiaTheoGio, int toaDoX, int toaDoY, int chieuDai, int chieuRong) {
        this(maKG, tenKG, tenLoaiKG, viTri, trangThaiKG, maCN, tenCN, thoiGianMoCua, thoiGianDongCua,
                donGiaTheoGio, toaDoX, toaDoY, chieuDai, chieuRong, true, null);
    }

    public KhongGianView(String maKG, String tenKG, String tenLoaiKG, String viTri,
                     String trangThaiKG, String tenCN, BigDecimal donGiaTheoGio,
                     boolean available, LocalDateTime busyUntil) {
        this(maKG, tenKG, tenLoaiKG, viTri, trangThaiKG, tenCN, donGiaTheoGio,
                0, 0, 1, 1, available, busyUntil);
    }

    public KhongGianView(String maKG, String tenKG, String tenLoaiKG, String viTri,
                     String trangThaiKG, String tenCN, BigDecimal donGiaTheoGio,
                     int toaDoX, int toaDoY, int chieuDai, int chieuRong,
                     boolean available, LocalDateTime busyUntil) {
        this(maKG, tenKG, tenLoaiKG, viTri, trangThaiKG, tenCN, null, null,
                donGiaTheoGio, toaDoX, toaDoY, chieuDai, chieuRong, available, busyUntil);
    }

    public KhongGianView(String maKG, String tenKG, String tenLoaiKG, String viTri,
                     String trangThaiKG, String tenCN, String thoiGianMoCua, String thoiGianDongCua,
                     BigDecimal donGiaTheoGio, int toaDoX, int toaDoY, int chieuDai, int chieuRong,
                     boolean available, LocalDateTime busyUntil) {
        this(maKG, tenKG, tenLoaiKG, viTri, trangThaiKG, null, tenCN, thoiGianMoCua, thoiGianDongCua,
                donGiaTheoGio, toaDoX, toaDoY, chieuDai, chieuRong, available, busyUntil);
    }

    public KhongGianView(String maKG, String tenKG, String tenLoaiKG, String viTri,
                     String trangThaiKG, String maCN, String tenCN, String thoiGianMoCua, String thoiGianDongCua,
                     BigDecimal donGiaTheoGio, int toaDoX, int toaDoY, int chieuDai, int chieuRong,
                     boolean available, LocalDateTime busyUntil) {
        this.maKG = maKG;
        this.tenKG = tenKG;
        this.tenLoaiKG = tenLoaiKG;
        this.viTri = viTri;
        this.trangThaiKG = trangThaiKG;
        this.maCN = maCN;
        this.tenCN = tenCN;
        this.thoiGianMoCua = thoiGianMoCua;
        this.thoiGianDongCua = thoiGianDongCua;
        this.donGiaTheoGio = donGiaTheoGio;
        this.toaDoX = Math.max(0, toaDoX);
        this.toaDoY = Math.max(0, toaDoY);
        this.chieuDai = chieuDai > 0 ? chieuDai : 1;
        this.chieuRong = chieuRong > 0 ? chieuRong : 1;
        this.available = available;
        this.busyUntil = busyUntil;
    }

    public String getMaKG() {
        return maKG;
    }

    public String getTenKG() {
        return tenKG;
    }

    public String getTenLoaiKG() {
        return tenLoaiKG;
    }

    public String getViTri() {
        return viTri;
    }

    public String getTrangThaiKG() {
        return trangThaiKG;
    }

    public String getMaCN() {
        return maCN;
    }

    public String getTenCN() {
        return tenCN;
    }

    public String getThoiGianMoCua() {
        return thoiGianMoCua;
    }

    public String getThoiGianDongCua() {
        return thoiGianDongCua;
    }

    public BigDecimal getDonGiaTheoGio() {
        return donGiaTheoGio;
    }

    public int getToaDoX() {
        return toaDoX;
    }

    public int getToaDoY() {
        return toaDoY;
    }

    public int getChieuDai() {
        return chieuDai;
    }

    public int getChieuRong() {
        return chieuRong;
    }

    public boolean conTrong() {
        return available;
    }

    public LocalDateTime getBusyUntil() {
        return busyUntil;
    }
}
