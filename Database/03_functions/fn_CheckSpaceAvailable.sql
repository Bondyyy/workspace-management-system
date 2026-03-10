CREATE OR REPLACE FUNCTION fn_CheckSpaceAvailable ( 
    p_space_id IN NUMBER,
    p_start_time IN TIMESTAMP,
    p_end_time IN TIMESTAMP
) RETURN NUMBER -- 1: còn chỗ, 0: không còn chỗ
IS
    v_count NUMBER;
    v_current_status VARCHAR2(20);
BEGIN
    IF p_start_time >= p_end_time THEN
        RETURN 0;
    END IF;

    -- 5 phút dọn lại chỗ
    IF p_start_time <= SYSTIMESTAMP + INTERVAL '5' MINUTE THEN
        SELECT current_status INTO v_current_status
        FROM Spaces
        WHERE space_id = p_space_id;

        IF v_current_status != 'AVAILABLE' THEN
            RETURN 0; -- Đã có người ngồi hoặc đang bảo trì
        END IF;
    END IF;

    -- Kiểm tra các booking cũ đè lên booking mới
    SELECT COUNT(*)
    INTO v_count
    FROM BookingDetails bd
    JOIN Bookings b ON bd.booking_id = b.booking_id
    WHERE bd.space_id = p_space_id
        AND b.status NOT IN ('CANCELLED', 'COMPLETED')
        AND p_start_time < bd.expected_end_time
        AND p_end_time > bd.expected_start_time;

    IF v_count > 0 THEN
        RETURN 0;
    END IF;

    -- Kiểm tra khách đang ngồi và vừa xin Lễ tân gia hạn (ĐÃ SỬA LẠI JOIN)
    SELECT COUNT(*)
    INTO v_count
    FROM Sessions s
    JOIN SessionDetails sd ON s.session_id = sd.session_id
    JOIN SessionExtensions ext ON s.session_id = ext.session_id
    WHERE sd.space_id = p_space_id
      AND sd.status = 'ACTIVE'
      AND p_start_time < ext.end_time
      AND p_end_time > ext.start_time;

    IF v_count > 0 THEN
        RETURN 0;
    END IF;

    -- Đáp ứng toàn bộ
    RETURN 1;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
    WHEN OTHERS THEN
        RETURN 0;
END fn_CheckSpaceAvailable;
/