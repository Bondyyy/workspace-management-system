package com.wms.web.model;

import java.math.BigDecimal;

public class SpaceView {
    private final String maKG;
    private final String tenKG;
    private final String tenLoaiKG;
    private final String viTri;
    private final String trangThaiKG;
    private final String tenCN;
    private final BigDecimal donGiaTheoGio;

    public SpaceView(String maKG, String tenKG, String tenLoaiKG, String viTri,
                     String trangThaiKG, String tenCN, BigDecimal donGiaTheoGio) {
        this.maKG = maKG;
        this.tenKG = tenKG;
        this.tenLoaiKG = tenLoaiKG;
        this.viTri = viTri;
        this.trangThaiKG = trangThaiKG;
        this.tenCN = tenCN;
        this.donGiaTheoGio = donGiaTheoGio;
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

    public String getTenCN() {
        return tenCN;
    }

    public BigDecimal getDonGiaTheoGio() {
        return donGiaTheoGio;
    }
}
