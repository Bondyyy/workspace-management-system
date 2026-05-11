package com.wms.model.TrangChuQuanLy.QuanLyHoaDon;

import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DichVuDaDungDTO;
import java.util.ArrayList;
import java.util.List;

public class ThongTinHoaDonDTO {
    private String maHoaDon;
    private String hoTenKH;
    private String tenKhongGian;
    private String thoiGianSửDung; // Chuỗi format "08:00 - 12:00 (27/04/2024)"
    private double tongSoGio;
    private double tongTien;
    private double thanhTien;
    private String maPhien;
    private String trangThaiPhien;
    private List<DichVuDaDungDTO> danhSachDichVu;

    public ThongTinHoaDonDTO() {
        this.danhSachDichVu = new ArrayList<>();
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public String getHoTenKH() { return hoTenKH; }
    public void setHoTenKH(String hoTenKH) { this.hoTenKH = hoTenKH; }
    public String getTenKhongGian() { return tenKhongGian; }
    public void setTenKhongGian(String tenKhongGian) { this.tenKhongGian = tenKhongGian; }
    public String getThoiGianSửDung() { return thoiGianSửDung; }
    public void setThoiGianSửDung(String thoiGianSửDung) { this.thoiGianSửDung = thoiGianSửDung; }
    public double getTongSoGio() { return tongSoGio; }
    public void setTongSoGio(double tongSoGio) { this.tongSoGio = tongSoGio; }
    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }
    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
    public List<DichVuDaDungDTO> getDanhSachDichVu() { return danhSachDichVu; }
    public void setDanhSachDichVu(List<DichVuDaDungDTO> danhSachDichVu) { this.danhSachDichVu = danhSachDichVu; }
    public String getMaPhien() { return maPhien; }
    public void setMaPhien(String maPhien) { this.maPhien = maPhien; }
    public String getTrangThaiPhien() { return trangThaiPhien; }
    public void setTrangThaiPhien(String trangThaiPhien) { this.trangThaiPhien = trangThaiPhien; }
}
