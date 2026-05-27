package com.wms.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpaceMapQueryRegressionTest {

    @Test
    void spaceMapQueryUsesNamedParametersForSelectedTimeWindow() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/wms/web/repository/CongThongTinWebRepository.java"));

        assertTrue(source.contains("NamedParameterJdbcTemplate namedJdbc"));
        assertTrue(source.contains("namedJdbc.query(sql, params"));
        assertTrue(source.contains("WHERE kg.MaCN = :maCN"));
        assertTrue(source.contains("p.ThoiGianKetThuc IS NULL"));
        assertTrue(source.contains("p.ThoiGianBatDau < :requestEnd"));
        assertTrue(source.contains("> :requestStart"));
        assertTrue(source.contains("dc.ThoiGianDuKienToi < :requestEnd"));
        assertTrue(source.contains("dc.TrangThaiDatTruoc IN (:confirmedBookingStatuses)"));
        assertTrue(source.contains("dc.TrangThaiDatTruoc = :pendingBookingStatus"));
        assertTrue(source.contains("NUMTODSINTERVAL(:paymentHoldMinutes, 'MINUTE')"));
        assertTrue(source.contains("THEN :lockedDisplay"));
        assertTrue(source.contains("System.out.println(\"[CongThongTinWebRepository] tai khong gian"));
        assertFalse(source.contains("kg.TrangThaiKG = :busySpaceStatus"));
        assertFalse(source.contains("dc.TrangThaiDatTruoc IN (:busyBookingStatuses)"));
    }
}
