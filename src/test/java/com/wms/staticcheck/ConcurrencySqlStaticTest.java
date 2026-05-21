package com.wms.staticcheck;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrencySqlStaticTest {

    private static final List<Path> ROOTS = List.of(
            Path.of("Database"),
            Path.of("src", "main", "java")
    );

    private static final Pattern DBMS_SESSION_SLEEP = Pattern.compile("(?i)\\bDBMS_SESSION\\s*\\.\\s*SLEEP\\s*\\(");
    private static final Pattern SHORT_SUCCESS_STATUS = Pattern.compile(
            "(?i)TrangThaiThanhToan\\s*(=|<>|!=)\\s*'Thành công'");
    private static final Pattern NONEXISTENT_STOCK_COLUMN = Pattern.compile("(?i)\\bNhanVienNhap\\b");
    private static final Pattern DEFAULT_IMAGE_IN_SQL = Pattern.compile("(?i)default\\.png");

    @Test
    void productionSqlDoesNotContainDebugSleep() throws IOException {
        assertNoMatches(Path.of("Database", "04_procedures"), DBMS_SESSION_SLEEP,
                "Không được để DBMS_SESSION.SLEEP trong procedure nghiệp vụ production");
    }

    @Test
    void invoicePaymentStatusUsesSchemaConstraintValues() throws IOException {
        assertNoMatches(ROOTS, SHORT_SUCCESS_STATUS,
                "Trạng thái hóa đơn phải dùng giá trị constraint đầy đủ: Đã thanh toán thành công");
    }

    @Test
    void stockImportDoesNotUseNonexistentEmployeeNameColumn() throws IOException {
        assertNoMatches(ROOTS, NONEXISTENT_STOCK_COLUMN,
                "CHUNGTUNHAPKHO không có cột NhanVienNhap; phải lưu MaNV/MaCN theo schema");
    }

    @Test
    void serviceImageBlobDoesNotReceiveStringPlaceholder() throws IOException {
        assertNoMatches(ROOTS, DEFAULT_IMAGE_IN_SQL,
                "DICHVU.HinhAnh là BLOB; không được insert chuỗi default.png vào cột ảnh");
    }

    private static void assertNoMatches(Path root, Pattern pattern, String description) throws IOException {
        assertNoMatches(List.of(root), pattern, description);
    }

    private static void assertNoMatches(List<Path> roots, Pattern pattern, String description) throws IOException {
        List<String> violations = new ArrayList<>();

        for (Path root : roots) {
            if (!Files.exists(root)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(root)) {
                for (Path path : files.filter(Files::isRegularFile)
                        .filter(ConcurrencySqlStaticTest::isCheckedFile)
                        .toList()) {
                    String content = Files.readString(path, StandardCharsets.UTF_8);
                    String[] lines = content.split("\\R", -1);
                    for (int i = 0; i < lines.length; i++) {
                        if (pattern.matcher(stripLineComment(lines[i])).find()) {
                            violations.add(path + ":" + (i + 1) + ": " + lines[i].trim());
                        }
                    }
                }
            }
        }

        assertThat(violations).as(description).isEmpty();
    }

    private static boolean isCheckedFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(".sql") || fileName.endsWith(".java");
    }

    private static String stripLineComment(String line) {
        int commentStart = line.indexOf("--");
        return commentStart >= 0 ? line.substring(0, commentStart) : line;
    }
}
