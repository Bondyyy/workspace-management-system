package com.wms.web.model;

public class ChiNhanhView {
    private final String maCN;
    private final String tenCN;
    private final String diaChi;
    private final String thoiGianMoCua;
    private final String thoiGianDongCua;
    private final String duongDayNong;

    public ChiNhanhView(String maCN, String tenCN, String diaChi, String thoiGianMoCua,
                      String thoiGianDongCua, String duongDayNong) {
        this.maCN = maCN;
        this.tenCN = tenCN;
        this.diaChi = diaChi;
        this.thoiGianMoCua = thoiGianMoCua;
        this.thoiGianDongCua = thoiGianDongCua;
        this.duongDayNong = duongDayNong;
    }

    public String getMaCN() {
        return maCN;
    }

    public String getTenCN() {
        return tenCN;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public String getThoiGianMoCua() {
        return thoiGianMoCua;
    }

    public String getThoiGianDongCua() {
        return thoiGianDongCua;
    }

    public String getDuongDayNong() {
        return duongDayNong;
    }
}
