package com.wms.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class BusinessHoursUtil {
    private BusinessHoursUtil() {
    }

    public static LocalTime parseBranchTime(String value, LocalTime fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim();
        if ("24:00".equals(normalized)) {
            normalized = "00:00";
        }
        if (normalized.length() > 5) {
            normalized = normalized.substring(0, 5);
        }
        try {
            return LocalTime.parse(normalized);
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    public static boolean isTwentyFourHours(LocalTime openTime, LocalTime closeTime) {
        return openTime != null && openTime.equals(closeTime);
    }

    public static boolean isOvernight(LocalTime openTime, LocalTime closeTime) {
        return openTime != null && closeTime != null && openTime.isAfter(closeTime);
    }

    public static boolean isActiveAt(LocalTime openTime, LocalTime closeTime, LocalTime time) {
        if (openTime == null || closeTime == null || time == null) {
            return false;
        }
        if (isTwentyFourHours(openTime, closeTime)) {
            return true;
        }
        if (openTime.isBefore(closeTime)) {
            return !time.isBefore(openTime) && time.isBefore(closeTime);
        }
        return !time.isBefore(openTime) || time.isBefore(closeTime);
    }

    public static TimeWindow activeWindowFor(LocalDateTime reference, LocalTime openTime, LocalTime closeTime) {
        if (reference == null) {
            throw new IllegalArgumentException("Reference time is required.");
        }
        LocalDate date = reference.toLocalDate();
        if (isTwentyFourHours(openTime, closeTime)) {
            return new TimeWindow(date.atTime(openTime), date.plusDays(1).atTime(closeTime));
        }
        if (openTime.isBefore(closeTime)) {
            return new TimeWindow(date.atTime(openTime), date.atTime(closeTime));
        }
        if (reference.toLocalTime().isBefore(closeTime)) {
            return new TimeWindow(date.minusDays(1).atTime(openTime), date.atTime(closeTime));
        }
        return new TimeWindow(date.atTime(openTime), date.plusDays(1).atTime(closeTime));
    }

    public static TimeWindow nextWindowFrom(LocalDateTime reference, LocalTime openTime, LocalTime closeTime) {
        TimeWindow currentWindow = activeWindowFor(reference, openTime, closeTime);
        if (!reference.isBefore(currentWindow.start()) && reference.isBefore(currentWindow.end())) {
            return currentWindow;
        }
        LocalDate date = reference.toLocalDate();
        LocalDateTime nextStart = date.atTime(openTime);
        if (!nextStart.isAfter(reference)) {
            nextStart = nextStart.plusDays(1);
        }
        LocalDateTime nextEnd = isTwentyFourHours(openTime, closeTime) || isOvernight(openTime, closeTime)
                ? nextStart.toLocalDate().plusDays(1).atTime(closeTime)
                : nextStart.toLocalDate().atTime(closeTime);
        return new TimeWindow(nextStart, nextEnd);
    }

    public static LocalDateTime resolveEnd(LocalDate date, LocalTime startTime, LocalTime endTime) {
        LocalDateTime start = LocalDateTime.of(date, startTime);
        LocalDateTime end = LocalDateTime.of(date, endTime);
        if (!end.isAfter(start)) {
            end = end.plusDays(1);
        }
        return end;
    }

    public static boolean fitsInBranchHours(LocalDateTime start, LocalDateTime end,
                                           LocalTime openTime, LocalTime closeTime) {
        if (start == null || end == null || !end.isAfter(start)) {
            return false;
        }
        TimeWindow window = activeWindowFor(start, openTime, closeTime);
        return !start.isBefore(window.start()) && !end.isAfter(window.end());
    }

    public static List<String> hourlyOptions(LocalTime openTime, LocalTime closeTime, boolean includeClose) {
        List<String> result = new ArrayList<>();
        if (openTime == null || closeTime == null) {
            return result;
        }
        int openMinutes = toMinutes(openTime);
        int closeMinutes = toMinutes(closeTime);
        int spanMinutes = isTwentyFourHours(openTime, closeTime)
                ? 24 * 60
                : (closeMinutes > openMinutes ? closeMinutes - openMinutes : closeMinutes + 24 * 60 - openMinutes);
        int limit = includeClose ? spanMinutes : Math.max(0, spanMinutes - 60);
        for (int offset = 0; offset <= limit; offset += 60) {
            result.add(formatMinutes((openMinutes + offset) % (24 * 60)));
        }
        return result;
    }

    public static long durationHours(LocalDate date, LocalTime startTime, LocalTime endTime) {
        LocalDateTime start = LocalDateTime.of(date, startTime);
        LocalDateTime end = resolveEnd(date, startTime, endTime);
        return Duration.between(start, end).toHours();
    }

    public static String format(LocalTime time) {
        if (time == null) {
            return "";
        }
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    private static int toMinutes(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private static String formatMinutes(int totalMinutes) {
        int normalized = ((totalMinutes % (24 * 60)) + (24 * 60)) % (24 * 60);
        return String.format("%02d:%02d", normalized / 60, normalized % 60);
    }

    public record TimeWindow(LocalDateTime start, LocalDateTime end) {
    }
}
