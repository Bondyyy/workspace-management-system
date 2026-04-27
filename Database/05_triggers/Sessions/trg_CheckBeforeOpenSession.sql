CREATE OR REPLACE TRIGGER trg_CheckBeforeOpenSession
BEFORE INSERT ON SessionDetails
FOR EACH ROW
DECLARE
    v_currentStatus VARCHAR2(20);
BEGIN
    -- 1. Get current space status
    SELECT current_status INTO v_currentStatus
    FROM Spaces
    WHERE space_id = :NEW.space_id;

    -- 2. Throw error and rollback if space is not available
    IF v_currentStatus NOT IN ('AVAILABLE', 'BOOKED') THEN
        RAISE_APPLICATION_ERROR(-20001, 'Lỗi: Không gian này đã có người sử dụng, không thể mở phiên!');
    END IF;
END;
/