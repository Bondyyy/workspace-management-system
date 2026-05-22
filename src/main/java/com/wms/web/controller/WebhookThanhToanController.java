package com.wms.web.controller;

import com.wms.web.form.YeuCauWebhookThanhToan;
import com.wms.web.service.CongThongTinService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * =================================================================================
 * LƯU Ý QUAN TRỌNG VỀ LUỒNG THANH TOÁN VIETQR WEBHOOK:
 * =================================================================================
 * 1. VietQR (VietQR.io/VietQR.net) CHỈ hỗ trợ sinh mã QR tĩnh/động theo đặc tả NAPAS 247.
 *    Bản thân VietQR KHÔNG TỰ ĐỘNG GỬI CALLBACK/WEBHOOK thanh toán về ứng dụng khi khách hàng
 *    chuyển khoản thành công qua ứng dụng ngân hàng.
 * 
 * 2. Để hệ thống nhận được biến động số dư tự động và gọi webhook vào API này,
 *    cần phải tích hợp các dịch vụ bên thứ ba chuyên quét biến động số dư tài khoản ngân hàng
 *    (ví dụ: SePay, Casso, PayOS, hoặc API ngân hàng doanh nghiệp) cấu hình webhook gọi về.
 * 
 * 3. Khi chạy thử nghiệm tại local (môi trường localhost):
 *    Cần sử dụng các công cụ tunnel như ngrok (ví dụ: `ngrok http 8080`) hoặc Cloudflare Tunnel
 *    để chuyển tiếp webhook từ Internet vào máy local của lập trình viên, sau đó cấu hình URL webhook
 *    tại SePay/Casso trỏ về địa chỉ URL tunnel đó (ví dụ: https://<subdomain>.ngrok-free.app/api/payment-webhook/bank-transfer).
 * 
 * 4. Để đảm bảo an toàn bảo mật, hệ thống sử dụng Secret Token thông qua header "X-Webhook-Secret".
 *    Mã token này được đọc từ biến môi trường "WEBHOOK_SECRET" (fallback mặc định là "WMS_SECRET_TOKEN_2026").
 *    Mọi request không mang đúng token này sẽ bị từ chối ngay lập tức (401 Unauthorized).
 * =================================================================================
 */
@RestController
@RequestMapping("/api/payment-webhook")
public class WebhookThanhToanController {

    private final CongThongTinService congThongTinService;

    public WebhookThanhToanController(CongThongTinService congThongTinService) {
        this.congThongTinService = congThongTinService;
    }

    @PostMapping("/bank-transfer")
    public ResponseEntity<Map<String, Object>> nhanChuyenKhoan(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secretToken,
            @RequestBody YeuCauWebhookThanhToan request) {

        System.out.println("================================================================================");
        System.out.println("[Webhook Controller] NHẬN ĐƯỢC REQUEST WEBHOOK BIẾN ĐỘNG SỐ DƯ TÀI KHOẢN NGÂN HÀNG");
        System.out.println("[Webhook Controller] X-Webhook-Secret nhận được: \"" + secretToken + "\"");

        // 1. Kiểm tra secret token bảo mật
        String envSecret = System.getenv("WEBHOOK_SECRET");
        String configuredSecret = (envSecret != null && !envSecret.isBlank()) ? envSecret : "WMS_SECRET_TOKEN_2026";

        if (secretToken == null || !secretToken.trim().equals(configuredSecret)) {
            System.err.println("[Webhook Controller] -> TỪ CHỐI REQUEST: Secret Token không hợp lệ hoặc bị thiếu!");
            System.out.println("================================================================================");
            Map<String, Object> errorBody = new LinkedHashMap<>();
            errorBody.put("success", false);
            errorBody.put("message", "Từ chối truy cập: Webhook Secret Token không hợp lệ.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
        }

        // 2. Log chi tiết payload đầu vào của webhook
        if (request != null) {
            System.out.println("[Webhook Controller] Payload JSON nhận được:");
            System.out.println("  - Mã giao dịch (maGiaoDich): " + request.getMaGiaoDich());
            System.out.println("  - Số tiền (soTien): " + request.getSoTien());
            System.out.println("  - Nội dung chuyển khoản (noiDung): \"" + request.getNoiDung() + "\"");
            System.out.println("  - Trạng thái (trangThai): " + request.getTrangThai());
            System.out.println("  - Thời gian giao dịch (thoiGianThanhToan): " + request.getThoiGianThanhToan());
        } else {
            System.err.println("[Webhook Controller] Payload request là NULL!");
        }

        // 3. Thực hiện xử lý nghiệp vụ thông qua Service
        CongThongTinService.KetQuaWebhookThanhToan result = congThongTinService.xuLyWebhookThanhToan(request);
        
        System.out.println("[Webhook Controller] Kết quả xử lý nghiệp vụ:");
        System.out.println("  - Thành công (success): " + result.success());
        System.out.println("  - Thông điệp (message): " + result.message());
        System.out.println("  - Mã đặt chỗ (maDatCho): " + result.maDatCho());
        System.out.println("================================================================================");

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
