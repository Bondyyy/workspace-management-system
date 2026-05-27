package com.wms.util;

import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DiscountLine;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class HoaDonGiamGiaUtil {
    private static final DecimalFormat FORMAT_PHAN_TRAM =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));

    private HoaDonGiamGiaUtil() {
    }

    public static ThongTinGiamGia taoThongTinGiamGia(ThongTinHoaDonDTO hoaDon, double tienGiamVoucherFallback) {
        if (hoaDon == null) {
            return ThongTinGiamGia.rong();
        }

        double tienGiamVoucherDatTruoc = 0;
        double tienGiamVoucherTaiQuay = 0;
        String maPGGDatTruoc = null;
        String maChuSoPGGDatTruoc = null;
        String maPGGTaiQuay = null;
        String maChuSoPGGTaiQuay = null;
        for (DiscountLine line : hoaDon.getDongVoucher()) {
            if (line.isDatTruoc()) {
                tienGiamVoucherDatTruoc += Math.max(0, line.getSoTienGiam());
                maPGGDatTruoc = layGiaTriDauTien(maPGGDatTruoc, line.getMaPGG());
                maChuSoPGGDatTruoc = layGiaTriDauTien(maChuSoPGGDatTruoc, line.getMaChuSoPGG());
            } else {
                tienGiamVoucherTaiQuay += Math.max(0, line.getSoTienGiam());
                maPGGTaiQuay = layGiaTriDauTien(maPGGTaiQuay, line.getMaPGG());
                maChuSoPGGTaiQuay = layGiaTriDauTien(maChuSoPGGTaiQuay, line.getMaChuSoPGG());
            }
        }
        if (tienGiamVoucherTaiQuay <= 0 && tienGiamVoucherFallback > 0) {
            tienGiamVoucherTaiQuay = Math.max(0, tienGiamVoucherFallback);
        }

        double phanTramTaiQuay = Math.min(100, Math.max(0, hoaDon.getPhanTramGiamHangThanhVien()));
        double tienGiamHangTaiQuay = Math.max(0, hoaDon.getTienGiamHang());
        double tongTienGiam = Math.max(0,
                tienGiamVoucherDatTruoc + tienGiamVoucherTaiQuay + tienGiamHangTaiQuay);

        return new ThongTinGiamGia(
                maPGGDatTruoc,
                maChuSoPGGDatTruoc,
                tienGiamVoucherDatTruoc,
                0,
                0,
                maPGGTaiQuay,
                maChuSoPGGTaiQuay,
                tienGiamVoucherTaiQuay,
                phanTramTaiQuay,
                tienGiamHangTaiQuay,
                hoaDon.getTenHangThanhVien(),
                tongTienGiam);
    }

    public static double layConPhaiThanhToan(ThongTinHoaDonDTO hoaDon, ThongTinGiamGia giamGia,
            double tienGiamVoucherFallback) {
        if (hoaDon == null) {
            return 0;
        }
        if (hoaDon.getSoTienCanThanhToan() > 0) {
            return Math.max(0, hoaDon.getSoTienCanThanhToan());
        }
        return Math.max(0, hoaDon.getThanhTien());
    }

    public static String formatTienVnd(double value) {
        return InputFormatUtil.formatThousands(Math.max(0, Math.round(value))) + " VNĐ";
    }

    public static String formatTienGiamVnd(double value) {
        if (Math.round(Math.max(0, value)) == 0) {
            return formatTienVnd(0);
        }
        return "-" + formatTienVnd(value);
    }

    public static String formatPhanTram(double value) {
        synchronized (FORMAT_PHAN_TRAM) {
            return FORMAT_PHAN_TRAM.format(Math.max(0, value));
        }
    }

    private static String layGiaTriDauTien(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    public static final class ThongTinGiamGia {
        private final String maPGGDatTruoc;
        private final String maChuSoPGGDatTruoc;
        private final double tienGiamVoucherDatTruoc;
        private final double phanTramGiamHangDatTruoc;
        private final double tienGiamHangDatTruoc;
        private final String maPGGTaiQuay;
        private final String maChuSoPGGTaiQuay;
        private final double tienGiamVoucherTaiQuay;
        private final double phanTramGiamHangTaiQuay;
        private final double tienGiamHangTaiQuay;
        private final String tenHangThanhVien;
        private final double tongTienGiam;

        private static ThongTinGiamGia rong() {
            return new ThongTinGiamGia(null, null, 0, 0, 0, null, null, 0, 0, 0, null, 0);
        }

        private ThongTinGiamGia(String maPGGDatTruoc, String maChuSoPGGDatTruoc,
                double tienGiamVoucherDatTruoc, double phanTramGiamHangDatTruoc,
                double tienGiamHangDatTruoc, String maPGGTaiQuay, String maChuSoPGGTaiQuay,
                double tienGiamVoucherTaiQuay, double phanTramGiamHangTaiQuay,
                double tienGiamHangTaiQuay, String tenHangThanhVien, double tongTienGiam) {
            this.maPGGDatTruoc = maPGGDatTruoc;
            this.maChuSoPGGDatTruoc = maChuSoPGGDatTruoc;
            this.tienGiamVoucherDatTruoc = tienGiamVoucherDatTruoc;
            this.phanTramGiamHangDatTruoc = phanTramGiamHangDatTruoc;
            this.tienGiamHangDatTruoc = tienGiamHangDatTruoc;
            this.maPGGTaiQuay = maPGGTaiQuay;
            this.maChuSoPGGTaiQuay = maChuSoPGGTaiQuay;
            this.tienGiamVoucherTaiQuay = tienGiamVoucherTaiQuay;
            this.phanTramGiamHangTaiQuay = phanTramGiamHangTaiQuay;
            this.tienGiamHangTaiQuay = tienGiamHangTaiQuay;
            this.tenHangThanhVien = tenHangThanhVien;
            this.tongTienGiam = tongTienGiam;
        }

        public boolean coGiamVoucher() {
            return getSoTienGiamVoucher() > 0;
        }

        public boolean coGiamHangThanhVien() {
            return getSoTienGiamHangThanhVien() > 0;
        }

        public boolean coTongGiam() {
            return tongTienGiam > 0;
        }

        public boolean coGiamDatTruoc() {
            return tienGiamVoucherDatTruoc > 0 || tienGiamHangDatTruoc > 0;
        }

        public boolean coGiamTaiQuay() {
            return tienGiamVoucherTaiQuay > 0 || tienGiamHangTaiQuay > 0;
        }

        public String getNhanVoucher() {
            return "GIẢM VOUCHER";
        }

        public String getNhanHangThanhVien() {
            return "GIẢM HẠNG TV";
        }

        public String getNhanVoucherDatTruoc() {
            String maHienThi = layGiaTriDauTien(maChuSoPGGDatTruoc, maPGGDatTruoc);
            return maHienThi == null ? "Voucher đặt trước" : "Voucher đặt trước [" + maHienThi + "]";
        }

        public String getNhanHangDatTruoc() {
            return "Hạng TV đặt trước [" + nhanHang(phanTramGiamHangDatTruoc) + "]";
        }

        public String getNhanVoucherTaiQuay() {
            String maHienThi = layGiaTriDauTien(maChuSoPGGTaiQuay, maPGGTaiQuay);
            return maHienThi == null ? "Voucher tại quán" : "Voucher tại quán [" + maHienThi + "]";
        }

        public String getNhanHangTaiQuay() {
            return "Hạng TV tại quán [" + nhanHang(phanTramGiamHangTaiQuay) + "]";
        }

        public double getSoTienGiamVoucher() {
            return tienGiamVoucherDatTruoc + tienGiamVoucherTaiQuay;
        }

        public double getSoTienGiamHangThanhVien() {
            return tienGiamHangDatTruoc + tienGiamHangTaiQuay;
        }

        public double getTongTienGiam() {
            return tongTienGiam;
        }

        public double getTienGiamVoucherDatTruoc() {
            return tienGiamVoucherDatTruoc;
        }

        public double getTienGiamHangDatTruoc() {
            return tienGiamHangDatTruoc;
        }

        public double getTongGiamDatTruoc() {
            return tienGiamVoucherDatTruoc + tienGiamHangDatTruoc;
        }

        public double getTienGiamVoucherTaiQuay() {
            return tienGiamVoucherTaiQuay;
        }

        public double getTienGiamHangTaiQuay() {
            return tienGiamHangTaiQuay;
        }

        public double getTongGiamTaiQuay() {
            return tienGiamVoucherTaiQuay + tienGiamHangTaiQuay;
        }

        public double getPhanTramGiamHangDatTruoc() {
            return phanTramGiamHangDatTruoc;
        }

        public double getPhanTramGiamHangTaiQuay() {
            return phanTramGiamHangTaiQuay;
        }

        private String nhanHang(double phanTram) {
            String tenHang = tenHangThanhVien == null || tenHangThanhVien.isBlank()
                    ? "Hạng TV"
                    : tenHangThanhVien.trim();
            return tenHang + " - " + HoaDonGiamGiaUtil.formatPhanTram(phanTram) + "%";
        }

        private String layGiaTriDauTien(String... values) {
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    return value.trim();
                }
            }
            return null;
        }
    }
}
