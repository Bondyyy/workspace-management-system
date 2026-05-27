package com.wms.model.TrangChuQuanLy.QuanLyHoaDon;

public class DiscountLine {
    private String maPGG;
    private String maChuSoPGG;
    private String noiDung;
    private double soTienGiam;
    private boolean datTruoc;

    public DiscountLine() {
    }

    public DiscountLine(String maPGG, String maChuSoPGG, String noiDung, double soTienGiam, boolean datTruoc) {
        this.maPGG = maPGG;
        this.maChuSoPGG = maChuSoPGG;
        this.noiDung = noiDung;
        this.soTienGiam = soTienGiam;
        this.datTruoc = datTruoc;
    }

    public String getMaPGG() {
        return maPGG;
    }

    public void setMaPGG(String maPGG) {
        this.maPGG = maPGG;
    }

    public String getMaChuSoPGG() {
        return maChuSoPGG;
    }

    public void setMaChuSoPGG(String maChuSoPGG) {
        this.maChuSoPGG = maChuSoPGG;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public double getSoTienGiam() {
        return soTienGiam;
    }

    public void setSoTienGiam(double soTienGiam) {
        this.soTienGiam = soTienGiam;
    }

    public boolean isDatTruoc() {
        return datTruoc;
    }

    public void setDatTruoc(boolean datTruoc) {
        this.datTruoc = datTruoc;
    }
}
