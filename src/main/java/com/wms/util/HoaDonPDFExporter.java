package com.wms.util;

import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DiscountLine;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.InvoiceLine;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class HoaDonPDFExporter {

    private static BaseFont unicodeBaseFont;
    private static boolean daThuTaiFontUnicode;

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

        Font titleFont = taoFontPdf(18, Font.BOLD);
        Font boldFont = taoFontPdf(12, Font.BOLD);
        Font normalFont = taoFontPdf(12, Font.NORMAL);

        Paragraph title = new Paragraph(pdfText("HÓA ĐƠN THANH TOÁN"), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        document.add(new Paragraph(pdfText("Mã HĐ: " + hoaDon.getMaHoaDon()), boldFont));
        document.add(new Paragraph(pdfText("Khách hàng: " + giaTri(hoaDon.getHoTenKH())), normalFont));
        document.add(new Paragraph(pdfText("Không gian: " + giaTri(hoaDon.getTenKhongGian())), normalFont));
        document.add(new Paragraph(pdfText("Thời gian: " + giaTri(hoaDon.getThoiGianSửDung())), normalFont));
        document.add(new Paragraph(
                pdfText("Số giờ tính tiền: " + String.format(java.util.Locale.US, "%.1f", hoaDon.getTongSoGio()) + " giờ"),
                normalFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.addCell(new Phrase(pdfText("Tên dịch vụ"), boldFont));
        table.addCell(new Phrase(pdfText("SL/Giờ"), boldFont));
        table.addCell(new Phrase(pdfText("Đơn giá"), boldFont));
        table.addCell(new Phrase(pdfText("Thành tiền"), boldFont));

        if (hoaDon.getDongChiPhi() != null) {
            for (InvoiceLine line : hoaDon.getDongChiPhi()) {
                table.addCell(new Phrase(pdfText(giaTri(line.getNoiDung())), normalFont));
                String slStr = line.getNoiDung() != null && line.getNoiDung().startsWith("Thuê")
                        ? String.format(java.util.Locale.US, "%d giờ", line.getSoLuong())
                        : String.valueOf(line.getSoLuong());
                table.addCell(new Phrase(pdfText(slStr), normalFont));
                table.addCell(new Phrase(pdfText(HoaDonGiamGiaUtil.formatTienVnd(line.getDonGia())), normalFont));
                table.addCell(new Phrase(pdfText(HoaDonGiamGiaUtil.formatTienVnd(line.getThanhTien())), normalFont));
            }
        }
        document.add(table);
        document.add(new Paragraph(" "));

        if (hoaDon.getDongVoucher() != null && !hoaDon.getDongVoucher().isEmpty()) {
            for (DiscountLine line : hoaDon.getDongVoucher()) {
                document.add(new Paragraph(pdfText(line.getNoiDung() + ": "
                        + HoaDonGiamGiaUtil.formatTienGiamVnd(line.getSoTienGiam())), normalFont));
            }
        }
        document.add(new Paragraph(pdfText("Số tiền tổng: "
                + HoaDonGiamGiaUtil.formatTienVnd(hoaDon.getSoTienConLai())), normalFont));
        document.add(new Paragraph(pdfText("Hạng thành viên (" + giaTri(hoaDon.getTenHangThanhVien()) + "): "
                + HoaDonGiamGiaUtil.formatTienGiamVnd(hoaDon.getTienGiamHang())), normalFont));
        document.add(new Paragraph(pdfText("Thành tiền: "
                + HoaDonGiamGiaUtil.formatTienVnd(hoaDon.getThanhTien())), boldFont));
        document.add(new Paragraph(pdfText("Phương thức thanh toán: "
                + giaTri(hoaDon.getPhuongThucThanhToan())), normalFont));
        Paragraph end = new Paragraph(pdfText("\n      --- CẢM ƠN QUÝ KHÁCH ---"), normalFont);
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

    private static Font taoFontPdf(int size, int style) {
        BaseFont baseFont = getUnicodeBaseFont();
        if (baseFont != null) {
            return new Font(baseFont, size, style);
        }
        return new Font(Font.FontFamily.HELVETICA, size, style);
    }

    private static String pdfText(String text) {
        if (getUnicodeBaseFont() != null) {
            return text == null ? "" : text;
        }
        return removeAccents(text).replace("VNĐ", "VND");
    }

    private static String giaTri(String value) {
        return value == null ? "" : value;
    }

    private static synchronized BaseFont getUnicodeBaseFont() {
        if (daThuTaiFontUnicode) {
            return unicodeBaseFont;
        }
        daThuTaiFontUnicode = true;

        String windir = System.getenv("WINDIR");
        String[] fontPaths = {
                windir == null ? null : windir + File.separator + "Fonts" + File.separator + "arial.ttf",
                "C:" + File.separator + "Windows" + File.separator + "Fonts" + File.separator + "arial.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation2/LiberationSans-Regular.ttf"
        };

        for (String fontPath : fontPaths) {
            if (fontPath == null) {
                continue;
            }
            File fontFile = new File(fontPath);
            if (!fontFile.exists()) {
                continue;
            }
            try {
                unicodeBaseFont = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                return unicodeBaseFont;
            } catch (Exception ignored) {
                // Thử font tiếp theo nếu font hệ thống hiện tại không nạp được.
            }
        }
        return null;
    }
}
