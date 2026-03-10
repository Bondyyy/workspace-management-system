CREATE OR REPLACE PROCEDURE sp_ProcessCheckIn (
    p_qr_code             IN VARCHAR2,
    p_customer_id         IN NUMBER,
    p_check_in_staff_id   IN NUMBER,
    p_walkin_spaces       IN t_space_booking_list,
    p_out_message         OUT VARCHAR2
)
AS
    v_booking_id    NUMBER;
    v_customer_id   NUMBER;
    v_bkg_status    VARCHAR2(20);
    v_space_status  VARCHAR2(20);
    v_session_id    NUMBER;
BEGIN
    -- Khách đặt trước
    IF p_qr_code IS NOT NULL THEN
        BEGIN
            SELECT booking_id, customer_id, status
            INTO v_booking_id, v_customer_id, v_bkg_status
            FROM Bookings
            WHERE qr_code = p_qr_code
            FOR UPDATE NOWAIT;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20001, 'Mã QR không hợp lệ hoặc không tồn tại trên hệ thống!');
        END;

        IF v_bkg_status NOT IN ('PENDING', 'BOOKED') THEN
            RAISE_APPLICATION_ERROR(-20002, 'Phiếu đặt chỗ này không ở trạng thái hợp lệ để Check-in.');
        END IF;

        -- Cập nhật thời gian check-in
        UPDATE Bookings
        SET check_in_time = SYSTIMESTAMP,
            status = 'ACTIVE'
        WHERE booking_id = v_booking_id;

        INSERT INTO Sessions (
            customer_id, booking_id, checkin_time, check_in_staff_id, status
        ) VALUES (
            v_customer_id, v_booking_id, SYSTIMESTAMP, p_check_in_staff_id, 'ACTIVE'
        ) RETURNING session_id INTO v_session_id;

        -- Tạo SessionDetails cho từng bàn của khách đã đặt
        FOR rec IN (SELECT space_id, price_at_booking FROM BookingDetails WHERE booking_id = v_booking_id) LOOP
            SELECT current_status INTO v_space_status FROM Spaces WHERE space_id = rec.space_id FOR UPDATE NOWAIT;

            INSERT INTO SessionDetails (
                session_id, space_id, checkin_time, applied_hourly_rate, status
            ) VALUES (
                v_session_id, rec.space_id, SYSTIMESTAMP, rec.price_at_booking, 'ACTIVE'
            );

            UPDATE Spaces
            SET current_status = 'OCCUPIED'
            WHERE space_id = rec.space_id;
        END LOOP;

        p_out_message := 'Check-in thành công qua mã QR. Đã khởi tạo phiên làm việc.';

    -- Khách vãng lai
    ELSE
        IF p_walkin_spaces IS NULL OR p_walkin_spaces.COUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20003, 'Lỗi: Khách vãng lai bắt buộc phải chọn ít nhất 1 không gian.');
        END IF;

        -- Quét kiểm tra tất cả các bàn xem có trống không
        FOR i IN 1..p_walkin_spaces.COUNT LOOP
            BEGIN
                SELECT current_status INTO v_space_status
                FROM Spaces
                WHERE space_id = p_walkin_spaces(i).space_id
                FOR UPDATE NOWAIT;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    RAISE_APPLICATION_ERROR(-20005, 'Không tìm thấy không gian với ID: ' || p_walkin_spaces(i).space_id);
            END;

            IF v_space_status != 'AVAILABLE' THEN
                RAISE_APPLICATION_ERROR(-20006, 'Không gian ID ' || p_walkin_spaces(i).space_id || ' hiện không trống. Vui lòng chọn vị trí khác!');
            END IF;
        END LOOP;

        INSERT INTO Sessions (
            customer_id, booking_id, checkin_time, check_in_staff_id, status
        ) VALUES (
            p_customer_id, NULL, SYSTIMESTAMP, p_check_in_staff_id, 'ACTIVE'
        ) RETURNING session_id INTO v_session_id;

        -- Tạo SessionDetails cho từng bàn của khách vãng lai
        FOR i IN 1..p_walkin_spaces.COUNT LOOP
            INSERT INTO SessionDetails (
                session_id, space_id, checkin_time, applied_hourly_rate, status
            ) VALUES (
                v_session_id, p_walkin_spaces(i).space_id, SYSTIMESTAMP, p_walkin_spaces(i).price_at_booking, 'ACTIVE'
            );

            UPDATE Spaces
            SET current_status = 'OCCUPIED'
            WHERE space_id = p_walkin_spaces(i).space_id;
        END LOOP;

        p_out_message := 'Check-in thành công cho khách vãng lai. Đã mở phiên mới cho ' || p_walkin_spaces.COUNT || ' không gian.';
    END IF;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_ProcessCheckIn;
/