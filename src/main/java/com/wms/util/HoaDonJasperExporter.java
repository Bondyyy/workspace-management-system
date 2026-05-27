package com.wms.util;

import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DiscountLine;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DongHoaDonJasperDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.InvoiceLine;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HoaDonJasperExporter {

    private static final Map<String, JasperReport> REPORT_CACHE = new ConcurrentHashMap<>();

    public static void xuatHoaDonA4(Component parentComponent, ThongTinHoaDonDTO hoaDon, double tienGiamGia) throws Exception {
        xuatHoaDonChung(parentComponent, hoaDon, tienGiamGia, "/reports/hoa_don_wms.jrxml", "A4");
    }

    public static void xuatHoaDon80mm(Component parentComponent, ThongTinHoaDonDTO hoaDon, double tienGiamGia) throws Exception {
        xuatHoaDonChung(parentComponent, hoaDon, tienGiamGia, "/reports/hoa_don_receipt_80mm.jrxml", "80mm");
    }

    public static void xuatHoaDonA4ToFile(File file, ThongTinHoaDonDTO hoaDon, double tienGiamGia) throws Exception {
        xuatHoaDonChungToFile(file, hoaDon, tienGiamGia, "/reports/hoa_don_wms.jrxml");
    }

    public static void xuatHoaDon80mmToFile(File file, ThongTinHoaDonDTO hoaDon, double tienGiamGia) throws Exception {
        xuatHoaDonChungToFile(file, hoaDon, tienGiamGia, "/reports/hoa_don_receipt_80mm.jrxml");
    }

    private static void xuatHoaDonChung(Component parentComponent, ThongTinHoaDonDTO hoaDon, double tienGiamGia, String templatePath, String typeName) throws Exception {
        if (hoaDon == null) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file PDF (" + typeName + ")");
        fileChooser.setSelectedFile(new File("HoaDon_" + hoaDon.getMaHoaDon() + "_" + typeName + ".pdf"));

        if (fileChooser.showSaveDialog(parentComponent) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".pdf")) {
                filePath += ".pdf";
            }

            File pdfFile = new File(filePath);
            xuatHoaDonChungToFile(pdfFile, hoaDon, tienGiamGia, templatePath);
            JOptionPane.showMessageDialog(parentComponent, "Xuất hóa đơn " + typeName + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            Desktop.getDesktop().open(pdfFile);
        }
    }

    private static void xuatHoaDonChungToFile(File file, ThongTinHoaDonDTO hoaDon, double tienGiamGia, String templatePath) throws Exception {
        if (hoaDon == null) return;
        if (file == null) {
            throw new IllegalArgumentException("File xuất hóa đơn không được rỗng.");
        }

        long start = System.currentTimeMillis();
        JasperReport report = compileTemplate(templatePath);

        Map<String, Object> params = new HashMap<>();
        params.put("TEN_CHI_NHANH", giaTri(hoaDon.getTenChiNhanh()));
        params.put("MA_HOA_DON", giaTri(hoaDon.getMaHoaDon()));
        String ngayLap = hoaDon.getNgayLapHoaDon();
        if (ngayLap == null || ngayLap.isBlank()) {
            ngayLap = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        }
        params.put("NGAY_LAP", ngayLap);
        params.put("KHACH_HANG", giaTri(hoaDon.getHoTenKH()));
        params.put("TEN_KHONG_GIAN", giaTri(hoaDon.getTenKhongGian()));

        String thoiGian = giaTri(hoaDon.getThoiGianSửDung());
        params.put("THOI_GIAN", thoiGian);
        params.put("TRANG_THAI_THANH_TOAN", giaTri(hoaDon.getTrangThaiThanhToan()));
        params.put("PHUONG_THUC_THANH_TOAN", giaTri(hoaDon.getPhuongThucThanhToan()));

        params.put("TONG_CONG", HoaDonGiamGiaUtil.formatTienVnd(hoaDon.getSoTienConLai()));
        params.put("DA_TRA_TRUOC", "");
        params.put("GIAM_GIA", "");
        params.put("GIAM_VOUCHER_LABEL", "");
        params.put("GIAM_VOUCHER", "");
        params.put("GIAM_HANG_TV_LABEL", "");
        params.put("GIAM_HANG_TV", "");
        params.put("TONG_GIAM", "");
        params.put("CON_PHAI_THANH_TOAN", HoaDonGiamGiaUtil.formatTienVnd(hoaDon.getThanhTien()));

        List<DongHoaDonJasperDTO> rows = new ArrayList<>();
        int stt = 1;
        if (hoaDon.getDongChiPhi() != null) {
            for (InvoiceLine line : hoaDon.getDongChiPhi()) {
                if (line.getThanhTien() < 0) {
                    continue;
                }
                String soLuongStr = line.getNoiDung() != null && line.getNoiDung().startsWith("Thuê")
                        ? line.getSoLuong() + "h"
                        : String.valueOf(line.getSoLuong());
                rows.add(new DongHoaDonJasperDTO(
                        String.valueOf(stt++),
                        line.getNoiDung(),
                        soLuongStr,
                        HoaDonGiamGiaUtil.formatTienVnd(line.getDonGia()),
                        HoaDonGiamGiaUtil.formatTienVnd(line.getThanhTien())
                ));
            }
        }

        if (hoaDon.getDongVoucher() != null) {
            for (DiscountLine line : hoaDon.getDongVoucher()) {
                rows.add(new DongHoaDonJasperDTO("", line.getNoiDung(), "", "",
                        HoaDonGiamGiaUtil.formatTienGiamVnd(line.getSoTienGiam())));
            }
        }
        rows.add(new DongHoaDonJasperDTO("", "Số tiền còn lại", "", "",
                HoaDonGiamGiaUtil.formatTienVnd(hoaDon.getSoTienConLai())));
        rows.add(new DongHoaDonJasperDTO("", "Hạng thành viên (" + giaTri(hoaDon.getTenHangThanhVien()) + ")", "", "",
                HoaDonGiamGiaUtil.formatTienGiamVnd(hoaDon.getTienGiamHang())));
        rows.add(new DongHoaDonJasperDTO("", "Thành tiền", "", "",
                HoaDonGiamGiaUtil.formatTienVnd(hoaDon.getThanhTien())));
        rows.add(new DongHoaDonJasperDTO("", "Phương thức thanh toán", "", "",
                giaTri(hoaDon.getPhuongThucThanhToan())));

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
        JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);
        JasperExportManager.exportReportToPdfFile(print, ensurePdfExtension(file).getAbsolutePath());
        System.out.println("[HoaDonJasperExporter] export PDF mat " + (System.currentTimeMillis() - start) + " ms");
    }

    private static JasperReport compileTemplate(String templatePath) throws Exception {
        JasperReport cached = REPORT_CACHE.get(templatePath);
        if (cached != null) {
            return cached;
        }
        synchronized (REPORT_CACHE) {
            cached = REPORT_CACHE.get(templatePath);
            if (cached != null) {
                return cached;
            }
            try (InputStream input = HoaDonJasperExporter.class.getResourceAsStream(templatePath)) {
                if (input == null) {
                    throw new Exception("Không tìm thấy template: " + templatePath);
                }
                JasperReport report = JasperCompileManager.compileReport(input);
                REPORT_CACHE.put(templatePath, report);
                return report;
            }
        }
    }

    private static File ensurePdfExtension(File file) {
        if (file.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
            return file;
        }
        return new File(file.getAbsolutePath() + ".pdf");
    }

    private static String giaTri(String value) {
        return value == null ? "" : value;
    }
}
