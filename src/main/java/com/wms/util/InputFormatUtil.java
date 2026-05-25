package com.wms.util;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class InputFormatUtil {
    private static final String FORMATTER_ATTACHED_KEY = "wms.thousandsFormatterAttached";

    private InputFormatUtil() {
    }

    public static void attachThousandsFormatter(JTextField field) {
        if (field == null || Boolean.TRUE.equals(field.getClientProperty(FORMATTER_ATTACHED_KEY))) {
            return;
        }
        Document document = field.getDocument();
        if (document instanceof AbstractDocument abstractDocument) {
            abstractDocument.setDocumentFilter(new ThousandsDocumentFilter(field));
            field.putClientProperty(FORMATTER_ATTACHED_KEY, Boolean.TRUE);
            if (!field.getText().isBlank()) {
                field.setText(formatThousands(field.getText()));
            }
        }
    }

    public static Long getNumberValue(JTextField field) {
        if (field == null) {
            return null;
        }
        String normalized = normalizeNumberText(field.getText());
        if (normalized.isEmpty()) {
            return null;
        }
        return Long.parseLong(normalized);
    }

    public static BigDecimal getBigDecimalValue(JTextField field) {
        if (field == null) {
            return null;
        }
        String normalized = normalizeNumberText(field.getText());
        if (normalized.isEmpty()) {
            return null;
        }
        return new BigDecimal(normalized);
    }

    public static String normalizeNumberText(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        boolean negative = trimmed.startsWith("-");
        String cleaned = trimmed.replaceAll("[^0-9.,]", "");
        if (cleaned.isEmpty()) {
            return negative ? "-" : "";
        }

        int lastDot = cleaned.lastIndexOf('.');
        int lastComma = cleaned.lastIndexOf(',');
        int lastSeparator = Math.max(lastDot, lastComma);
        if (lastSeparator >= 0 && lastSeparator < cleaned.length() - 1) {
            String fraction = cleaned.substring(lastSeparator + 1);
            if (fraction.length() <= 2 && fraction.matches("0+")) {
                cleaned = cleaned.substring(0, lastSeparator);
            }
        }

        String digits = cleaned.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return negative ? "-" : "";
        }
        digits = stripLeadingZeros(digits);
        return negative ? "-" + digits : digits;
    }

    public static String formatThousands(String raw) {
        String normalized = normalizeNumberText(raw);
        if (normalized.isEmpty()) {
            return "";
        }
        if ("-".equals(normalized)) {
            return "-";
        }
        boolean negative = normalized.startsWith("-");
        String digits = negative ? normalized.substring(1) : normalized;
        String grouped = groupDigits(stripLeadingZeros(digits));
        return negative ? "-" + grouped : grouped;
    }

    public static String formatThousands(Number value) {
        if (value == null) {
            return "";
        }
        BigDecimal decimal;
        if (value instanceof BigDecimal bigDecimal) {
            decimal = bigDecimal;
        } else {
            decimal = new BigDecimal(value.toString());
        }
        decimal = decimal.setScale(0, RoundingMode.HALF_UP);
        return formatThousands(decimal.toPlainString());
    }

    private static String stripLeadingZeros(String digits) {
        if (digits == null || digits.isEmpty()) {
            return "";
        }
        String stripped = digits.replaceFirst("^0+(?!$)", "");
        return stripped.isEmpty() ? "0" : stripped;
    }

    private static String groupDigits(String digits) {
        StringBuilder result = new StringBuilder();
        int count = 0;
        for (int i = digits.length() - 1; i >= 0; i--) {
            if (count > 0 && count % 3 == 0) {
                result.append('.');
            }
            result.append(digits.charAt(i));
            count++;
        }
        return result.reverse().toString();
    }

    private static int countDigits(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    private static int caretPositionAfterDigits(String formatted, int digitsBeforeCaret) {
        if (formatted == null || formatted.isEmpty()) {
            return 0;
        }
        if (digitsBeforeCaret <= 0) {
            return formatted.startsWith("-") ? 1 : 0;
        }
        int seen = 0;
        for (int i = 0; i < formatted.length(); i++) {
            if (Character.isDigit(formatted.charAt(i))) {
                seen++;
                if (seen == digitsBeforeCaret) {
                    return i + 1;
                }
            }
        }
        return formatted.length();
    }

    private static final class ThousandsDocumentFilter extends DocumentFilter {
        private final JTextField field;
        private boolean formatting;

        private ThousandsDocumentFilter(JTextField field) {
            this.field = field;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            replace(fb, offset, length, "", null);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (formatting) {
                fb.replace(offset, length, text, attrs);
                return;
            }

            Document doc = fb.getDocument();
            String current = doc.getText(0, doc.getLength());
            String replacement = text == null ? "" : text;
            String candidate = current.substring(0, offset)
                    + replacement
                    + current.substring(offset + length);
            int digitsBeforeCaret = countDigits(current.substring(0, offset) + replacement);
            String formatted = formatThousands(candidate);
            int caretPosition = caretPositionAfterDigits(formatted, digitsBeforeCaret);

            formatting = true;
            try {
                fb.replace(0, doc.getLength(), formatted, attrs);
            } finally {
                formatting = false;
            }

            SwingUtilities.invokeLater(() -> field.setCaretPosition(Math.min(caretPosition, field.getText().length())));
        }
    }
}
