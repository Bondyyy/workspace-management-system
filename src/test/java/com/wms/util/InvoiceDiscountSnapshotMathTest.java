package com.wms.util;

import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceDiscountSnapshotMathTest {

    @Test
    void case1_datTruocKhongVoucherKhongHang() {
        ThongTinHoaDonDTO hoaDon = datTruoc(200_000_000, 200_000_000, 0, 0, 0, 0);

        HoaDonGiamGiaUtil.ThongTinGiamGia giamGia = HoaDonGiamGiaUtil.taoThongTinGiamGia(hoaDon, 0);

        assertEquals(200_000_000, hoaDon.getTongTienGoc(), 0.001);
        assertEquals(0, giamGia.getTongTienGiam(), 0.001);
        assertEquals(0, HoaDonGiamGiaUtil.layConPhaiThanhToan(hoaDon, giamGia, 0), 0.001);
    }

    @Test
    void case2_datTruocCoVoucherKhongHang() {
        ThongTinHoaDonDTO hoaDon = datTruoc(200_000_000, 199_990_000, 10_000, 0, 0, 0);

        HoaDonGiamGiaUtil.ThongTinGiamGia giamGia = HoaDonGiamGiaUtil.taoThongTinGiamGia(hoaDon, 0);

        assertEquals(200_000_000, hoaDon.getTongTienGoc(), 0.001);
        assertEquals(10_000, giamGia.getTienGiamVoucherDatTruoc(), 0.001);
        assertEquals(10_000, giamGia.getTongTienGiam(), 0.001);
        assertEquals(0, HoaDonGiamGiaUtil.layConPhaiThanhToan(hoaDon, giamGia, 0), 0.001);
    }

    @Test
    void case3_datTruocCoVoucherVaHangDong() {
        ThongTinHoaDonDTO hoaDon = datTruoc(200_000_000, 195_990_200, 10_000, 2, 3_999_800, 0);

        HoaDonGiamGiaUtil.ThongTinGiamGia giamGia = HoaDonGiamGiaUtil.taoThongTinGiamGia(hoaDon, 0);

        assertEquals(200_000_000, hoaDon.getTongTienGoc(), 0.001);
        assertEquals(10_000, giamGia.getTienGiamVoucherDatTruoc(), 0.001);
        assertEquals(3_999_800, giamGia.getTienGiamHangDatTruoc(), 0.001);
        assertEquals(4_009_800, giamGia.getTongTienGiam(), 0.001);
        assertEquals(0, HoaDonGiamGiaUtil.layConPhaiThanhToan(hoaDon, giamGia, 0), 0.001);
    }

    @Test
    void case4_datTruocCoPhatSinhKhongVoucherTaiQuay() {
        ThongTinHoaDonDTO hoaDon = datTruoc(200_000_000, 195_990_200, 10_000, 2, 3_999_800, 1_000_000);
        hoaDon.setPhanTramGiamHangTVTaiQuay(2);
        hoaDon.setTienGiamHangTVTaiQuay(20_000);
        hoaDon.setTongTienGiam(4_029_800);
        hoaDon.setThanhTien(980_000);

        HoaDonGiamGiaUtil.ThongTinGiamGia giamGia = HoaDonGiamGiaUtil.taoThongTinGiamGia(hoaDon, 0);

        assertEquals(1_000_000, hoaDon.getTienGocPhatSinh(), 0.001);
        assertEquals(0, giamGia.getTienGiamVoucherTaiQuay(), 0.001);
        assertEquals(20_000, giamGia.getTienGiamHangTaiQuay(), 0.001);
        assertEquals(980_000, HoaDonGiamGiaUtil.layConPhaiThanhToan(hoaDon, giamGia, 0), 0.001);
    }

    @Test
    void case5_datTruocCoPhatSinhVaVoucherTaiQuay() {
        ThongTinHoaDonDTO hoaDon = datTruoc(200_000_000, 195_990_200, 10_000, 2, 3_999_800, 1_000_000);
        hoaDon.setMaPGGTaiQuay("PGG_TQ");
        hoaDon.setTienGiamVoucherTaiQuay(100_000);
        hoaDon.setPhanTramGiamHangTVTaiQuay(2);
        hoaDon.setTienGiamHangTVTaiQuay(18_000);
        hoaDon.setTongTienGiam(4_127_800);
        hoaDon.setThanhTien(882_000);

        HoaDonGiamGiaUtil.ThongTinGiamGia giamGia = HoaDonGiamGiaUtil.taoThongTinGiamGia(hoaDon, 0);

        assertEquals(100_000, giamGia.getTienGiamVoucherTaiQuay(), 0.001);
        assertEquals(18_000, giamGia.getTienGiamHangTaiQuay(), 0.001);
        assertEquals(882_000, HoaDonGiamGiaUtil.layConPhaiThanhToan(hoaDon, giamGia, 0), 0.001);
    }

    @Test
    void case6_phienTrucTiepCoVoucherVaHang() {
        ThongTinHoaDonDTO hoaDon = new ThongTinHoaDonDTO();
        hoaDon.setTongTienGoc(1_000_000);
        hoaDon.setTongTien(1_000_000);
        hoaDon.setTienGocPhatSinh(1_000_000);
        hoaDon.setMaPGGTaiQuay("PGG_TQ");
        hoaDon.setTienGiamVoucherTaiQuay(100_000);
        hoaDon.setPhanTramGiamHangTVTaiQuay(2);
        hoaDon.setTienGiamHangTVTaiQuay(18_000);
        hoaDon.setTongTienGiam(118_000);
        hoaDon.setThanhTien(882_000);

        HoaDonGiamGiaUtil.ThongTinGiamGia giamGia = HoaDonGiamGiaUtil.taoThongTinGiamGia(hoaDon, 0);

        assertEquals(118_000, giamGia.getTongTienGiam(), 0.001);
        assertEquals(882_000, HoaDonGiamGiaUtil.layConPhaiThanhToan(hoaDon, giamGia, 0), 0.001);
    }

    private ThongTinHoaDonDTO datTruoc(double tongGoc, double daTraTruoc, double giamVoucher,
            double phanTramHang, double giamHang, double phatSinh) {
        ThongTinHoaDonDTO hoaDon = new ThongTinHoaDonDTO();
        hoaDon.setTongTienGoc(tongGoc + phatSinh);
        hoaDon.setTongTien(tongGoc + phatSinh);
        hoaDon.setTienGocDatTruoc(tongGoc);
        hoaDon.setTienGocPhatSinh(phatSinh);
        hoaDon.setSoTienDaTraTruoc(daTraTruoc);
        hoaDon.setMaPGGDatTruoc(giamVoucher > 0 ? "PGG_DT" : null);
        hoaDon.setTienGiamVoucherDatTruoc(giamVoucher);
        hoaDon.setPhanTramGiamHangTVDatTruoc(phanTramHang);
        hoaDon.setTienGiamHangTVDatTruoc(giamHang);
        hoaDon.setTongTienGiam(giamVoucher + giamHang);
        hoaDon.setThanhTien(Math.max(0, phatSinh));
        return hoaDon;
    }
}
