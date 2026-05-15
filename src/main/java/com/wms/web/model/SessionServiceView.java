package com.wms.web.model;

import java.math.BigDecimal;

public class SessionServiceView {
    private final String maDV;
    private final String tenDV;
    private final String tenLoaiDV;
    private final Integer soLuong;
    private final BigDecimal donGia;
    private final BigDecimal thanhTien;
    private final String ghiChu;

    public SessionServiceView(String maDV, String tenDV, String tenLoaiDV, Integer soLuong,
                              BigDecimal donGia, BigDecimal thanhTien, String ghiChu) {
        this.maDV = maDV;
        this.tenDV = tenDV;
        this.tenLoaiDV = tenLoaiDV;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
        this.ghiChu = ghiChu;
    }

    public String getMaDV() {
        return maDV;
    }

    public String getTenDV() {
        return tenDV;
    }

    public String getTenLoaiDV() {
        return tenLoaiDV;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public String getGhiChu() {
        return ghiChu;
    }
}
