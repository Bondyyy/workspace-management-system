package com.wms.util;

import com.wms.web.model.KhongGianView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class KhongGianViewStatusTest {

    @Test
    void unavailableEmptySpaceStillDisplaysAsEmpty() {
        KhongGianView view = new KhongGianView(
                "KG0001", "Bàn 1", "Bàn làm việc", "Tầng 1",
                "Trống", "CN0001", "Chi nhánh 1", "07:00", "22:00",
                4, BigDecimal.ZERO, 0, 0, 1, 1,
                false, null
        );

        assertEquals("empty", view.getStatusClass());
        assertEquals("Trống", view.getTrangThaiHienThi());
        assertFalse(view.coTheDat());
    }

    @Test
    void bookedStatusComesFromStatusText() {
        KhongGianView view = new KhongGianView(
                "KG0001", "Bàn 1", "Bàn làm việc", "Tầng 1",
                "Đã đặt trước", "CN0001", "Chi nhánh 1", "07:00", "22:00",
                4, BigDecimal.ZERO, 0, 0, 1, 1,
                true, null
        );

        assertEquals("booked", view.getStatusClass());
        assertEquals("Đã đặt trước", view.getTrangThaiHienThi());
        assertFalse(view.coTheDat());
    }
}
