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
    public void kiemTraDatCho() {
        try {
            // 1. Kiểm tra và hủy các đặt chỗ quá hạn 10 phút chưa được thanh toán
            congThongTinService.hetHanDatChoChoThanhToan();
        } catch (Exception e) {
            System.err.println("[Scheduler] Không thể kiểm tra đặt chỗ hết hạn do mất kết nối DB: " + e.getMessage());
        }

        try {
            // 2. Xử lý đặt chỗ đã thanh toán nhưng khách không đến quá giờ nhận chỗ
            congThongTinService.xuLyDatChoDaThanhToanNhungKhongDen();
        } catch (Exception e) {
            System.err.println("[Scheduler] Không thể tự động xử lý đặt chỗ đã thanh toán quá giờ nhận chỗ: " + e.getMessage());
        }

        try {
            // 3. Tự động kết thúc các phiên làm việc đã quá giờ của các đặt trước
            congThongTinService.tuDongKetThucPhienQuaHanDatCho();
        } catch (Exception e) {
            System.err.println("[Scheduler] Không thể tự động kết thúc phiên làm việc quá hạn: " + e.getMessage());
        }
    }
}
