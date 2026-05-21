package com.wms.web.form;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class DatChoForm {
    @NotBlank(message = "Vui long chon khong gian.")
    private String maKG;

    @NotNull(message = "Vui long chon thoi gian den.")
    @Future(message = "Thoi gian den phai o tuong lai.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Vui long chon so gio su dung.")
    @Min(value = 1, message = "Toi thieu 1 gio.")
    @Max(value = 8, message = "Toi da 8 gio cho ban demo.")
    private Integer durationHours;

    private String voucherCode;
    private String note;

    public String getMaKG() {
        return maKG;
    }

    public void setMaKG(String maKG) {
        this.maKG = maKG;
    }

    public LocalDateTime getThoiGianDen() {
        return arrivalTime;
    }

    public void setThoiGianDen(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Integer getSoGioSuDung() {
        return durationHours;
    }

    public void setSoGioSuDung(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public String getMaGiamGia() {
        return voucherCode;
    }

    public void setMaGiamGia(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public String getGhiChu() {
        return note;
    }

    public void setGhiChu(String note) {
        this.note = note;
    }
}
