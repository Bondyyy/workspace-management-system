package com.wms.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpaceMapQueryRegressionTest {

    @Test
    void spaceMapQueryUsesNamedParametersForSelectedTimeWindow() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/wms/web/repository/CongThongTinWebRepository.java"));

        assertTrue(source.contains("NamedParameterJdbcTemplate namedJdbc"));
        assertTrue(source.contains("namedJdbc.query(sql, params"));
        assertTrue(source.contains("WHERE kg.MaCN = :maCN"));
        assertTrue(source.contains("p.ThoiGianBatDau < :requestEnd"));
        assertTrue(source.contains("> :requestStart"));
        assertTrue(source.contains("dc.ThoiGianDuKienToi < :requestEnd"));
        assertTrue(source.contains("dc.TrangThaiDatTruoc IN (:busyBookingStatuses)"));
        assertTrue(source.contains("System.out.println(\"[CongThongTinWebRepository] tai khong gian"));
    }
}
