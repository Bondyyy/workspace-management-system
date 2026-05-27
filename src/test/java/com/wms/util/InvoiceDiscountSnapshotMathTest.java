package com.wms.util;

import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DiscountLine;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.InvoiceLine;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceDiscountSnapshotMathTest {

    @Test
    void tinhVoucherDatTruocTuDongQuanHe() {
        ThongTinHoaDonDTO hoaDon = hoaDonMoi(200_000, 190_000, 0);
        hoaDon.getDongVoucher().add(new DiscountLine("PGG_DT", "DAT10", "Áp dụng voucher DAT10 (đặt trước)",
                10_000, true));

        HoaDonGiamGiaUtil.ThongTinGiamGia giamGia = HoaDonGiamGiaUtil.taoThongTinGiamGia(hoaDon, 0);

        assertEquals(10_000, giamGia.getTienGiamVoucherDatTruoc(), 0.001);
        assertEquals(10_000, giamGia.getTongTienGiam(), 0.001);
    }

    @Test
    void tinhVoucherTaiQuayVaHangThanhVienSauVoucher() {
        ThongTinHoaDonDTO hoaDon = hoaDonMoi(1_000_000, 882_000, 118_000);
        hoaDon.setSoTienConLai(900_000);
        hoaDon.setTienGiamHang(18_000);
        hoaDon.getDongVoucher().add(new DiscountLine("PGG_TQ", "TQ100", "Áp dụng voucher TQ100",
                100_000, false));

        HoaDonGiamGiaUtil.ThongTinGiamGia giamGia = HoaDonGiamGiaUtil.taoThongTinGiamGia(hoaDon, 0);

        assertEquals(100_000, giamGia.getTienGiamVoucherTaiQuay(), 0.001);
        assertEquals(18_000, giamGia.getTienGiamHangTaiQuay(), 0.001);
        assertEquals(118_000, giamGia.getTongTienGiam(), 0.001);
        assertEquals(882_000, HoaDonGiamGiaUtil.layConPhaiThanhToan(hoaDon, giamGia, 0), 0.001);
    }

    @Test
    void soTienCanThanhToanDungChoHoaDonDatTruocCoPhuThu() {
        ThongTinHoaDonDTO hoaDon = hoaDonMoi(250_000, 230_000, 20_000);
        hoaDon.setSoTienCanThanhToan(30_000);

        HoaDonGiamGiaUtil.ThongTinGiamGia giamGia = HoaDonGiamGiaUtil.taoThongTinGiamGia(hoaDon, 0);

        assertEquals(30_000, HoaDonGiamGiaUtil.layConPhaiThanhToan(hoaDon, giamGia, 0), 0.001);
    }

    private ThongTinHoaDonDTO hoaDonMoi(double tongTien, double thanhTien, double tienGiamHang) {
        ThongTinHoaDonDTO hoaDon = new ThongTinHoaDonDTO();
        hoaDon.getDongChiPhi().add(new InvoiceLine("Thuê KG001", 2, tongTien / 2, tongTien, false));
        hoaDon.setTongTien(tongTien);
        hoaDon.setTongTienGoc(tongTien);
        hoaDon.setSoTienConLai(tongTien);
        hoaDon.setTienGiamHang(tienGiamHang);
        hoaDon.setThanhTien(thanhTien);
        hoaDon.setSoTienCanThanhToan(thanhTien);
        return hoaDon;
    }
}
