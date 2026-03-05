CREATE OR REPLACE TRIGGER trg_AutoClose_SessionStatus
BEFORE UPDATE OF checkout_time ON Sessions
FOR EACH ROW
WHEN (NEW.checkout_time IS NOT NULL AND OLD.checkout_time IS NULL)
BEGIN
    -- Chuyển trạng thái của phiên làm việc từ ACTIVE sang COMPLETED
    :NEW.status := 'COMPLETED';
END;
/