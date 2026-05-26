package com.wms.util;

import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
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
            return new ThongTinGiamGia(null, null, null, null, 0, 0, 0, 0);
        }

        double tongTien = Math.max(0, hoaDon.getTongTien());
        double soTienGiamVoucherDto = Math.max(0, hoaDon.getSoTienGiamVoucher());
        double soTienGiamVoucherFallback = Math.max(0, tienGiamVoucherFallback);
        boolean dungVoucherFallback = soTienGiamVoucherDto <= 0 && soTienGiamVoucherFallback > 0;
        double soTienGiamVoucher = soTienGiamVoucherDto > 0
                ? soTienGiamVoucherDto
                : Math.min(soTienGiamVoucherFallback, tongTien);

        double phanTramGiamHang = Math.min(100, Math.max(0, hoaDon.getPhanTramGiamHangThanhVien()));
        double soTienGiamHang = Math.max(0, hoaDon.getSoTienGiamHangThanhVien());
        if ((dungVoucherFallback || soTienGiamHang <= 0) && phanTramGiamHang > 0) {
            soTienGiamHang = Math.round(Math.max(0, tongTien - soTienGiamVoucher) * phanTramGiamHang / 100.0);
        }

        double tongTienGiam = Math.max(0, hoaDon.getTongTienGiam());
        if (dungVoucherFallback || tongTienGiam <= 0) {
            tongTienGiam = soTienGiamVoucher + soTienGiamHang;
        }
        tongTienGiam = Math.min(tongTien, Math.max(0, tongTienGiam));

        return new ThongTinGiamGia(
                hoaDon.getMaPGG(),
                hoaDon.getMaVoucher(),
                hoaDon.getMaChuSoPGG(),
                hoaDon.getTenHangThanhVien(),
                phanTramGiamHang,
                soTienGiamVoucher,
                soTienGiamHang,
                tongTienGiam);
    }

    public static double layConPhaiThanhToan(ThongTinHoaDonDTO hoaDon, ThongTinGiamGia giamGia,
            double tienGiamVoucherFallback) {
        if (hoaDon == null) {
            return 0;
        }
        if (tienGiamVoucherFallback > 0 && hoaDon.getSoTienGiamVoucher() <= 0 && giamGia != null) {
            return Math.max(0, hoaDon.getTongTien() - giamGia.getTongTienGiam() - hoaDon.getSoTienDaTraTruoc());
        }
        return Math.max(0, hoaDon.getThanhTien());
    }

    public static String formatTienVnd(double value) {
        return InputFormatUtil.formatThousands(Math.max(0, Math.round(value))) + " VNĐ";
    }

    public static String formatTienGiamVnd(double value) {
        return "-" + formatTienVnd(value);
    }

    public static String formatPhanTram(double value) {
        synchronized (FORMAT_PHAN_TRAM) {
            return FORMAT_PHAN_TRAM.format(Math.max(0, value));
        }
    }

    public static final class ThongTinGiamGia {
        private final String maPGG;
        private final String maVoucher;
        private final String maChuSoPGG;
        private final String tenHangThanhVien;
        private final double phanTramGiamHangThanhVien;
        private final double soTienGiamVoucher;
        private final double soTienGiamHangThanhVien;
        private final double tongTienGiam;

        private ThongTinGiamGia(String maPGG, String maVoucher, String maChuSoPGG, String tenHangThanhVien,
                double phanTramGiamHangThanhVien, double soTienGiamVoucher, double soTienGiamHangThanhVien,
                double tongTienGiam) {
            this.maPGG = maPGG;
            this.maVoucher = maVoucher;
            this.maChuSoPGG = maChuSoPGG;
            this.tenHangThanhVien = tenHangThanhVien;
            this.phanTramGiamHangThanhVien = phanTramGiamHangThanhVien;
            this.soTienGiamVoucher = soTienGiamVoucher;
            this.soTienGiamHangThanhVien = soTienGiamHangThanhVien;
            this.tongTienGiam = tongTienGiam;
        }

        public boolean coGiamVoucher() {
            return soTienGiamVoucher > 0;
        }

        public boolean coGiamHangThanhVien() {
            return soTienGiamHangThanhVien > 0 && phanTramGiamHangThanhVien > 0;
        }

        public boolean coTongGiam() {
            return tongTienGiam > 0;
        }

        public String getNhanVoucher() {
            String maHienThi = layGiaTriDauTien(maVoucher, maChuSoPGG, maPGG);
            return maHienThi == null ? "GIẢM VOUCHER" : "GIẢM VOUCHER [" + maHienThi + "]";
        }

        public String getNhanHangThanhVien() {
            String tenHang = tenHangThanhVien == null || tenHangThanhVien.isBlank()
                    ? "Hạng TV"
                    : tenHangThanhVien.trim();
            return "GIẢM HẠNG TV [" + tenHang + " - "
                    + HoaDonGiamGiaUtil.formatPhanTram(phanTramGiamHangThanhVien) + "%]";
        }

        public double getSoTienGiamVoucher() {
            return soTienGiamVoucher;
        }

        public double getSoTienGiamHangThanhVien() {
            return soTienGiamHangThanhVien;
        }

        public double getTongTienGiam() {
            return tongTienGiam;
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
