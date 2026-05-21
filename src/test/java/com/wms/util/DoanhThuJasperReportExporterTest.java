package com.wms.util;

import com.wms.model.TrangChuQuanLy.TongQuan.DoanhThuReportRowDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoanhThuJasperReportExporterTest {

    @TempDir
    Path tempDir;

    @Test
    void loadCompileVaExportPdfTuDuLieuMauKhongCanOracle() throws Exception {
        assertNotNull(
                DoanhThuJasperReportExporter.class.getResourceAsStream(DoanhThuJasperReportExporter.TEMPLATE_PATH),
                "Template JasperReports phải tồn tại trong classpath."
        );
        assertNotNull(DoanhThuJasperReportExporter.compileTemplate());

        Path output = tempDir.resolve("bao-cao-doanh-thu-jasper.pdf");
        List<DoanhThuReportRowDTO> rows = List.of(new DoanhThuReportRowDTO(
                "HD001",
                "21/05/2026 10:00",
                "Nguyễn Văn A",
                "Chi nhánh Trung tâm",
                "Phòng họp A",
                "1,000,000",
                "900,000",
                "Chuyển khoản",
                "Đã thanh toán thành công"
        ));
        DoanhThuJasperReportExporter.ReportParams params = new DoanhThuJasperReportExporter.ReportParams(
                "01/05/2026",
                "21/05/2026",
                "Tất cả chi nhánh",
                900000,
                rows.size()
        );

        DoanhThuJasperReportExporter.exportPdf(output.toFile(), params, rows);

        assertTrue(Files.exists(output), "File PDF JasperReports phải được tạo.");
        assertTrue(Files.size(output) > 0, "File PDF JasperReports không được rỗng.");
    }
}
