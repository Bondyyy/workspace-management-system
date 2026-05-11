package com.wms.model.TrangChuQuanLy.QuanLyHoaDon;

import com.wms.model.TrangChuQuanLy.QuanLyPhieuGiamGia.PhieuGiamGiaDTO;

public class XacNhanPhieuGiamGiaDTO {
    private boolean isValid;
    private String message;
    private PhieuGiamGiaDTO voucherInfo;
    private double discountAmount;
    private String errorCode;

    // Constructor cho trường hợp thành công
    public XacNhanPhieuGiamGiaDTO(boolean isValid, PhieuGiamGiaDTO voucherInfo, double discountAmount) {
        this.isValid = isValid;
        this.voucherInfo = voucherInfo;
        this.discountAmount = discountAmount;
        this.message = isValid ? "[OK] Áp dụng thành công" : "";
    }

    // Constructor cho trường hợp invalid
    public XacNhanPhieuGiamGiaDTO(boolean isValid, String message, String errorCode) {
        this.isValid = isValid;
        this.message = message;
        this.errorCode = errorCode;
        this.voucherInfo = null;
        this.discountAmount = 0;
    }

    // Getters
    public boolean isValid() { return isValid; }
    public String getMessage() { return message; }
    public PhieuGiamGiaDTO getVoucherInfo() { return voucherInfo; }
    public double getDiscountAmount() { return discountAmount; }
    public String getErrorCode() { return errorCode; }

    // Setters
    public void setValid(boolean valid) { isValid = valid; }
    public void setMessage(String message) { this.message = message; }
    public void setVoucherInfo(PhieuGiamGiaDTO voucherInfo) { this.voucherInfo = voucherInfo; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
}
