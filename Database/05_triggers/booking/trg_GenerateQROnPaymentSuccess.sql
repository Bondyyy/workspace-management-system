CREATE OR REPLACE TRIGGER trg_GenerateQR_OnPaymentSuccess
BEFORE UPDATE OF payment_status ON Bookings
FOR EACH ROW
WHEN (NEW.payment_status IN ('SUCCEED') AND OLD.payment_status = 'UNPAID')
DECLARE
    v_new_qr VARCHAR2(255);
BEGIN
    -- Gọi Procedure hệ thống để phát sinh chuỗi mã QR duy nhất
    sp_GenerateBookingQR(:NEW.booking_id, v_new_qr);

    -- Gán chuỗi QR vừa tạo vào cột qr_code của bản ghi đang được cập nhật
    :NEW.qr_code := v_new_qr;

    -- Ghi log lưu vết hệ thống
    :NEW.note := :NEW.note || ' | Cấp QR thành công qua Procedure.';

    -- Cập nhật thời gian chỉnh sửa cuối cùng
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/