package com.wms.util;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

public final class DateInputUtil {
    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final String DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm";
    public static final String TIME_PATTERN = "HH:mm";

    private static final String DATE_ATTACHED_KEY = "wms.datePickerAttached";
    private static final String DATE_TIME_ATTACHED_KEY = "wms.dateTimePickerAttached";
    private static final String TIME_ATTACHED_KEY = "wms.timeFormatterAttached";

    private static final DateTimeFormatter DATE_FORMATTER =
            new DateTimeFormatterBuilder().appendPattern("dd/MM/uuuu")
                    .toFormatter(Locale.ROOT)
                    .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            new DateTimeFormatterBuilder().appendPattern("dd/MM/uuuu HH:mm")
                    .toFormatter(Locale.ROOT)
                    .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_FORMATTER =
            new DateTimeFormatterBuilder().appendPattern("HH:mm")
                    .toFormatter(Locale.ROOT)
                    .withResolverStyle(ResolverStyle.STRICT);

    private DateInputUtil() {
    }

    public static void attachDatePicker(JTextField field) {
        if (field == null || Boolean.TRUE.equals(field.getClientProperty(DATE_ATTACHED_KEY))) {
            return;
        }
        field.putClientProperty(DATE_ATTACHED_KEY, Boolean.TRUE);
        field.setToolTipText("Chọn ngày hoặc nhập theo định dạng " + DATE_PATTERN);
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && field.isEnabled() && field.isEditable()) {
                    showDatePopup(field);
                }
            }
        });
        field.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke("F4"), "wms.showDatePicker");
        field.getActionMap().put("wms.showDatePicker", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showDatePopup(field);
            }
        });
    }

    public static void attachDateTimePicker(JTextField field) {
        if (field == null || Boolean.TRUE.equals(field.getClientProperty(DATE_TIME_ATTACHED_KEY))) {
            return;
        }
        field.putClientProperty(DATE_TIME_ATTACHED_KEY, Boolean.TRUE);
        field.setToolTipText("Chọn ngày giờ hoặc nhập theo định dạng " + DATE_TIME_PATTERN);
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && field.isEnabled() && field.isEditable()) {
                    showDateTimePopup(field);
                }
            }
        });
        field.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke("F4"), "wms.showDateTimePicker");
        field.getActionMap().put("wms.showDateTimePicker", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showDateTimePopup(field);
            }
        });
    }

    public static void attachTimeFormatter(JTextField field) {
        if (field == null || Boolean.TRUE.equals(field.getClientProperty(TIME_ATTACHED_KEY))) {
            return;
        }
        field.putClientProperty(TIME_ATTACHED_KEY, Boolean.TRUE);
        field.setToolTipText("Nhập giờ theo định dạng " + TIME_PATTERN);
        Document document = field.getDocument();
        if (document instanceof AbstractDocument abstractDocument) {
            abstractDocument.setDocumentFilter(new TimeDocumentFilter(field));
        }
    }

    public static LocalDate parseDate(String text, String fieldName) {
        String value = normalize(text);
        if (value.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            if (value.matches("\\d{2}/\\d{2}/\\d{4}")) {
                throw new IllegalArgumentException(fieldName + " không hợp lệ.");
            }
            throw new IllegalArgumentException(fieldName + " phải đúng định dạng " + DATE_PATTERN + ".");
        }
    }

    public static LocalDateTime parseDateTime(String text, String fieldName) {
        String value = normalize(text);
        if (value.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " phải đúng định dạng " + DATE_TIME_PATTERN + ".");
        }
    }

    public static LocalTime parseTime(String text, String fieldName) {
        String value = normalize(text);
        if (value.isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(value, TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " phải đúng định dạng " + TIME_PATTERN + ".");
        }
    }

    public static LocalDate requireDate(String text, String fieldName, String requiredMessage) {
        if (normalize(text).isEmpty()) {
            throw new IllegalArgumentException(requiredMessage);
        }
        return parseDate(text, fieldName);
    }

    public static LocalDateTime requireDateTime(String text, String fieldName, String requiredMessage) {
        if (normalize(text).isEmpty()) {
            throw new IllegalArgumentException(requiredMessage);
        }
        return parseDateTime(text, fieldName);
    }

    public static LocalTime requireTime(String text, String fieldName, String requiredMessage) {
        if (normalize(text).isEmpty()) {
            throw new IllegalArgumentException(requiredMessage);
        }
        return parseTime(text, fieldName);
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "" : DATE_FORMATTER.format(date);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME_FORMATTER.format(dateTime);
    }

    public static String formatTime(LocalTime time) {
        return time == null ? "" : TIME_FORMATTER.format(time);
    }

    public static java.sql.Date toSqlDate(LocalDate date) {
        return date == null ? null : java.sql.Date.valueOf(date);
    }

    public static Timestamp toSqlTimestamp(LocalDateTime dateTime) {
        return dateTime == null ? null : Timestamp.valueOf(dateTime);
    }

    public static LocalDate toLocalDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return new java.sql.Date(date.getTime()).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(java.util.Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate().atStartOfDay();
        }
        return LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
    }

    public static String formatDate(java.util.Date date) {
        return formatDate(toLocalDate(date));
    }

    public static String formatDateTime(java.util.Date date) {
        return formatDateTime(toLocalDateTime(date));
    }

    private static String normalize(String text) {
        return text == null ? "" : text.trim();
    }

    private static void showDatePopup(JTextField field) {
        LocalDate selected = parseExistingDate(field.getText());
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        popup.add(new CalendarPanel(selected, false, field, popup));
        popup.show(field, 0, field.getHeight());
    }

    private static void showDateTimePopup(JTextField field) {
        LocalDateTime selected = parseExistingDateTime(field.getText());
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        popup.add(new CalendarPanel(selected.toLocalDate(), true, field, popup, selected.toLocalTime()));
        popup.show(field, 0, field.getHeight());
    }

    private static LocalDate parseExistingDate(String text) {
        try {
            LocalDate date = parseDate(text, "Ngày");
            return date == null ? LocalDate.now() : date;
        } catch (IllegalArgumentException ex) {
            return LocalDate.now();
        }
    }

    private static LocalDateTime parseExistingDateTime(String text) {
        try {
            LocalDateTime dateTime = parseDateTime(text, "Thời gian");
            return dateTime == null ? LocalDateTime.now().withSecond(0).withNano(0) : dateTime;
        } catch (IllegalArgumentException ex) {
            return LocalDateTime.now().withSecond(0).withNano(0);
        }
    }

    private static final class CalendarPanel extends JPanel {
        private final JTextField target;
        private final JPopupMenu popup;
        private final boolean includeTime;
        private final JSpinner hourSpinner;
        private final JSpinner minuteSpinner;
        private YearMonth month;
        private LocalDate selectedDate;
        private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        private final JPanel daysPanel = new JPanel(new GridLayout(0, 7, 2, 2));

        private CalendarPanel(LocalDate selectedDate, boolean includeTime, JTextField target, JPopupMenu popup) {
            this(selectedDate, includeTime, target, popup, LocalTime.now().withSecond(0).withNano(0));
        }

        private CalendarPanel(LocalDate selectedDate, boolean includeTime, JTextField target,
                              JPopupMenu popup, LocalTime selectedTime) {
            super(new BorderLayout(6, 6));
            this.target = target;
            this.popup = popup;
            this.includeTime = includeTime;
            this.selectedDate = selectedDate == null ? LocalDate.now() : selectedDate;
            this.month = YearMonth.from(this.selectedDate);
            this.hourSpinner = new JSpinner(new SpinnerNumberModel(selectedTime.getHour(), 0, 23, 1));
            this.minuteSpinner = new JSpinner(new SpinnerNumberModel(selectedTime.getMinute(), 0, 59, 1));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            setPreferredSize(includeTime ? new Dimension(280, 310) : new Dimension(280, 270));
            build();
        }

        private void build() {
            JPanel header = new JPanel(new BorderLayout(4, 0));
            header.setOpaque(false);
            JButton previous = smallButton("<");
            JButton next = smallButton(">");
            previous.addActionListener(e -> {
                month = month.minusMonths(1);
                renderDays();
            });
            next.addActionListener(e -> {
                month = month.plusMonths(1);
                renderDays();
            });
            monthLabel.setFont(monthLabel.getFont().deriveFont(java.awt.Font.BOLD, 13f));
            header.add(previous, BorderLayout.WEST);
            header.add(monthLabel, BorderLayout.CENTER);
            header.add(next, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            daysPanel.setOpaque(false);
            add(daysPanel, BorderLayout.CENTER);

            JPanel footer = new JPanel(new BorderLayout(4, 4));
            footer.setOpaque(false);
            if (includeTime) {
                JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
                timePanel.setOpaque(false);
                timePanel.add(new JLabel("Giờ"));
                timePanel.add(hourSpinner);
                timePanel.add(new JLabel(":"));
                timePanel.add(minuteSpinner);
                footer.add(timePanel, BorderLayout.NORTH);
            }

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            actions.setOpaque(false);
            JButton today = smallButton("Hôm nay");
            JButton done = smallButton("Xong");
            today.addActionListener(e -> {
                selectedDate = LocalDate.now();
                month = YearMonth.from(selectedDate);
                applyValue();
            });
            done.addActionListener(e -> applyValue());
            actions.add(today);
            actions.add(done);
            footer.add(actions, BorderLayout.SOUTH);
            add(footer, BorderLayout.SOUTH);
            renderDays();
        }

        private JButton smallButton(String text) {
            JButton button = new JButton(text);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220)),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)));
            button.setBackground(Color.WHITE);
            return button;
        }

        private void renderDays() {
            daysPanel.removeAll();
            monthLabel.setText("Tháng " + month.getMonthValue() + "/" + month.getYear());
            for (String header : new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"}) {
                JLabel label = new JLabel(header, SwingConstants.CENTER);
                label.setForeground(new Color(95, 95, 95));
                daysPanel.add(label);
            }

            LocalDate firstDay = month.atDay(1);
            int firstOffset = firstDay.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            for (int i = 0; i < firstOffset; i++) {
                daysPanel.add(new JLabel(""));
            }

            LocalDate today = LocalDate.now();
            for (int day = 1; day <= month.lengthOfMonth(); day++) {
                LocalDate current = month.atDay(day);
                JButton button = smallButton(String.valueOf(day));
                button.setHorizontalAlignment(SwingConstants.CENTER);
                if (current.equals(today)) {
                    button.setBorder(BorderFactory.createLineBorder(new Color(235, 94, 141), 2));
                }
                if (current.equals(selectedDate)) {
                    button.setBackground(new Color(235, 94, 141));
                    button.setForeground(Color.WHITE);
                }
                button.addActionListener(e -> {
                    selectedDate = current;
                    if (includeTime) {
                        renderDays();
                    } else {
                        applyValue();
                    }
                });
                daysPanel.add(button);
            }
            revalidate();
            repaint();
        }

        private void applyValue() {
            if (includeTime) {
                LocalTime time = LocalTime.of((Integer) hourSpinner.getValue(), (Integer) minuteSpinner.getValue());
                target.setText(formatDateTime(LocalDateTime.of(selectedDate, time)));
            } else {
                target.setText(formatDate(selectedDate));
            }
            popup.setVisible(false);
            Component parent = target.getParent();
            if (parent != null) {
                parent.repaint();
            }
        }
    }

    private static final class TimeDocumentFilter extends DocumentFilter {
        private final JTextField field;
        private boolean formatting;

        private TimeDocumentFilter(JTextField field) {
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

            Document document = fb.getDocument();
            String current = document.getText(0, document.getLength());
            String replacement = text == null ? "" : text;
            String candidate = current.substring(0, offset)
                    + replacement
                    + current.substring(offset + length);
            String digits = candidate.replaceAll("\\D", "");
            if (digits.length() > 4) {
                digits = digits.substring(0, 4);
            }
            String formatted = digits.length() <= 2
                    ? digits
                    : digits.substring(0, 2) + ":" + digits.substring(2);

            formatting = true;
            try {
                fb.replace(0, document.getLength(), formatted, attrs);
            } finally {
                formatting = false;
            }
            SwingUtilities.invokeLater(() -> field.setCaretPosition(field.getText().length()));
        }
    }
}
