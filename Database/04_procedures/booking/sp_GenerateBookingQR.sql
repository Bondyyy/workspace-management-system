CREATE OR REPLACE PROCEDURE sp_GenerateBookingQR (
    p_booking_id IN NUMBER,
    p_qr_string  OUT VARCHAR2
) IS
BEGIN
    -- Logic tạo mã QR: QR-ID-[Thời gian (năm tháng ngày giờ phút giây)]
    p_qr_string := 'QR-' || p_booking_id || '-' || TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISS');
END;
/