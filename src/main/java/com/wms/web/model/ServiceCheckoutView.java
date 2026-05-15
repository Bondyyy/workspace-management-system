package com.wms.web.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceCheckoutView {
    private final MemberSessionView session;
    private final ServiceOptionView service;
    private final Integer quantity;
    private final String note;
    private final BigDecimal totalAmount;
    private final LocalDateTime newExpectedEnd;

    public ServiceCheckoutView(MemberSessionView session, ServiceOptionView service, Integer quantity,
                               String note, BigDecimal totalAmount, LocalDateTime newExpectedEnd) {
        this.session = session;
        this.service = service;
        this.quantity = quantity;
        this.note = note;
        this.totalAmount = totalAmount;
        this.newExpectedEnd = newExpectedEnd;
    }

    public MemberSessionView getSession() {
        return session;
    }

    public ServiceOptionView getService() {
        return service;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getNote() {
        return note;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getNewExpectedEnd() {
        return newExpectedEnd;
    }
}
