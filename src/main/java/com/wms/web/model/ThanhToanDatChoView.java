package com.wms.web.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ThanhToanDatChoView {
    private final String maDatCho;
    private final String hoTen;
    private final String tenKhongGian;
    private final String tenChiNhanh;
    private final LocalDateTime thoiGianDuKienToi;
    private final Integer khoangThoiGianSuDung;
    private final String trangThaiDatTruoc;
    private final BigDecimal thanhTien;
    private final String noiDungChuyenKhoan;
    private final LocalDateTime hetHanThanhToan;

    public ThanhToanDatChoView(String maDatCho, String hoTen, String tenKhongGian, String tenChiNhanh,
                              LocalDateTime thoiGianDuKienToi, Integer khoangThoiGianSuDung,
                              String trangThaiDatTruoc, BigDecimal thanhTien,
                              String noiDungChuyenKhoan, LocalDateTime hetHanThanhToan) {
        this.maDatCho = maDatCho;
        this.hoTen = hoTen;
        this.tenKhongGian = tenKhongGian;
        this.tenChiNhanh = tenChiNhanh;
        this.thoiGianDuKienToi = thoiGianDuKienToi;
        this.khoangThoiGianSuDung = khoangThoiGianSuDung;
        this.trangThaiDatTruoc = trangThaiDatTruoc;
        this.thanhTien = thanhTien;
        this.noiDungChuyenKhoan = noiDungChuyenKhoan;
        this.hetHanThanhToan = hetHanThanhToan;
    }

    public String getMaDatCho() {
        return maDatCho;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getTenKhongGian() {
        return tenKhongGian;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public LocalDateTime getThoiGianDuKienToi() {
        return thoiGianDuKienToi;
    }

    public Integer getKhoangThoiGianSuDung() {
        return khoangThoiGianSuDung;
    }

    public String getTrangThaiDatTruoc() {
        return trangThaiDatTruoc;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public String getNoiDungChuyenKhoan() {
        return noiDungChuyenKhoan;
    }

    public LocalDateTime getHetHanThanhToan() {
        return hetHanThanhToan;
    }
}
