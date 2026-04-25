-- Tự động tắt voucher khi used_count chạm usage_limit
CREATE OR REPLACE TRIGGER trg_AutoDeactivateVoucher
AFTER UPDATE OF used_count ON Vouchers
FOR EACH ROW
WHEN (NEW.is_active = 1)
BEGIN
    IF :NEW.usage_limit IS NOT NULL AND :NEW.used_count >= :NEW.usage_limit THEN
        UPDATE Vouchers
        SET is_active = 0, updated_at = CURRENT_TIMESTAMP
        WHERE voucher_id = :NEW.voucher_id;
    END IF;
END trg_AutoDeactivateVoucher;
/