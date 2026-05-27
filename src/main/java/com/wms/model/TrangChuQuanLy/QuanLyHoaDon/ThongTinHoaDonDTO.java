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
    private double tongTienGoc;
    private double tongTien;
    private double thanhTien;
    private String maPhien;
    private String trangThaiPhien;
    private String trangThaiThanhToan;
    private List<DichVuDaDungDTO> danhSachDichVu;
    private double soTienDaTraTruoc;
    private boolean daTraTruoc;
    private String ngayLapHoaDon;
    private String phuongThucThanhToan;
    private String tenChiNhanh;
    private String maPGG;
    private String maVoucher;
    private String maChuSoPGG;
    private String tenHangThanhVien;
    private double phanTramGiamHangThanhVien;
    private double soTienGiamVoucher;
    private double soTienGiamHangThanhVien;
    private double tongTienGiam;
    private double tienGocDatTruoc;
    private double tienGocPhatSinh;
    private String maPGGDatTruoc;
    private String maChuSoPGGDatTruoc;
    private double tienGiamVoucherDatTruoc;
    private double phanTramGiamHangTVDatTruoc;
    private double tienGiamHangTVDatTruoc;
    private String maPGGTaiQuay;
    private String maChuSoPGGTaiQuay;
    private double tienGiamVoucherTaiQuay;
    private double phanTramGiamHangTVTaiQuay;
    private double tienGiamHangTVTaiQuay;
    private double soTienThanhToanTaiQuay;
    private List<DichVuDaDungDTO> danhSachDichVuPhatSinh;
    private List<InvoiceLine> dongChiPhi;
    private List<DiscountLine> dongVoucher;
    private double soTienConLai;
    private double tienGiamHang;
    private double soTienCanThanhToan;

    public ThongTinHoaDonDTO() {
        this.danhSachDichVu = new ArrayList<>();
        this.danhSachDichVuPhatSinh = new ArrayList<>();
        this.dongChiPhi = new ArrayList<>();
        this.dongVoucher = new ArrayList<>();
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
    public double getTongTienGoc() { return tongTienGoc; }
    public void setTongTienGoc(double tongTienGoc) { this.tongTienGoc = tongTienGoc; }
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
    public String getTrangThaiThanhToan() { return trangThaiThanhToan; }
    public void setTrangThaiThanhToan(String trangThaiThanhToan) { this.trangThaiThanhToan = trangThaiThanhToan; }
    public double getSoTienDaTraTruoc() { return soTienDaTraTruoc; }
    public void setSoTienDaTraTruoc(double soTienDaTraTruoc) { this.soTienDaTraTruoc = soTienDaTraTruoc; }
    public boolean isDaTraTruoc() { return daTraTruoc; }
    public void setDaTraTruoc(boolean daTraTruoc) { this.daTraTruoc = daTraTruoc; }
    public String getNgayLapHoaDon() { return ngayLapHoaDon; }
    public void setNgayLapHoaDon(String ngayLapHoaDon) { this.ngayLapHoaDon = ngayLapHoaDon; }
    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }
    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }
    public String getMaPGG() { return maPGG; }
    public void setMaPGG(String maPGG) { this.maPGG = maPGG; }
    public String getMaVoucher() { return maVoucher; }
    public void setMaVoucher(String maVoucher) { this.maVoucher = maVoucher; }
    public String getMaChuSoPGG() { return maChuSoPGG; }
    public void setMaChuSoPGG(String maChuSoPGG) { this.maChuSoPGG = maChuSoPGG; }
    public String getTenHangThanhVien() { return tenHangThanhVien; }
    public void setTenHangThanhVien(String tenHangThanhVien) { this.tenHangThanhVien = tenHangThanhVien; }
    public double getPhanTramGiamHangThanhVien() { return phanTramGiamHangThanhVien; }
    public void setPhanTramGiamHangThanhVien(double phanTramGiamHangThanhVien) { this.phanTramGiamHangThanhVien = phanTramGiamHangThanhVien; }
    public double getSoTienGiamVoucher() { return soTienGiamVoucher; }
    public void setSoTienGiamVoucher(double soTienGiamVoucher) { this.soTienGiamVoucher = soTienGiamVoucher; }
    public double getSoTienGiamHangThanhVien() { return soTienGiamHangThanhVien; }
    public void setSoTienGiamHangThanhVien(double soTienGiamHangThanhVien) {
        this.soTienGiamHangThanhVien = soTienGiamHangThanhVien;
        this.tienGiamHang = soTienGiamHangThanhVien;
    }
    public double getTongTienGiam() { return tongTienGiam; }
    public void setTongTienGiam(double tongTienGiam) { this.tongTienGiam = tongTienGiam; }
    public double getTienGocDatTruoc() { return tienGocDatTruoc; }
    public void setTienGocDatTruoc(double tienGocDatTruoc) { this.tienGocDatTruoc = tienGocDatTruoc; }
    public double getTienGocPhatSinh() { return tienGocPhatSinh; }
    public void setTienGocPhatSinh(double tienGocPhatSinh) { this.tienGocPhatSinh = tienGocPhatSinh; }
    public String getMaPGGDatTruoc() { return maPGGDatTruoc; }
    public void setMaPGGDatTruoc(String maPGGDatTruoc) { this.maPGGDatTruoc = maPGGDatTruoc; }
    public String getMaChuSoPGGDatTruoc() { return maChuSoPGGDatTruoc; }
    public void setMaChuSoPGGDatTruoc(String maChuSoPGGDatTruoc) { this.maChuSoPGGDatTruoc = maChuSoPGGDatTruoc; }
    public double getTienGiamVoucherDatTruoc() { return tienGiamVoucherDatTruoc; }
    public void setTienGiamVoucherDatTruoc(double tienGiamVoucherDatTruoc) { this.tienGiamVoucherDatTruoc = tienGiamVoucherDatTruoc; }
    public double getPhanTramGiamHangTVDatTruoc() { return phanTramGiamHangTVDatTruoc; }
    public void setPhanTramGiamHangTVDatTruoc(double phanTramGiamHangTVDatTruoc) { this.phanTramGiamHangTVDatTruoc = phanTramGiamHangTVDatTruoc; }
    public double getTienGiamHangTVDatTruoc() { return tienGiamHangTVDatTruoc; }
    public void setTienGiamHangTVDatTruoc(double tienGiamHangTVDatTruoc) { this.tienGiamHangTVDatTruoc = tienGiamHangTVDatTruoc; }
    public String getMaPGGTaiQuay() { return maPGGTaiQuay; }
    public void setMaPGGTaiQuay(String maPGGTaiQuay) { this.maPGGTaiQuay = maPGGTaiQuay; }
    public String getMaChuSoPGGTaiQuay() { return maChuSoPGGTaiQuay; }
    public void setMaChuSoPGGTaiQuay(String maChuSoPGGTaiQuay) { this.maChuSoPGGTaiQuay = maChuSoPGGTaiQuay; }
    public double getTienGiamVoucherTaiQuay() { return tienGiamVoucherTaiQuay; }
    public void setTienGiamVoucherTaiQuay(double tienGiamVoucherTaiQuay) { this.tienGiamVoucherTaiQuay = tienGiamVoucherTaiQuay; }
    public double getPhanTramGiamHangTVTaiQuay() { return phanTramGiamHangTVTaiQuay; }
    public void setPhanTramGiamHangTVTaiQuay(double phanTramGiamHangTVTaiQuay) { this.phanTramGiamHangTVTaiQuay = phanTramGiamHangTVTaiQuay; }
    public double getTienGiamHangTVTaiQuay() { return tienGiamHangTVTaiQuay; }
    public void setTienGiamHangTVTaiQuay(double tienGiamHangTVTaiQuay) { this.tienGiamHangTVTaiQuay = tienGiamHangTVTaiQuay; }
    public double getSoTienThanhToanTaiQuay() { return soTienThanhToanTaiQuay; }
    public void setSoTienThanhToanTaiQuay(double soTienThanhToanTaiQuay) { this.soTienThanhToanTaiQuay = soTienThanhToanTaiQuay; }
    public List<DichVuDaDungDTO> getDanhSachDichVuPhatSinh() { return danhSachDichVuPhatSinh; }
    public void setDanhSachDichVuPhatSinh(List<DichVuDaDungDTO> danhSachDichVuPhatSinh) { this.danhSachDichVuPhatSinh = danhSachDichVuPhatSinh; }
    public List<InvoiceLine> getDongChiPhi() { return dongChiPhi; }
    public void setDongChiPhi(List<InvoiceLine> dongChiPhi) { this.dongChiPhi = dongChiPhi; }
    public List<DiscountLine> getDongVoucher() { return dongVoucher; }
    public void setDongVoucher(List<DiscountLine> dongVoucher) { this.dongVoucher = dongVoucher; }
    public double getSoTienConLai() { return soTienConLai; }
    public void setSoTienConLai(double soTienConLai) { this.soTienConLai = soTienConLai; }
    public double getTienGiamHang() { return tienGiamHang; }
    public void setTienGiamHang(double tienGiamHang) {
        this.tienGiamHang = tienGiamHang;
        this.soTienGiamHangThanhVien = tienGiamHang;
    }
    public double getPhanTramGiamHang() { return phanTramGiamHangThanhVien; }
    public void setPhanTramGiamHang(double phanTramGiamHang) { this.phanTramGiamHangThanhVien = phanTramGiamHang; }
    public double getSoTienCanThanhToan() { return soTienCanThanhToan; }
    public void setSoTienCanThanhToan(double soTienCanThanhToan) { this.soTienCanThanhToan = soTienCanThanhToan; }
}
