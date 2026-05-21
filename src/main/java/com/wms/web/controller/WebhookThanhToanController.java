package com.wms.web.controller;

import com.wms.web.form.YeuCauWebhookThanhToan;
import com.wms.web.service.CongThongTinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payment-webhook")
public class WebhookThanhToanController {

    private final CongThongTinService congThongTinService;

    public WebhookThanhToanController(CongThongTinService congThongTinService) {
        this.congThongTinService = congThongTinService;
    }

    @PostMapping("/bank-transfer")
    public ResponseEntity<Map<String, Object>> nhanChuyenKhoan(@RequestBody YeuCauWebhookThanhToan request) {
        CongThongTinService.KetQuaWebhookThanhToan result = congThongTinService.xuLyWebhookThanhToan(request);
        return ResponseEntity.ok(Map.of(
                "success", result.success(),
                "message", result.message()
        ));
    }
}
