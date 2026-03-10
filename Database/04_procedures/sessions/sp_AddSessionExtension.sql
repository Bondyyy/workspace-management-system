CREATE OR REPLACE PROCEDURE sp_AddSessionExtension (
    p_session_id          IN NUMBER,
    p_extended_duration   IN NUMBER,          -- Đơn vị tính là giờ
    p_request_channel     IN VARCHAR2,        -- 'APP', 'RECEPTION'
    p_staff_id            IN NUMBER,
    p_out_extension_id    OUT NUMBER,
    p_out_message         OUT VARCHAR2
)
AS
    v_session_status      VARCHAR2(20);
    v_total_applied_rate  NUMBER(15, 2) := 0;
    v_booking_id          NUMBER;

    v_current_end_time    TIMESTAMP;
    v_new_end_time        TIMESTAMP;
    v_cost_incurred       NUMBER(15, 2);
    v_extension_id        NUMBER;
    v_overlap_count       NUMBER;
BEGIN
    SELECT status, booking_id
    INTO v_session_status, v_booking_id
    FROM Sessions
    WHERE session_id = p_session_id
    FOR UPDATE NOWAIT;

    IF v_session_status != 'ACTIVE' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Lỗi: Chỉ có thể gia hạn cho phiên làm việc đang hoạt động (ACTIVE).');
    END IF;

    IF p_extended_duration <= 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Lỗi: Thời gian gia hạn phải lớn hơn 0.');
    END IF;

    SELECT SUM(applied_hourly_rate)
    INTO v_total_applied_rate
    FROM SessionDetails
    WHERE session_id = p_session_id AND status = 'ACTIVE';

    IF NVL(v_total_applied_rate, 0) = 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Lỗi: Không có không gian nào đang hoạt động trong phiên này để gia hạn.');
    END IF;

    -- Đã từng gia hạn
    SELECT MAX(end_time) INTO v_current_end_time
    FROM SessionExtensions
    WHERE session_id = p_session_id;

    -- Chưa gia hạn lần nào
    IF v_current_end_time IS NULL AND v_booking_id IS NOT NULL THEN
        SELECT MAX(expected_end_time) INTO v_current_end_time
        FROM BookingDetails
        WHERE booking_id = v_booking_id
          AND space_id IN (SELECT space_id FROM SessionDetails WHERE session_id = p_session_id AND status = 'ACTIVE');
    END IF;

    -- Khách vãng lai
    IF v_current_end_time IS NULL THEN
        v_current_end_time := SYSTIMESTAMP;
    END IF;

    v_new_end_time := v_current_end_time + NUMTODSINTERVAL(p_extended_duration, 'HOUR');
    v_cost_incurred := p_extended_duration * NVL(v_total_applied_rate, 0);

    -- Kiểm tra trùng lịch
    SELECT COUNT(*)
    INTO v_overlap_count
    FROM BookingDetails bd
    JOIN Bookings b ON bd.booking_id = b.booking_id
    WHERE b.status NOT IN ('CANCELLED', 'COMPLETED')
      AND bd.space_id IN (SELECT space_id FROM SessionDetails WHERE session_id = p_session_id AND status = 'ACTIVE')
      AND v_current_end_time < bd.expected_end_time
      AND v_new_end_time > bd.expected_start_time;

    IF v_overlap_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20004, 'Lỗi: Không thể gia hạn vì có ít nhất một bàn trong phiên của bạn đã được khách khác đặt trước trong khung giờ tới!');
    END IF;

    INSERT INTO SessionExtensions (
        session_id, extended_duration, start_time, end_time,
        cost_incurred, payment_status, request_channel, created_by
    ) VALUES (
        p_session_id, p_extended_duration, v_current_end_time, v_new_end_time,
        v_cost_incurred, 'UNPAID', p_request_channel, p_staff_id
    ) RETURNING extension_id INTO v_extension_id;

    COMMIT;

    p_out_extension_id := v_extension_id;
    p_out_message := 'Gia hạn thành công thêm ' || p_extended_duration || ' giờ. Phí phát sinh: ' || v_cost_incurred || ' VNĐ.';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_AddSessionExtension;
/