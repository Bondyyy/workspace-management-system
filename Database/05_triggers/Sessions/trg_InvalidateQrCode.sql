CREATE OR REPLACE TRIGGER trg_InvalidateQrCode
AFTER INSERT ON Sessions
FOR EACH ROW
WHEN (NEW.booking_id IS NOT NULL)
BEGIN
    -- 1. Invalidate QR by setting Booking status to ACTIVE
    UPDATE Bookings
    SET status = 'ACTIVE',
        check_in_time = SYSTIMESTAMP
    WHERE booking_id = :NEW.booking_id
      AND status IN ('PENDING', 'BOOKED');
END;
/