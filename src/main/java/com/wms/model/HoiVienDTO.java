package com.wms.model;

import java.sql.Date;

public class HoiVienDTO {
    private String maKH;
    private String maND;
    private String hoTen;
    private String sdt;
    private String email;
    private Date ngaySinh;
    private String hangThanhVien;

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

    public String getHangThanhVien() { return hangThanhVien; }
    public void setHangThanhVien(String hangThanhVien) { this.hangThanhVien = hangThanhVien; }
}
