package com.wms.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaTuDongUtilTest {

    @Test
    void formatSinhMaDungChuanSauChuSo() {
        assertEquals("KG000001", MaTuDongUtil.format("KG", 1, 6));
        assertEquals("CN000005", MaTuDongUtil.format("CN", 5, 6));
        assertEquals("PGG000002", MaTuDongUtil.format("PGG", 2, 6));
        assertEquals("HV000001", MaTuDongUtil.format(MaTuDongUtil.MaDoiTuong.KHACH_HANG.prefix(), 1, 6));
    }

    @Test
    void parseDocDuocMaCuVaMaMoi() {
        assertEquals(1, MaTuDongUtil.parseNumber("KG", "KG001"));
        assertEquals(123, MaTuDongUtil.parseNumber("KG", "KG000123"));
    }

    @Test
    void parseTraVeKhongKhiSaiPrefixHoacKhongCoSo() {
        assertEquals(0, MaTuDongUtil.parseNumber("KG", "HD000123"));
        assertEquals(0, MaTuDongUtil.parseNumber("KG", "KG"));
    }

    @Test
    void nextFromExistingLaySoLonNhatVaGiuChuanSauChuSo() {
        assertEquals("KG000124", MaTuDongUtil.nextFromExisting(
                List.of("KG001", "KG000123", "KG000008"), "KG", 6));
    }
}
