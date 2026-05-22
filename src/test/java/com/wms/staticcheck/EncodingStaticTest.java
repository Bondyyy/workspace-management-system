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

class EncodingStaticTest {

    private static final List<Path> ROOTS = List.of(
            Path.of("src", "main", "java"),
            Path.of("src", "main", "resources"),
            Path.of("Database")
    );

    private static final List<String> EXTENSIONS = List.of(".java", ".html", ".css", ".js", ".properties", ".sql", ".xml", ".form");

    private static final Pattern MOJIBAKE = Pattern.compile(String.join("|", List.of(
            "\\u00E1\\u00BA", "\\u00E1\\u00BB", "\\u00C4\\u2018", "\\u00C4\\u0090", "\\u00C6",
            "Kh\\u00C3\\u00B4ng", "L\\u00E1\\u00BB", "Phi\\u00E1", "nh\\u00E1\\u00BA",
            "l\\u00C3\\u00B2ng", "m\\u00C3\\u00A3", "t\\u00C3\\u00AAn", "t\\u00C3\\u00A0i",
            "kh\\u00C3\\u00B4ng", "m\\u00E1\\u00BA", "c\\u00E1\\u00BA", "y\\u00C3\\u00AAu",
            "qu\\u00C3\\u00AAn", "ho\\u00E1\\u00BA"
    )));

    @Test
    void sourceFilesDoNotContainObviousMojibake() throws IOException {
        List<String> violations = new ArrayList<>();

        for (Path root : ROOTS) {
            if (!Files.exists(root)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(root)) {
                for (Path path : files.filter(Files::isRegularFile)
                        .filter(EncodingStaticTest::hasCheckedExtension)
                        .toList()) {
                    String content = Files.readString(path, StandardCharsets.UTF_8);
                    String[] lines = content.split("\\R", -1);
                    for (int i = 0; i < lines.length; i++) {
                        if (MOJIBAKE.matcher(lines[i]).find() && !isWhitelisted(path, lines[i])) {
                            violations.add(path + ":" + (i + 1) + ": " + lines[i].trim());
                        }
                    }
                }
            }
        }

        assertThat(violations).as("Obvious mojibake strings found").isEmpty();
    }

    private static boolean hasCheckedExtension(Path path) {
        String fileName = path.getFileName().toString();
        return EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    private static boolean isWhitelisted(Path path, String line) {
        return path.getFileName().toString().equals("QuanLyDatChoTruocDAO.java")
                && (line.contains("value.contains(") || line.contains("coDauHieuLoiFont"));
    }
}
