package com.wms.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoanhThuReportExporterTest {

    @TempDir
    Path tempDir;

    @Test
    void xuatCsvTaoFileUtf8BomCoHeaderVaEscapeDung() throws Exception {
        Path output = tempDir.resolve("bao-cao-doanh-thu.csv");
        List<Object[]> rows = List.<Object[]>of(new Object[]{
                "HD001",
                "Nguyễn Văn A, \"VIP\"",
                "21/05/2026 10:00",
                "1,000,000",
                "900,000",
                "Chuyển khoản",
                "Đã thanh toán\nhoàn tất"
        });

        DoanhThuReportExporter.xuatCsv(output.toFile(), rows);

        byte[] bytes = Files.readAllBytes(output);
        assertTrue(bytes.length > 3, "File CSV phải có dữ liệu.");
        assertArrayEquals(
                new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF},
                new byte[]{bytes[0], bytes[1], bytes[2]},
                "CSV cần có UTF-8 BOM để Excel mở tiếng Việt ổn định."
        );

        String csv = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("\ufeffMã hóa đơn,Khách hàng,Ngày lập,Tổng tiền,Thành tiền,Phương thức,Trạng thái"));
        assertTrue(csv.contains("\"Nguyễn Văn A, \"\"VIP\"\"\""));
        assertTrue(csv.contains("\"1,000,000\""));
        assertTrue(csv.contains("\"Đã thanh toán\nhoàn tất\""));
    }
}
