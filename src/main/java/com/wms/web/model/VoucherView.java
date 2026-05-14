package com.wms.web.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VoucherView {
    private final String maPGG;
    private final String maChuSoPGG;
    private final BigDecimal giaTriGiamGia;
    private final BigDecimal giaTriApDungToiThieu;
    private final LocalDateTime ngayKetThucApDung;
    private final Integer slDaDung;
    private final Integer slToiDa;

    public VoucherView(String maPGG, String maChuSoPGG, BigDecimal giaTriGiamGia,
                       BigDecimal giaTriApDungToiThieu, LocalDateTime ngayKetThucApDung,
                       Integer slDaDung, Integer slToiDa) {
        this.maPGG = maPGG;
        this.maChuSoPGG = maChuSoPGG;
        this.giaTriGiamGia = giaTriGiamGia;
        this.giaTriApDungToiThieu = giaTriApDungToiThieu;
        this.ngayKetThucApDung = ngayKetThucApDung;
        this.slDaDung = slDaDung;
        this.slToiDa = slToiDa;
    }

    public String getMaPGG() {
        return maPGG;
    }

    public String getMaChuSoPGG() {
        return maChuSoPGG;
    }

    public BigDecimal getGiaTriGiamGia() {
        return giaTriGiamGia;
    }

    public BigDecimal getGiaTriApDungToiThieu() {
        return giaTriApDungToiThieu;
    }

    public LocalDateTime getNgayKetThucApDung() {
        return ngayKetThucApDung;
    }

    public Integer getSlDaDung() {
        return slDaDung;
    }

    public Integer getSlToiDa() {
        return slToiDa;
    }
}
