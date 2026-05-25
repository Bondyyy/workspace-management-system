package com.wms.util;

import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DichVuDaDungDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DongHoaDonJasperDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoaDonJasperExporter {

    private static final DecimalFormat formatTien = new DecimalFormat("#,##0");

    public static void xuatHoaDonA4(Component parentComponent, ThongTinHoaDonDTO hoaDon, double tienGiamGia) throws Exception {
        xuatHoaDonChung(parentComponent, hoaDon, tienGiamGia, "/reports/hoa_don_wms.jrxml", "A4");
    }

    public static void xuatHoaDon80mm(Component parentComponent, ThongTinHoaDonDTO hoaDon, double tienGiamGia) throws Exception {
        xuatHoaDonChung(parentComponent, hoaDon, tienGiamGia, "/reports/hoa_don_receipt_80mm.jrxml", "80mm");
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

            try (InputStream input = HoaDonJasperExporter.class.getResourceAsStream(templatePath)) {
                if (input == null) {
                    throw new Exception("Không tìm thấy template: " + templatePath);
                }

                JasperReport report = JasperCompileManager.compileReport(input);

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

                double tongTienGoc = hoaDon.getTongTien();
                double tienDaTraTruoc = hoaDon.isDaTraTruoc() ? hoaDon.getSoTienDaTraTruoc() : 0;
                double conPhaiThanhToan = Math.max(0, tongTienGoc - tienDaTraTruoc - tienGiamGia);

                params.put("TONG_CONG", formatTien.format(tongTienGoc));
                params.put("DA_TRA_TRUOC", "-" + formatTien.format(tienDaTraTruoc));
                params.put("GIAM_GIA", "-" + formatTien.format(tienGiamGia));
                params.put("CON_PHAI_THANH_TOAN", formatTien.format(conPhaiThanhToan));

                List<DongHoaDonJasperDTO> rows = new ArrayList<>();
                int stt = 1;
                if (hoaDon.getDanhSachDichVu() != null) {
                    for (DichVuDaDungDTO dv : hoaDon.getDanhSachDichVu()) {
                        if (dv.getThanhTien() < 0) {
                            continue; // Bỏ qua dòng âm
                        }
                        
                        String tenDV = dv.getTenDichVu();
                        String soLuongStr = String.valueOf(dv.getSoLuong());
                        
                        if (tenDV.startsWith("Thuê")) {
                            soLuongStr = String.format("%.1fh", (double) dv.getSoLuong());
                            if (hoaDon.isDaTraTruoc()) {
                                tenDV = "Thuê " + hoaDon.getTenKhongGian() + " (đã đặt trước)";
                            }
                        } else if (tenDV.equalsIgnoreCase("Phụ thu quá giờ")) {
                            tenDV = "Gia hạn giờ";
                        }
                        
                        rows.add(new DongHoaDonJasperDTO(
                                String.valueOf(stt++),
                                tenDV,
                                soLuongStr,
                                formatTien.format(dv.getDonGia()),
                                formatTien.format(dv.getThanhTien())
                        ));
                    }
                }

                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
                JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);
                JasperExportManager.exportReportToPdfFile(print, filePath);

                JOptionPane.showMessageDialog(parentComponent, "Xuất hóa đơn " + typeName + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                Desktop.getDesktop().open(new File(filePath));

            }
        }
    }

    private static String giaTri(String value) {
        return value == null ? "" : value;
    }
}
