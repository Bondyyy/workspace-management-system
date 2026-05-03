package com.wms.model;

import java.sql.Date;
import java.sql.Timestamp;

public class HoiVienDTO {
    private String maKH;
    private String maND;
    private String hoTen;
    private String sdt;
    private String email;
    private Date ngaySinh;
    private String gioiTinh;
    private String maHangThanhVien;
    private String hangThanhVien;
    private Timestamp thoiGianTao;
    private String anhDaiDien;
    private String trangThai;
    private Double tongChiTieu;

    public HoiVienDTO() {}

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getMaND() { return maND; }
    public void setMaND(String maND) { this.maND = maND; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Date getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(Date ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getMaHangThanhVien() { return maHangThanhVien; }
    public void setMaHangThanhVien(String maHangThanhVien) { this.maHangThanhVien = maHangThanhVien; }

    public String getHangThanhVien() { return hangThanhVien; }
    public void setHangThanhVien(String hangThanhVien) { this.hangThanhVien = hangThanhVien; }

    public Timestamp getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(Timestamp thoiGianTao) { this.thoiGianTao = thoiGianTao; }

    public String getAnhDaiDien() { return anhDaiDien; }
    public void setAnhDaiDien(String anhDaiDien) { this.anhDaiDien = anhDaiDien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Double getTongChiTieu() { return tongChiTieu; }
    public void setTongChiTieu(Double tongChiTieu) { this.tongChiTieu = tongChiTieu; }
}
