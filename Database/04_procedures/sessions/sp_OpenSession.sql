CREATE OR REPLACE PROCEDURE sp_OpenSession (
    p_customerId  IN NUMBER,   
    p_spaceId     IN NUMBER,   
    p_qrCode      IN VARCHAR2, 
    p_staffId     IN NUMBER,   
    p_outMessage  OUT VARCHAR2
)
AS
    v_bookingId    NUMBER := NULL;
    v_bkgStatus    VARCHAR2(20);
    v_spaceStatus  VARCHAR2(20);
    v_sessionId    NUMBER;
    v_hourlyRate   NUMBER(15, 2);
BEGIN
    -- 1. Process QR Code if provided
    IF p_qrCode IS NOT NULL THEN
        SELECT booking_id, status INTO v_bookingId, v_bkgStatus
        FROM Bookings WHERE qr_code = p_qrCode FOR UPDATE NOWAIT;
        
        IF v_bkgStatus NOT IN ('PENDING', 'BOOKED') THEN
            RAISE_APPLICATION_ERROR(-20001, 'Mã QR không hợp lệ hoặc đã được sử dụng!');
        END IF;
        
        UPDATE Bookings SET status = 'ACTIVE', check_in_time = SYSTIMESTAMP 
        WHERE booking_id = v_bookingId;
    END IF;

    -- 2. Check space availability and get base price
    SELECT current_status INTO v_spaceStatus FROM Spaces 
    WHERE space_id = p_spaceId FOR UPDATE NOWAIT;
    
    IF v_spaceStatus NOT IN ('AVAILABLE', 'BOOKED') THEN
        RAISE_APPLICATION_ERROR(-20003, 'Vị trí này hiện không trống để phục vụ!');
    END IF;

    SELECT base_price_per_hour INTO v_hourlyRate 
    FROM SpaceTypes st JOIN Spaces s ON st.type_id = s.type_id 
    WHERE s.space_id = p_spaceId;

    -- 3. Create Session
    INSERT INTO Sessions (customer_id, booking_id, checkin_time, check_in_staff_id, status) 
    VALUES (p_customerId, v_bookingId, SYSTIMESTAMP, p_staffId, 'ACTIVE') 
    RETURNING session_id INTO v_sessionId;

    -- 4. Create Session Details and lock space
    INSERT INTO SessionDetails (session_id, space_id, checkin_time, applied_hourly_rate, status) 
    VALUES (v_sessionId, p_spaceId, SYSTIMESTAMP, v_hourlyRate, 'ACTIVE');

    UPDATE Spaces SET current_status = 'OCCUPIED' WHERE space_id = p_spaceId;

    -- 5. Commit transaction
    COMMIT;
    p_outMessage := 'Mở phiên làm việc thành công!';
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20002, 'Không tìm thấy dữ liệu hợp lệ!');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_OpenSession;
/