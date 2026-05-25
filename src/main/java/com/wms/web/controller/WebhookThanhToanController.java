package com.wms.web.controller;

import com.wms.web.form.YeuCauWebhookThanhToan;
import com.wms.web.service.CongThongTinService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/*
 * LƯU Ý QUAN TRỌNG VỀ LUỒNG THANH TOÁN VIETQR:
 * VietQR chỉ là công nghệ sinh mã QR tĩnh/động chứa thông tin chuyển khoản (số tài khoản, ngân hàng, số tiền, nội dung).
 * Bản thân VietQR KHÔNG TỰ ĐỘNG GỬI CALLBACK/WEBHOOK khi khách hàng chuyển khoản thành công từ App ngân hàng.
 * Để nhận được callback/webhook tự động vào ứng dụng này, bạn cần:
 * 1. Đăng ký và cấu hình dịch vụ webhook biến động số dư bên thứ ba như SePay (sepay.vn), Casso (casso.vn), hoặc các cổng thanh toán có tích hợp API ngân hàng.
 * 2. Sử dụng công cụ như ngrok hoặc Cloudflare Tunnel để public cổng localhost của ứng dụng ra Internet (ví dụ: ngrok http 8080) và điền URL đó vào cấu hình webhook của SePay/Casso.
 * 
 * NẾU BẠN CHUYỂN KHOẢN THẬT TRÊN ỨNG DỤNG NGÂN HÀNG MÀ KHÔNG THẤY LOG WEBHOOK DƯỚI ĐÂY:
 * Kết luận rõ ràng: Chưa có SePay/Casso/API biến động số dư cấu hình để gọi vào app, hoặc ngrok chưa hoạt động. VietQR không tự callback!
 */
@RestController
@RequestMapping("/api/payment-webhook")
public class WebhookThanhToanController {

    private static final String WEBHOOK_SECRET = "SpringMngtSecretToken2026";
    private final CongThongTinService congThongTinService;

    public WebhookThanhToanController(CongThongTinService congThongTinService) {
        this.congThongTinService = congThongTinService;
    }

    @PostMapping("/bank-transfer")
    public ResponseEntity<Map<String, Object>> nhanChuyenKhoan(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secretToken,
            @RequestBody YeuCauWebhookThanhToan request) {
        
        System.out.println("[WebhookController] ========================================");
        System.out.println("[WebhookController] Webhook thuc te da duoc goi vao he thong!");
        
        // Kiem tra secret token bao mat
        if (secretToken == null || !secretToken.equals(WEBHOOK_SECRET)) {
            System.err.println("[WebhookController] CANH BAO: Token bao mat 'X-Webhook-Secret' khong hop le hoac bi thieu.");
            System.err.println("[WebhookController] Tu choi xu ly request.");
            Map<String, Object> errorBody = new LinkedHashMap<>();
            errorBody.put("success", false);
            errorBody.put("message", "Secret token khong hop le hoac thieu.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
        }

        if (request != null) {
            System.out.println("[WebhookController] Nhan payload: MaGiaoDich=" + request.getMaGiaoDich() 
                    + ", SoTien=" + request.getSoTien() + ", NoiDung='" + request.getNoiDung() + "'");
        } else {
            System.err.println("[WebhookController] Request body bi NULL.");
        }

        CongThongTinService.KetQuaWebhookThanhToan result = congThongTinService.xuLyWebhookThanhToan(request);
        HttpStatus status = result.success() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", result.success());
        body.put("message", result.message());
        if (result.maDatCho() != null && !result.maDatCho().isBlank()) {
            body.put("maDatCho", result.maDatCho());
        }
        
        System.out.println("[WebhookController] Ket qua xu ly: success=" + result.success() + ", message='" + result.message() + "'");
        System.out.println("[WebhookController] ========================================");
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> xuLyPayloadKhongHopLe(HttpMessageNotReadableException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", "Dữ liệu thời gian thanh toán không đúng định dạng. Vui lòng kiểm tra lại.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
