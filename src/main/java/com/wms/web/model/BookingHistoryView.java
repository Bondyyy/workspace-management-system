package com.wms.web.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingHistoryView {
    private final String maPhien;
    private final String maDatCho;
    private final String maHoaDon;
    private final String maQR;
    private final String tenKhongGian;
    private final String tenChiNhanh;
    private final LocalDateTime thoiGianBatDau;
    private final LocalDateTime thoiGianDuKienKetThuc;
    private final LocalDateTime thoiGianKetThuc;
    private final String trangThaiPhien;
    private final String trangThaiThanhToan;
    private final BigDecimal tongTien;
    private final BigDecimal thanhTien;
    private final LocalDateTime ngayLapHoaDon;
    private final List<SessionServiceView> dichVuDaDung;

    public BookingHistoryView(String maPhien, String maDatCho, String maHoaDon, String maQR,
                              String tenKhongGian, String tenChiNhanh,
                              LocalDateTime thoiGianBatDau, LocalDateTime thoiGianDuKienKetThuc,
                              LocalDateTime thoiGianKetThuc, String trangThaiPhien,
                              String trangThaiThanhToan, BigDecimal tongTien, BigDecimal thanhTien,
                              LocalDateTime ngayLapHoaDon, List<SessionServiceView> dichVuDaDung) {
        this.maPhien = maPhien;
        this.maDatCho = maDatCho;
        this.maHoaDon = maHoaDon;
        this.maQR = maQR;
        this.tenKhongGian = tenKhongGian;
        this.tenChiNhanh = tenChiNhanh;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianDuKienKetThuc = thoiGianDuKienKetThuc;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.trangThaiPhien = trangThaiPhien;
        this.trangThaiThanhToan = trangThaiThanhToan;
        this.tongTien = tongTien;
        this.thanhTien = thanhTien;
        this.ngayLapHoaDon = ngayLapHoaDon;
        this.dichVuDaDung = dichVuDaDung == null ? List.of() : dichVuDaDung;
    }

    public String getMaPhien() {
        return maPhien;
    }

    public String getMaDatCho() {
        return maDatCho;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public String getMaQR() {
        return maQR;
    }

    public String getTenKhongGian() {
        return tenKhongGian;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public LocalDateTime getThoiGianDuKienKetThuc() {
        return thoiGianDuKienKetThuc;
    }

    public LocalDateTime getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public String getTrangThaiPhien() {
        return trangThaiPhien;
    }

    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public LocalDateTime getNgayLapHoaDon() {
        return ngayLapHoaDon;
    }

    public List<SessionServiceView> getDichVuDaDung() {
        return dichVuDaDung;
    }
}
