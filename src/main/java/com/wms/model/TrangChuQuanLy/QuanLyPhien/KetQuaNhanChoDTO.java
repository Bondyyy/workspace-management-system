package com.wms.model.TrangChuQuanLy.QuanLyPhien;

public class KetQuaNhanChoDTO {
    private final boolean thanhCong;
    private final String thongBao;
    private final String maDatCho;
    private final String maPhien;

    public KetQuaNhanChoDTO(boolean thanhCong, String thongBao) {
        this(thanhCong, thongBao, null, null);
    }

    public KetQuaNhanChoDTO(boolean thanhCong, String thongBao, String maDatCho, String maPhien) {
        this.thanhCong = thanhCong;
        this.thongBao = thongBao;
        this.maDatCho = maDatCho;
        this.maPhien = maPhien;
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public String getThongBao() {
        return thongBao;
    }

    public String getMaDatCho() {
        return maDatCho;
    }

    public String getMaPhien() {
        return maPhien;
    }
}
