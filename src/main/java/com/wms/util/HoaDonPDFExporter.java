package com.wms.util;

import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DichVuDaDungDTO;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public class HoaDonPDFExporter {

    private static final NumberFormat formatTien = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public static void xuatHoaDonPDF(Component parentComponent, ThongTinHoaDonDTO hoaDon, double tienGiamGia) {
        if (hoaDon == null)
            return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file PDF");
        fileChooser.setSelectedFile(new File("HoaDon_" + hoaDon.getMaHoaDon() + ".pdf"));

        int userSelection = fileChooser.showSaveDialog(parentComponent);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".pdf")) {
                filePath += ".pdf";
            }

            try {
                File pdfFile = new File(filePath);
                xuatHoaDonPDFToFile(pdfFile, hoaDon, tienGiamGia);
                JOptionPane.showMessageDialog(parentComponent, "Đã xuất PDF thành công: " + filePath, "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);

                Desktop.getDesktop().open(pdfFile);

            } catch (Exception ex) {
                MessageUtil.showError(parentComponent, ex);
            }
        }
    }

    public static void xuatHoaDonPDFToFile(File file, ThongTinHoaDonDTO hoaDon, double tienGiamGia) throws Exception {
        if (hoaDon == null) {
            return;
        }
        File pdfFile = ensurePdfExtension(file);
        long start = System.currentTimeMillis();

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

        Paragraph title = new Paragraph("HOA DON THANH TOAN", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Ma HD: " + hoaDon.getMaHoaDon(), boldFont));
        document.add(new Paragraph("Khach hang: " + removeAccents(hoaDon.getHoTenKH()), normalFont));
        document.add(new Paragraph("Khong gian: " + removeAccents(hoaDon.getTenKhongGian()), normalFont));
        document.add(new Paragraph("Thoi gian: " + removeAccents(hoaDon.getThoiGianSửDung()), normalFont));
        document.add(new Paragraph(
                "So gio tinh tien: " + String.format(Locale.US, "%.1f", hoaDon.getTongSoGio()) + " gio",
                normalFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.addCell(new Phrase("Ten dich vu", boldFont));
        table.addCell(new Phrase("SL/Gio", boldFont));
        table.addCell(new Phrase("Don gia (VND)", boldFont));
        table.addCell(new Phrase("Thanh tien (VND)", boldFont));

        // Danh sách dịch vụ, trong đó dòng đầu tiên là tiền thuê không gian thực tế
        if (hoaDon.getDanhSachDichVu() != null) {
            for (DichVuDaDungDTO dv : hoaDon.getDanhSachDichVu()) {
                table.addCell(new Phrase(removeAccents(dv.getTenDichVu()), normalFont));
                String slStr = dv.getTenDichVu().startsWith("Thuê") ? String.format(Locale.US, "%.1f gio", (double) dv.getSoLuong()) : String.valueOf(dv.getSoLuong());
                table.addCell(new Phrase(slStr, normalFont));
                table.addCell(new Phrase(InputFormatUtil.formatThousands(dv.getDonGia()), normalFont));
                table.addCell(new Phrase(InputFormatUtil.formatThousands(dv.getThanhTien()), normalFont));
            }
        }
        document.add(table);
        document.add(new Paragraph(" "));

        double tongTienGoc = hoaDon.getTongTien();
        double tienDaTraTruoc = hoaDon.getSoTienDaTraTruoc();
        double conPhaiThanhToan = Math.max(0, tongTienGoc - tienDaTraTruoc - tienGiamGia);

        document.add(new Paragraph("TONG CONG: " + formatTien.format(tongTienGoc), boldFont));
        if (tienDaTraTruoc > 0) {
            document.add(new Paragraph("DA TRA TRUOC (ONLINE): -" + formatTien.format(tienDaTraTruoc), normalFont));
        }
        if (tienGiamGia > 0) {
            document.add(new Paragraph("GIAM GIA: -" + formatTien.format(tienGiamGia), normalFont));
        }

        document.add(new Paragraph("CON PHAI THANH TOAN: " + formatTien.format(conPhaiThanhToan), boldFont));
        Paragraph end = new Paragraph("\n      --- CAM ON QUY KHACH ---");
        end.setAlignment(Element.ALIGN_CENTER);
        document.add(end);
        document.close();
        System.out.println("[HoaDonPDFExporter] export PDF mat " + (System.currentTimeMillis() - start) + " ms");
    }

    private static File ensurePdfExtension(File file) {
        if (file.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
            return file;
        }
        return new File(file.getAbsolutePath() + ".pdf");
    }

    private static String removeAccents(String str) {
        if (str == null)
            return "";
        try {
            String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
        } catch (Exception e) {
            return str;
        }
    }
}
