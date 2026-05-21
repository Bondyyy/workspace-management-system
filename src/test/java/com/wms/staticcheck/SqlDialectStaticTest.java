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

class SqlDialectStaticTest {

    private static final Path SOURCE_ROOT = Path.of("src", "main", "java");

    private static final List<ForbiddenPattern> FORBIDDEN_PATTERNS = List.of(
            new ForbiddenPattern("SELECT TOP", Pattern.compile("(?is)\\bSELECT\\s+TOP\\s+\\d+\\b")),
            new ForbiddenPattern("GETDATE()", Pattern.compile("\\bGETDATE\\s*\\(")),
            new ForbiddenPattern("ISNULL(", Pattern.compile("\\bISNULL\\s*\\(")),
            new ForbiddenPattern("SQL Server Unicode literal N'", Pattern.compile("(?iu)(?<![\\p{L}\\p{N}_])N\\s*'"))
    );

    @Test
    void javaSqlDoesNotUseSqlServerDialectPatterns() throws IOException {
        List<String> violations = new ArrayList<>();

        try (Stream<Path> files = Files.walk(SOURCE_ROOT)) {
            for (Path path : files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList()) {
                String content = Files.readString(path, StandardCharsets.UTF_8);
                for (ForbiddenPattern forbiddenPattern : FORBIDDEN_PATTERNS) {
                    if (forbiddenPattern.pattern().matcher(content).find()) {
                        violations.add(path + " contains " + forbiddenPattern.name());
                    }
                }
            }
        }

        assertThat(violations).as("SQL Server dialect patterns found in Oracle Java SQL").isEmpty();
    }

    private record ForbiddenPattern(String name, Pattern pattern) {
    }
}
