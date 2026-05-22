package com.wms.web.form;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class YeuCauWebhookThanhToan {
    @JsonAlias({"maGiaoDich", "ma_giao_dich", "transaction_id", "reference", "referenceCode"})
    private String transactionId;
    @JsonAlias({"soTien", "so_tien", "transferAmount", "transfer_amount", "transactionAmount"})
    private BigDecimal amount;
    @JsonAlias({"noiDung", "noi_dung", "content", "transferContent", "transfer_content", "addInfo"})
    private String description;
    @JsonAlias({"trangThai", "trang_thai", "transactionStatus"})
    private String status;
    @JsonAlias({"thoiGianThanhToan", "thoi_gian_thanh_toan", "paid_at", "transactionTime", "transaction_time"})
    private LocalDateTime paidAt;

    public String getMaGiaoDich() {
        return transactionId;
    }

    public void setMaGiaoDich(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getSoTien() {
        return amount;
    }

    public void setSoTien(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNoiDung() {
        return description;
    }

    public void setNoiDung(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTrangThai() {
        return status;
    }

    public void setTrangThai(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getThoiGianThanhToan() {
        return paidAt;
    }

    public void setThoiGianThanhToan(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
