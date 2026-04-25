CREATE OR REPLACE TRIGGER trg_Booking_Start
BEFORE INSERT ON Bookings
FOR EACH ROW
BEGIN
    -- PENDING để giữ chỗ chờ thanh toán
    IF :NEW.status IS NULL OR :NEW.status = 'AVAILABLE' THEN
        :NEW.status := 'PENDING';
    END IF;

    :NEW.created_at := CURRENT_TIMESTAMP;
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/