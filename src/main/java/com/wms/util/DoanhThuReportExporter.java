package com.wms.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public final class DoanhThuReportExporter {

    public static final String[] CSV_HEADERS = {
            "Mã hóa đơn",
            "Khách hàng",
            "Ngày lập",
            "Tổng tiền",
            "Thành tiền",
            "Phương thức",
            "Trạng thái"
    };

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,### VNĐ");
    private static final SimpleDateFormat EXPORT_TIME = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private DoanhThuReportExporter() {
    }

    public static void xuatCsv(File file, List<Object[]> rows) throws java.io.IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write('\ufeff');
            writer.write(toCsvLine((Object[]) CSV_HEADERS));
            writer.newLine();
            if (rows != null) {
                for (Object[] row : rows) {
                    writer.write(toCsvLine(row));
                    writer.newLine();
                }
            }
        }
    }

    static String toCsvLine(Object[] values) {
        StringBuilder line = new StringBuilder();
        if (values == null) {
            return "";
        }
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(escapeCsv(values[i]));
        }
        return line.toString();
    }

    static String escapeCsv(Object value) {
        String text = value == null ? "" : value.toString();
        boolean mustQuote = text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");
        String escaped = text.replace("\"", "\"\"");
        return mustQuote ? "\"" + escaped + "\"" : escaped;
    }

    public static void xuatPdf(File file, ReportData data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Dữ liệu báo cáo không được rỗng.");
        }

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

        Paragraph title = new Paragraph("BAO CAO DOANH THU", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Khoang ngay: " + removeAccents(data.tuNgay()) + " - " + removeAccents(data.denNgay()), normalFont));
        document.add(new Paragraph("Chi nhanh: " + removeAccents(data.chiNhanh()), normalFont));
        document.add(new Paragraph("Ngay xuat: " + EXPORT_TIME.format(new Date()), normalFont));
        document.add(new Paragraph("Tong doanh thu: " + removeAccents(MONEY_FORMAT.format(data.tongDoanhThu())), boldFont));
        document.add(new Paragraph("So hoa don: " + data.soHoaDon(), boldFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.4f, 2.0f, 1.8f, 1.5f, 1.5f, 1.7f, 2.0f});
        for (String header : CSV_HEADERS) {
            table.addCell(new Phrase(removeAccents(header), boldFont));
        }

        if (data.rows() != null) {
            for (Object[] row : data.rows()) {
                for (int i = 0; i < CSV_HEADERS.length; i++) {
                    Object cell = i < row.length ? row[i] : "";
                    table.addCell(new Phrase(removeAccents(cell == null ? "" : cell.toString()), normalFont));
                }
            }
        }

        document.add(table);
        document.close();
    }

    private static String removeAccents(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(normalized)
                .replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D');
    }

    public record ReportData(
            String tuNgay,
            String denNgay,
            String chiNhanh,
            double tongDoanhThu,
            int soHoaDon,
            List<Object[]> rows
    ) {
    }
}
