CREATE OR REPLACE TRIGGER trg_DeactivateQR_OnCheckIn
BEFORE UPDATE OF check_in_time ON Bookings
FOR EACH ROW
-- Chỉ chạy khi check_in_time từ NULL chuyển sang CÓ GIÁ TRỊ
WHEN (NEW.check_in_time IS NOT NULL AND OLD.check_in_time IS NULL)
BEGIN
    -- Xóa QR để không thể quét lại lần nữa
    :NEW.qr_code := NULL;
    -- Tự động chuyển trạng thái đơn hàng sang 'ACTIVE'
    :NEW.status := 'ACTIVE';
    -- Ghi chú log
    :NEW.note := :NEW.note || ' | Khách đã nhận chỗ. QR bị vô hiệu hóa.';
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/