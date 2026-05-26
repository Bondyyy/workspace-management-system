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
            return ThongTinGiamGia.rong();
        }

        double tongTienGoc = Math.max(0, hoaDon.getTongTienGoc() > 0 ? hoaDon.getTongTienGoc() : hoaDon.getTongTien());
        double tienGocPhatSinh = Math.max(0, hoaDon.getTienGocPhatSinh());
        if (tienGocPhatSinh <= 0) {
            tienGocPhatSinh = Math.max(0, tongTienGoc - hoaDon.getTienGocDatTruoc());
        }

        boolean coSnapshotDatTruoc = hoaDon.getSoTienDaTraTruoc() > 0
                || hoaDon.getTienGocDatTruoc() > 0
                || coGiaTri(hoaDon.getMaPGGDatTruoc());

        double tienGiamVoucherDatTruoc = Math.max(0, hoaDon.getTienGiamVoucherDatTruoc());
        double tienGiamVoucherTaiQuay = Math.max(0, hoaDon.getTienGiamVoucherTaiQuay());
        double tongVoucherCu = Math.max(0, hoaDon.getSoTienGiamVoucher());
        if (tienGiamVoucherDatTruoc <= 0 && tienGiamVoucherTaiQuay <= 0 && tongVoucherCu > 0) {
            if (coSnapshotDatTruoc && coGiaTri(hoaDon.getMaPGGDatTruoc())) {
                tienGiamVoucherDatTruoc = tongVoucherCu;
            } else {
                tienGiamVoucherTaiQuay = Math.min(tongVoucherCu,
                        tienGocPhatSinh > 0 ? tienGocPhatSinh : tongTienGoc);
            }
        }
        if (tienGiamVoucherTaiQuay <= 0 && tienGiamVoucherFallback > 0) {
            double nenApVoucher = tienGocPhatSinh > 0 ? tienGocPhatSinh : tongTienGoc;
            tienGiamVoucherTaiQuay = Math.min(Math.max(0, tienGiamVoucherFallback), nenApVoucher);
        }

        double phanTramDatTruoc = Math.min(100, Math.max(0, hoaDon.getPhanTramGiamHangTVDatTruoc()));
        double phanTramTaiQuay = Math.min(100, Math.max(0, hoaDon.getPhanTramGiamHangTVTaiQuay()));
        if (phanTramTaiQuay <= 0) {
            phanTramTaiQuay = Math.min(100, Math.max(0, hoaDon.getPhanTramGiamHangThanhVien()));
        }

        double tienGiamHangDatTruoc = Math.max(0, hoaDon.getTienGiamHangTVDatTruoc());
        double tienGiamHangTaiQuay = Math.max(0, hoaDon.getTienGiamHangTVTaiQuay());
        double tongHangCu = Math.max(0, hoaDon.getSoTienGiamHangThanhVien());
        if (tienGiamHangDatTruoc <= 0 && tienGiamHangTaiQuay <= 0 && tongHangCu > 0) {
            if (coSnapshotDatTruoc && hoaDon.getTienGocDatTruoc() > 0) {
                tienGiamHangDatTruoc = tongHangCu;
            } else {
                tienGiamHangTaiQuay = tongHangCu;
            }
        }
        if (tienGiamVoucherFallback > 0 && tienGocPhatSinh > 0 && phanTramTaiQuay > 0) {
            tienGiamHangTaiQuay = Math.round(Math.max(0, tienGocPhatSinh - tienGiamVoucherTaiQuay)
                    * phanTramTaiQuay / 100.0);
        }

        double tongTienGiam = Math.max(0, hoaDon.getTongTienGiam());
        double tongTuSnapshot = tienGiamVoucherDatTruoc + tienGiamHangDatTruoc
                + tienGiamVoucherTaiQuay + tienGiamHangTaiQuay;
        if (tongTienGiam <= 0 || tienGiamVoucherFallback > 0) {
            tongTienGiam = tongTuSnapshot;
        }
        tongTienGiam = Math.min(tongTienGoc, Math.max(0, tongTienGiam));

        return new ThongTinGiamGia(
                hoaDon.getMaPGGDatTruoc(),
                hoaDon.getMaChuSoPGGDatTruoc(),
                tienGiamVoucherDatTruoc,
                phanTramDatTruoc,
                tienGiamHangDatTruoc,
                hoaDon.getMaPGGTaiQuay(),
                hoaDon.getMaChuSoPGGTaiQuay(),
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
        if (tienGiamVoucherFallback > 0 && giamGia != null) {
            double tongTienGoc = hoaDon.getTongTienGoc() > 0 ? hoaDon.getTongTienGoc() : hoaDon.getTongTien();
            return Math.max(0, tongTienGoc - giamGia.getTongTienGiam() - hoaDon.getSoTienDaTraTruoc());
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

    private static boolean coGiaTri(String value) {
        return value != null && !value.isBlank();
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
