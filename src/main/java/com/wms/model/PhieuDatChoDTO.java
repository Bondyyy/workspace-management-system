package com.wms.model;

import java.sql.Timestamp;

public class PhieuDatChoDTO {
    private String maPhieu;
    private String maKH;
    private String maKhongGian;
    private Timestamp thoiGianBatDau;
    private Timestamp thoiGianKetThuc;
    private int soGio;
    private double tongTien;
    private String trangThai;
    private Timestamp thoiGianTao;

    public PhieuDatChoDTO() {}

    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getMaKhongGian() { return maKhongGian; }
    public void setMaKhongGian(String maKhongGian) { this.maKhongGian = maKhongGian; }

    public Timestamp getThoiGianBatDau() { return thoiGianBatDau; }
    public void setThoiGianBatDau(Timestamp thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }

    public Timestamp getThoiGianKetThuc() { return thoiGianKetThuc; }
    public void setThoiGianKetThuc(Timestamp thoiGianKetThuc) { this.thoiGianKetThuc = thoiGianKetThuc; }

    public int getSoGio() { return soGio; }
    public void setSoGio(int soGio) { this.soGio = soGio; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Timestamp getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(Timestamp thoiGianTao) { this.thoiGianTao = thoiGianTao; }
}
