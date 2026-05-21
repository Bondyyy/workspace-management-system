package com.wms.util;

import com.wms.model.TrangChuQuanLy.TongQuan.DoanhThuReportRowDTO;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class DoanhThuJasperReportExporter {

    static final String TEMPLATE_PATH = "/reports/bao_cao_doanh_thu.jrxml";

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,### VND");
    private static final SimpleDateFormat EXPORT_TIME = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private DoanhThuJasperReportExporter() {
    }

    public static void exportPdf(File outputFile, ReportParams params, List<DoanhThuReportRowDTO> rows)
            throws IOException, JRException {
        if (outputFile == null) {
            throw new IllegalArgumentException("File xuất báo cáo không được rỗng.");
        }
        ReportParams safeParams = params == null ? ReportParams.empty() : params;
        JasperReport report = compileTemplate();
        JasperPrint print = JasperFillManager.fillReport(
                report,
                toJasperParams(safeParams, rows),
                new JRBeanCollectionDataSource(toAsciiRows(rows))
        );
        JasperExportManager.exportReportToPdfFile(print, outputFile.getAbsolutePath());
    }

    static JasperReport compileTemplate() throws IOException, JRException {
        try (InputStream input = DoanhThuJasperReportExporter.class.getResourceAsStream(TEMPLATE_PATH)) {
            if (input == null) {
                throw new IOException("Không tìm thấy template JasperReports: " + TEMPLATE_PATH);
            }
            return JasperCompileManager.compileReport(input);
        }
    }

    private static Map<String, Object> toJasperParams(ReportParams params, List<DoanhThuReportRowDTO> rows) {
        Map<String, Object> map = new HashMap<>();
        map.put("REPORT_TITLE", "BAO CAO DOANH THU");
        map.put("TU_NGAY", removeAccents(params.tuNgay()));
        map.put("DEN_NGAY", removeAccents(params.denNgay()));
        map.put("TEN_CHI_NHANH", removeAccents(params.tenChiNhanh()));
        map.put("TONG_DOANH_THU", MONEY_FORMAT.format(params.tongDoanhThu()));
        map.put("SO_GIAO_DICH", String.valueOf(rows == null ? params.soGiaoDich() : rows.size()));
        map.put("NGAY_XUAT", EXPORT_TIME.format(new Date()));
        return map;
    }

    private static List<DoanhThuReportRowDTO> toAsciiRows(List<DoanhThuReportRowDTO> rows) {
        List<DoanhThuReportRowDTO> safeRows = new ArrayList<>();
        if (rows == null) {
            return safeRows;
        }
        for (DoanhThuReportRowDTO row : rows) {
            safeRows.add(new DoanhThuReportRowDTO(
                    removeAccents(row.getMaHoaDon()),
                    removeAccents(row.getNgayLap()),
                    removeAccents(row.getTenKhachHang()),
                    removeAccents(row.getTenChiNhanh()),
                    removeAccents(row.getTenKhongGian()),
                    removeAccents(row.getTongTien()),
                    removeAccents(row.getThanhTien()),
                    removeAccents(row.getPhuongThucThanhToan()),
                    removeAccents(row.getTrangThaiThanhToan())
            ));
        }
        return safeRows;
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

    public record ReportParams(
            String tuNgay,
            String denNgay,
            String tenChiNhanh,
            double tongDoanhThu,
            int soGiaoDich
    ) {
        static ReportParams empty() {
            return new ReportParams("", "", "Tat ca chi nhanh", 0.0, 0);
        }
    }
}
