package com.wms.web.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ThanhToanDichVuView {
    private final PhienHoiVienView session;
    private final TuyChonDichVuView service;
    private final Integer quantity;
    private final String note;
    private final BigDecimal totalAmount;
    private final LocalDateTime newExpectedEnd;

    public ThanhToanDichVuView(PhienHoiVienView session, TuyChonDichVuView service, Integer quantity,
                               String note, BigDecimal totalAmount, LocalDateTime newExpectedEnd) {
        this.session = session;
        this.service = service;
        this.quantity = quantity;
        this.note = note;
        this.totalAmount = totalAmount;
        this.newExpectedEnd = newExpectedEnd;
    }

    public PhienHoiVienView getPhien() {
        return session;
    }

    public TuyChonDichVuView getDichVu() {
        return service;
    }

    public Integer getSoLuong() {
        return quantity;
    }

    public String getGhiChu() {
        return note;
    }

    public BigDecimal getTongTien() {
        return totalAmount;
    }

    public LocalDateTime getThoiGianDuKienKetThucMoi() {
        return newExpectedEnd;
    }
}
