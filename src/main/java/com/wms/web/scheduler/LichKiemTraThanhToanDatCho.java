package com.wms.web.scheduler;

import com.wms.web.service.CongThongTinService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "wms.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class LichKiemTraThanhToanDatCho {

    private final CongThongTinService congThongTinService;

    public LichKiemTraThanhToanDatCho(CongThongTinService congThongTinService) {
        this.congThongTinService = congThongTinService;
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void hetHanDatChoChoThanhToan() {
        try {
            // Scheduler kiem tra va huy cac dat cho qua han 10 phut chua duoc thanh toan.
            // Luu y: khong tu y tang thoi gian timeout nham tranh giu cho ao qua lau.
            congThongTinService.hetHanDatChoChoThanhToan();
        } catch (Exception e) {
            System.err.println("[Scheduler] Không thể kiểm tra đặt chỗ hết hạn do mất kết nối DB: " + e.getMessage());
        }
    }
}
