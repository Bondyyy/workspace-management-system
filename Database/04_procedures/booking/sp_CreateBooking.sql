CREATE OR REPLACE PROCEDURE sp_CreateBooking (
    p_customer_id         IN NUMBER,
    p_booking_channel     IN VARCHAR2,
    p_note                IN VARCHAR2,
    p_created_by_staff_id IN NUMBER,
    p_deposit_amount      IN NUMBER,
    p_spaces_list         IN t_space_booking_list,
    p_booking_code        IN VARCHAR2,        -- Nhận từ Java
    p_qr_code             IN VARCHAR2,        -- Nhận từ Java
    p_out_booking_id      OUT NUMBER
)
AS
    v_total_amount  NUMBER(15, 2) := 0;
    v_is_available  NUMBER;
BEGIN
    -- Kiểm tra danh sách không gian đầu vào
    IF p_spaces_list IS NULL OR p_spaces_list.COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Danh sách không gian trống!');
    END IF;

    -- Duyệt qua danh sách để kiểm tra tính hợp lệ và tính tổng tiền
    FOR i IN 1..p_spaces_list.COUNT LOOP
        v_is_available := fn_CheckSpaceAvailable(
            p_spaces_list(i).space_id,
            p_spaces_list(i).expected_start_time,
            p_spaces_list(i).expected_end_time
        );

        IF v_is_available = 0 THEN
            RAISE_APPLICATION_ERROR(-20004, 'Không gian ID ' || p_spaces_list(i).space_id || ' đã bị trùng lịch hoặc không khả dụng trong khung giờ này!');
        END IF;

        -- Cộng dồn giá tiền
        v_total_amount := v_total_amount + p_spaces_list(i).price_at_booking;
    END LOOP;

    -- Ghi dữ liệu vào bảng Bookings
    INSERT INTO Bookings (
        customer_id, booking_code, qr_code, total_amount, deposit_amount,
        final_amount, booking_channel, note, created_by_staff_id, status, payment_status
    ) VALUES (
        p_customer_id, p_booking_code, p_qr_code, v_total_amount, NVL(p_deposit_amount, 0),
        (v_total_amount - NVL(p_deposit_amount, 0)),
        p_booking_channel, p_note, p_created_by_staff_id, 'PENDING',
        CASE WHEN NVL(p_deposit_amount, 0) > 0 THEN 'PARTIAL' ELSE 'UNPAID' END
    ) RETURNING booking_id INTO p_out_booking_id;

    -- Ghi dữ liệu vào bảng BookingDetails
    FOR i IN 1..p_spaces_list.COUNT LOOP
        INSERT INTO BookingDetails (
            booking_id, space_id, expected_start_time, expected_end_time,
            price_at_booking, note, booking_channel
        ) VALUES (
            p_out_booking_id, p_spaces_list(i).space_id, p_spaces_list(i).expected_start_time, p_spaces_list(i).expected_end_time,
            p_spaces_list(i).price_at_booking, p_spaces_list(i).note, p_booking_channel
        );
    END LOOP;

    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_CreateBooking;
/