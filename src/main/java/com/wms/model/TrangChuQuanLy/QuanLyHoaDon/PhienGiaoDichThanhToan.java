package com.wms.model.TrangChuQuanLy.QuanLyHoaDon;

import java.sql.Connection;

public class PhienGiaoDichThanhToan {

    private final Connection connection;
    private final String maHoaDon;
    private final ThongTinHoaDonDTO hoaDonLan1;
    private final boolean serializable;
    private final long thoiDiemBatDau;

    public PhienGiaoDichThanhToan(Connection connection, String maHoaDon,
            ThongTinHoaDonDTO hoaDonLan1, boolean serializable) {
        this.connection = connection;
        this.maHoaDon = maHoaDon;
        this.hoaDonLan1 = hoaDonLan1;
        this.serializable = serializable;
        this.thoiDiemBatDau = System.currentTimeMillis();
    }

    public Connection getConnection() {
        return connection;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public ThongTinHoaDonDTO getHoaDonLan1() {
        return hoaDonLan1;
    }

    public boolean isSerializable() {
        return serializable;
    }

    public long getThoiDiemBatDau() {
        return thoiDiemBatDau;
    }
}
