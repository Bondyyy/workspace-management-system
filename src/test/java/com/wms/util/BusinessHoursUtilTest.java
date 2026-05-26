package com.wms.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BusinessHoursUtilTest {
    @Test
    void normalDayWindowAcceptsInsideAndBlocksOutside() {
        LocalTime open = LocalTime.of(7, 0);
        LocalTime close = LocalTime.of(22, 0);
        assertTrue(BusinessHoursUtil.fitsInBranchHours(
                LocalDateTime.of(2026, 5, 25, 9, 0),
                LocalDateTime.of(2026, 5, 25, 11, 0),
                open,
                close));
        assertFalse(BusinessHoursUtil.fitsInBranchHours(
                LocalDateTime.of(2026, 5, 25, 22, 0),
                LocalDateTime.of(2026, 5, 25, 23, 0),
                open,
                close));
    }

    @Test
    void overnightWindowAcceptsLateNightAndAfterMidnight() {
        LocalTime open = LocalTime.of(22, 0);
        LocalTime close = LocalTime.of(6, 0);
        assertTrue(BusinessHoursUtil.fitsInBranchHours(
                LocalDateTime.of(2026, 5, 25, 23, 0),
                LocalDateTime.of(2026, 5, 26, 2, 0),
                open,
                close));
        assertTrue(BusinessHoursUtil.fitsInBranchHours(
                LocalDateTime.of(2026, 5, 26, 1, 0),
                LocalDateTime.of(2026, 5, 26, 3, 0),
                open,
                close));
    }

    @Test
    void overnightWindowBlocksMidday() {
        assertFalse(BusinessHoursUtil.fitsInBranchHours(
                LocalDateTime.of(2026, 5, 25, 12, 0),
                LocalDateTime.of(2026, 5, 25, 13, 0),
                LocalTime.of(22, 0),
                LocalTime.of(6, 0)));
    }

    @Test
    void equalOpenCloseMeansTwentyFourHours() {
        assertTrue(BusinessHoursUtil.fitsInBranchHours(
                LocalDateTime.of(2026, 5, 25, 12, 0),
                LocalDateTime.of(2026, 5, 25, 14, 0),
                LocalTime.MIDNIGHT,
                LocalTime.MIDNIGHT));
        assertTrue(BusinessHoursUtil.isTwentyFourHours(LocalTime.MIDNIGHT, LocalTime.MIDNIGHT));
    }

    @Test
    void resolveEndMovesOvernightCloseToNextDay() {
        assertTrue(BusinessHoursUtil.resolveEnd(LocalDate.of(2026, 5, 25),
                LocalTime.of(23, 0),
                LocalTime.of(2, 0))
                .equals(LocalDateTime.of(2026, 5, 26, 2, 0)));
    }

    @Test
    void durationHoursCheckoutComputesExpectedEndInsideNormalBranchHours() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 26, 14, 0);
        LocalDateTime end = start.plusHours(2);

        assertEquals(LocalDateTime.of(2026, 5, 26, 16, 0), end);
        assertTrue(BusinessHoursUtil.fitsInBranchHours(
                start,
                end,
                LocalTime.of(7, 0),
                LocalTime.of(22, 0)));
    }

    @Test
    void durationHoursCheckoutBlocksWhenComputedEndPassesNormalClose() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 26, 21, 0);
        LocalDateTime end = start.plusHours(2);

        assertFalse(BusinessHoursUtil.fitsInBranchHours(
                start,
                end,
                LocalTime.of(7, 0),
                LocalTime.of(22, 0)));
    }

    @Test
    void durationHoursCheckoutAcceptsOvernightBranch() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 26, 23, 0);
        LocalDateTime end = start.plusHours(2);

        assertTrue(BusinessHoursUtil.fitsInBranchHours(
                start,
                end,
                LocalTime.of(22, 0),
                LocalTime.of(6, 0)));
    }
}
