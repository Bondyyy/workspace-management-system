package com.wms.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class ChuyenKhoanQrUtil {
    public static final String MA_NGAN_HANG_VIET_QR = "VCB";
    public static final String TEN_NGAN_HANG_NHAN = "Vietcombank";
    public static final String SO_TAI_KHOAN_NHAN = "9375037830";
    public static final String CHU_TAI_KHOAN_NHAN = "LAI MOC HUY";

    private ChuyenKhoanQrUtil() {
    }

    public static String taoNoiDungDatCho(String maDatCho) {
        return "SPRINGMNGT DATCHO " + safe(maDatCho);
    }

    public static String taoNoiDungHoaDon(String maHoaDon) {
        return "UIT CW " + safe(maHoaDon);
    }

    public static BigDecimal lamTronTienVnd(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.max(BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP);
    }

    public static String taoVietQrUrl(BigDecimal soTien, String noiDungChuyenKhoan) {
        String amount = lamTronTienVnd(soTien).toPlainString();
        String content = encode(safe(noiDungChuyenKhoan));
        String accountName = encode(CHU_TAI_KHOAN_NHAN);
        return "https://img.vietqr.io/image/"
                + MA_NGAN_HANG_VIET_QR + "-" + SO_TAI_KHOAN_NHAN
                + "-compact2.png?amount=" + amount
                + "&addInfo=" + content
                + "&accountName=" + accountName;
    }

    public static String taoNoiDungQrDuPhong(BigDecimal soTien, String noiDungChuyenKhoan) {
        return "NGAN_HANG=" + TEN_NGAN_HANG_NHAN
                + "|SO_TK=" + SO_TAI_KHOAN_NHAN
                + "|CHU_TK=" + CHU_TAI_KHOAN_NHAN
                + "|SO_TIEN=" + lamTronTienVnd(soTien).toPlainString()
                + "|NOI_DUNG=" + safe(noiDungChuyenKhoan);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
