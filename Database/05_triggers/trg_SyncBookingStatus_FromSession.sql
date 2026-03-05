CREATE OR REPLACE TRIGGER trg_SyncBookingStatus_FromSession
AFTER INSERT OR UPDATE OF checkout_time ON Sessions
FOR EACH ROW
WHEN (NEW.booking_id IS NOT NULL)
BEGIN
    IF INSERTING THEN
        UPDATE Bookings
        SET status = 'ACTIVE',
            check_in_time = :NEW.checkin_time -- Cập nhật luôn giờ check-in thực tế vào đơn booking
        WHERE booking_id = :NEW.booking_id;

    -- 2. Khi khách Check-out
    ELSIF UPDATING AND :NEW.checkout_time IS NOT NULL AND :OLD.checkout_time IS NULL THEN
        UPDATE Bookings
        SET status = 'COMPLETED',
            check_out_time = :NEW.checkout_time -- Cập nhật luôn giờ check-out thực tế vào đơn booking
        WHERE booking_id = :NEW.booking_id;
    END IF;
END;
/