package com.wms.model.TrangChuQuanLy.QuanLyPhieuGiamGia;

import java.util.Date;

public class PhieuGiamGiaDTO {
    private String maPGG;
    private String maChuSoPGG;
    private double giaTriGiamGia;
    private double giaTriApDungToiThieu;
    private Date ngayBatDauApDung;
    private Date ngayKetThucApDung;
    private int slDaDung;
    private int slToiDa;
    private Date ngayTaoPGG;
    private String maNV;
    private String trangThai;

    public PhieuGiamGiaDTO() {}

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getMaPGG() { return maPGG; }
    public void setMaPGG(String maPGG) { this.maPGG = maPGG; }

    public String getMaChuSoPGG() { return maChuSoPGG; }
    public void setMaChuSoPGG(String maChuSoPGG) { this.maChuSoPGG = maChuSoPGG; }

    public double getGiaTriGiamGia() { return giaTriGiamGia; }
    public void setGiaTriGiamGia(double giaTriGiamGia) { this.giaTriGiamGia = giaTriGiamGia; }

    public double getGiaTriApDungToiThieu() { return giaTriApDungToiThieu; }
    public void setGiaTriApDungToiThieu(double giaTriApDungToiThieu) { this.giaTriApDungToiThieu = giaTriApDungToiThieu; }

    public Date getNgayBatDauApDung() { return ngayBatDauApDung; }
    public void setNgayBatDauApDung(Date ngayBatDauApDung) { this.ngayBatDauApDung = ngayBatDauApDung; }

    public Date getNgayKetThucApDung() { return ngayKetThucApDung; }
    public void setNgayKetThucApDung(Date ngayKetThucApDung) { this.ngayKetThucApDung = ngayKetThucApDung; }

    public int getSlDaDung() { return slDaDung; }
    public void setSlDaDung(int slDaDung) { this.slDaDung = slDaDung; }

    public int getSlToiDa() { return slToiDa; }
    public void setSlToiDa(int slToiDa) { this.slToiDa = slToiDa; }

    public Date getNgayTaoPGG() { return ngayTaoPGG; }
    public void setNgayTaoPGG(Date ngayTaoPGG) { this.ngayTaoPGG = ngayTaoPGG; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
}
