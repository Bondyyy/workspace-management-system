package com.wms.web.form;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class YeuCauWebhookThanhToan {
    private String transactionId;
    private BigDecimal amount;
    private String description;
    private String status;
    private LocalDateTime paidAt;

    public String getMaGiaoDich() {
        return transactionId;
    }

    public void setMaGiaoDich(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getSoTien() {
        return amount;
    }

    public void setSoTien(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNoiDung() {
        return description;
    }

    public void setNoiDung(String description) {
        this.description = description;
    }

    public String getTrangThai() {
        return status;
    }

    public void setTrangThai(String status) {
        this.status = status;
    }

    public LocalDateTime getThoiGianThanhToan() {
        return paidAt;
    }

    public void setThoiGianThanhToan(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
