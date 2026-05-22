package com.wms.web.form;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class DatChoForm {
    @NotBlank(message = "Vui lòng chọn không gian.")
    private String maKG;

    @NotNull(message = "Vui lòng chọn thời gian đến.")
    @Future(message = "Thời gian đặt chỗ không hợp lệ. Vui lòng chọn thời gian lớn hơn thời điểm hiện tại.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Vui lòng chọn số giờ sử dụng.")
    @Min(value = 1, message = "Tối thiểu 1 giờ.")
    @Max(value = 8, message = "Tối đa 8 giờ cho bản demo.")
    private Integer durationHours;

    private String voucherCode;
    private String note;

    public String getMaKG() {
        return maKG;
    }

    public void setMaKG(String maKG) {
        this.maKG = maKG;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalDateTime getThoiGianDen() {
        return arrivalTime;
    }

    public void setThoiGianDen(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Integer getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public Integer getSoGioSuDung() {
        return durationHours;
    }

    public void setSoGioSuDung(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public String getMaGiamGia() {
        return voucherCode;
    }

    public void setMaGiamGia(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getGhiChu() {
        return note;
    }

    public void setGhiChu(String note) {
        this.note = note;
    }
}
