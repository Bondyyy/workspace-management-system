package com.wms.model.TrangChuQuanLy.QuanLyPhien;

import com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecDTO;
import java.sql.Timestamp;

public class PhienLamViecFullDTO extends PhienLamViecDTO {
    private String tenKhongGian;
    private String tenKhachHang;
    private String trangThaiThanhToan;
    private String trangThaiDatCho;

    public PhienLamViecFullDTO() {
    }

    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public void setTrangThaiThanhToan(String trangThaiThanhToan) {
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public String getTenKhongGian() {
        return tenKhongGian;
    }

    public void setTenKhongGian(String tenKhongGian) {
        this.tenKhongGian = tenKhongGian;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getTrangThaiDatCho() {
        return trangThaiDatCho;
    }

    public void setTrangThaiDatCho(String trangThaiDatCho) {
        this.trangThaiDatCho = trangThaiDatCho;
    }
}
