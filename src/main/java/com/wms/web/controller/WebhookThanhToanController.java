package com.wms.web.controller;

import com.wms.web.form.YeuCauWebhookThanhToan;
import com.wms.web.service.CongThongTinService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
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
        HttpStatus status = result.success() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", result.success());
        body.put("message", result.message());
        if (result.maDatCho() != null && !result.maDatCho().isBlank()) {
            body.put("maDatCho", result.maDatCho());
        }
        return ResponseEntity.status(status).body(body);
    }
}
