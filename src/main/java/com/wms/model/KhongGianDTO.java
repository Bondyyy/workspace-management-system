package com.wms.model;

public class KhongGianDTO {
    private String maKG;
    private String tenKG;
    private String trangThaiKG;
    private String viTri;
    private String maLoaiKG;
    private String tenLoaiKG;
    private String maCN;
    private String tenCN;
    private int toaDoX;
    private int toaDoY;
    private int chieuDai;
    private int chieuRong;

    public KhongGianDTO() {}

    public String getTenLoaiKG() {
        return tenLoaiKG;
    }

    public void setTenLoaiKG(String tenLoaiKG) {
        this.tenLoaiKG = tenLoaiKG;
    }

    public String getTenCN() {
        return tenCN;
    }

    public void setTenCN(String tenCN) {
        this.tenCN = tenCN;
    }

    public String getMaKG() {
        return maKG;
    }

    public void setMaKG(String maKG) {
        this.maKG = maKG;
    }

    public String getTenKG() {
        return tenKG;
    }

    public void setTenKG(String tenKG) {
        this.tenKG = tenKG;
    }

    public String getTrangThaiKG() {
        return trangThaiKG;
    }

    public void setTrangThaiKG(String trangThaiKG) {
        this.trangThaiKG = trangThaiKG;
    }

    public String getViTri() {
        return viTri;
    }

    public void setViTri(String viTri) {
        this.viTri = viTri;
    }

    public String getMaLoaiKG() {
        return maLoaiKG;
    }

    public void setMaLoaiKG(String maLoaiKG) {
        this.maLoaiKG = maLoaiKG;
    }

    public String getMaCN() {
        return maCN;
    }

    public void setMaCN(String maCN) {
        this.maCN = maCN;
    }
    
    public int getToaDoX() {
        return toaDoX;
    }

    public void setToaDoX(int toaDoX) {
        this.toaDoX = toaDoX;
    }

    public int getToaDoY() {
        return toaDoY;
    }

    public void setToaDoY(int toaDoY) {
        this.toaDoY = toaDoY;
    }

    public int getChieuDai() {
        return chieuDai;
    }

    public void setChieuDai(int chieuDai) {
        this.chieuDai = chieuDai;
    }

    public int getChieuRong() {
        return chieuRong;
    }

    public void setChieuRong(int chieuRong) {
        this.chieuRong = chieuRong;
    }

}
