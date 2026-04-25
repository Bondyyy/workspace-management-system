CREATE OR REPLACE TRIGGER trg_UpdateSpaceStatus_Booking
AFTER UPDATE OF status ON Bookings
FOR EACH ROW
WHEN (NEW.status != OLD.status)
BEGIN
    -- Chỉ xử lý việc Giữ chỗ và Hủy đơn
    IF :NEW.status = 'BOOKED' OR :NEW.status = 'PENDING' THEN
        UPDATE Spaces
        SET current_status = 'BOOKED'
        WHERE space_id IN (
            SELECT space_id
            FROM BookingDetails
            WHERE booking_id = :NEW.booking_id);

    ELSIF :NEW.status = 'CANCELLED' THEN
        UPDATE Spaces
        SET current_status = 'AVAILABLE'
        WHERE space_id IN (
            SELECT space_id
            FROM BookingDetails
            WHERE booking_id = :NEW.booking_id)
                AND current_status = 'BOOKED'; -- Chỉ nhả về AVAILABLE nếu trước đó đang giữ chỗ
    END IF;
END;
/