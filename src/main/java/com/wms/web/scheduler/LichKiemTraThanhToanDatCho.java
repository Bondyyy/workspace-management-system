package com.wms.web.scheduler;

import com.wms.web.service.CongThongTinService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LichKiemTraThanhToanDatCho {

    private final CongThongTinService congThongTinService;

    public LichKiemTraThanhToanDatCho(CongThongTinService congThongTinService) {
        this.congThongTinService = congThongTinService;
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void hetHanDatChoChoThanhToan() {
        congThongTinService.hetHanDatChoChoThanhToan();
    }
}
