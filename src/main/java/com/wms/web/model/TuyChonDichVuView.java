package com.wms.web.model;

import java.math.BigDecimal;

public class TuyChonDichVuView {
    private final String maDV;
    private final String tenDV;
    private final String tenLoaiDV;
    private final BigDecimal donGia;
    private final Integer soLuongTon;

    public TuyChonDichVuView(String maDV, String tenDV, String tenLoaiDV, BigDecimal donGia, Integer soLuongTon) {
        this.maDV = maDV;
        this.tenDV = tenDV;
        this.tenLoaiDV = tenLoaiDV;
        this.donGia = donGia;
        this.soLuongTon = soLuongTon;
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

    public BigDecimal getDonGia() {
        return donGia;
    }

    public Integer getSoLuongTon() {
        return soLuongTon;
    }
}
