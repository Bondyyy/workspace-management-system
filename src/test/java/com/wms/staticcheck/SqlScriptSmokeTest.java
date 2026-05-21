package com.wms.staticcheck;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SqlScriptSmokeTest {

    private static final Path DATABASE_ROOT = Path.of("Database");
    private static final Pattern BLOCK_START = Pattern.compile("(?i)\\bCREATE\\s+OR\\s+REPLACE\\s+(TRIGGER|FUNCTION|PROCEDURE)\\b");
    private static final Pattern SLASH_LINE = Pattern.compile("(?m)^\\s*/\\s*$");

    @Test
    void sqlScriptsPassBasicStatementSmokeChecks() throws IOException {
        List<String> violations = new ArrayList<>();

        if (!Files.exists(DATABASE_ROOT)) {
            return;
        }

        try (Stream<Path> files = Files.walk(DATABASE_ROOT)) {
            for (Path path : files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .toList()) {
                String content = Files.readString(path, StandardCharsets.UTF_8);
                checkCommitTerminators(path, content, violations);
                checkAlterTableAddTerminators(path, content, violations);
                checkCreateOrReplaceBlocks(path, content, violations);
            }
        }

        assertThat(violations).as("SQL script smoke check violations").isEmpty();
    }

    private static void checkCommitTerminators(Path path, String content, List<String> violations) {
        String[] lines = content.split("\\R", -1);
        for (int i = 0; i < lines.length; i++) {
            String sql = stripLineComment(lines[i]).trim();
            if (sql.equalsIgnoreCase("COMMIT")) {
                violations.add(path + ":" + (i + 1) + ": COMMIT must end with semicolon");
            }
        }
    }

    private static void checkAlterTableAddTerminators(Path path, String content, List<String> violations) {
        List<String> statements = splitSqlStatements(content);
        for (String statement : statements) {
            String trimmed = statement.stripLeading();
            if (Pattern.compile("(?is)^ALTER\\s+TABLE\\b.*\\bADD\\b").matcher(trimmed).find()
                    && !trimmed.stripTrailing().endsWith(";")) {
                violations.add(path + ": ALTER TABLE ... ADD statement must end with semicolon");
            }
        }
    }

    private static void checkCreateOrReplaceBlocks(Path path, String content, List<String> violations) {
        String withoutComments = stripLineComments(content);
        Matcher matcher = BLOCK_START.matcher(withoutComments);
        List<Integer> starts = new ArrayList<>();
        while (matcher.find()) {
            starts.add(matcher.start());
        }

        for (int i = 0; i < starts.size(); i++) {
            int start = starts.get(i);
            int end = i + 1 < starts.size() ? starts.get(i + 1) : withoutComments.length();
            String block = withoutComments.substring(start, end);
            if (!Pattern.compile("(?is)\\bEND\\b[^;]*;").matcher(block).find()
                    || !SLASH_LINE.matcher(block).find()) {
                violations.add(path + ": CREATE OR REPLACE block must end with END; followed by / on its own line");
            }
        }
    }

    private static List<String> splitSqlStatements(String content) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : content.split("\\R", -1)) {
            String sqlLine = stripLineComment(line);
            if (sqlLine.isBlank()) {
                continue;
            }
            current.append(sqlLine).append(System.lineSeparator());
            if (sqlLine.trim().endsWith(";")) {
                statements.add(current.toString());
                current.setLength(0);
            }
        }
        if (!current.isEmpty()) {
            statements.add(current.toString());
        }
        return statements;
    }

    private static String stripLineComments(String content) {
        StringBuilder result = new StringBuilder();
        for (String line : content.split("\\R", -1)) {
            result.append(stripLineComment(line)).append(System.lineSeparator());
        }
        return result.toString();
    }

    private static String stripLineComment(String line) {
        int commentStart = line.indexOf("--");
        return commentStart >= 0 ? line.substring(0, commentStart) : line;
    }
}
