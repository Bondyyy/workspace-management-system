CREATE OR REPLACE PROCEDURE sp_CreateBooking (
    p_customer_id         IN NUMBER,          -- NULL nếu là khách vãng lai
    p_booking_channel     IN VARCHAR2,        -- 'ONLINE' hoặc 'OFFLINE'
    p_note                IN VARCHAR2,        -- Ghi chú của khách
    p_created_by_staff_id IN NUMBER,          -- NULL nếu khách tự đặt online
    p_deposit_amount      IN NUMBER,          -- Tiền cọc (nếu có)
    p_spaces_list         IN t_space_booking_list, -- Danh sách các không gian muốn đặt
    p_out_booking_code    OUT VARCHAR2,       -- Trả về mã booking
    p_out_qr_code         OUT VARCHAR2        -- Trả về mã QR
)
AS
    v_booking_id    NUMBER;
    v_booking_code  VARCHAR2(50);
    v_qr_code       VARCHAR2(255);
    v_space_status  VARCHAR2(20);
    v_total_amount  NUMBER(15, 2) := 0;
BEGIN
    -- 1. Kiểm tra danh sách không gian đầu vào
    IF p_spaces_list IS NULL OR p_spaces_list.COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Danh sách không gian trống!');
    END IF;

    -- 2. Duyệt qua danh sách để kiểm tra tính hợp lệ và tính tổng tiền
    FOR i IN 1..p_spaces_list.COUNT LOOP
        BEGIN
            -- FOR UPDATE NOWAIT: Khóa dòng dữ liệu này lại. Nếu có ai khác đang giữ khóa (đang book), sẽ văng lỗi ngay lập tức
            SELECT current_status INTO v_space_status
            FROM Spaces
            WHERE space_id = p_spaces_list(i).space_id
            FOR UPDATE NOWAIT;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20002, 'Không tìm thấy không gian với ID: ' || p_spaces_list(i).space_id);
            WHEN OTHERS THEN
                RAISE_APPLICATION_ERROR(-20003, 'Không gian ID ' || p_spaces_list(i).space_id || ' đang được người khác thao tác. Vui lòng thử lại!');
        END;

        -- Kiểm tra trạng thái
        IF v_space_status != 'AVAILABLE' THEN
            RAISE_APPLICATION_ERROR(-20004, 'Không gian ID ' || p_spaces_list(i).space_id || ' không còn trống!');
        END IF;

        -- Cộng dồn giá tiền snapshot
        v_total_amount := v_total_amount + p_spaces_list(i).price_at_booking;
    END LOOP;

    -- 3. Sinh mã booking_code và qr_code (Sử dụng GUID cho mã QR để đảm bảo tính duy nhất)
    v_booking_code := 'BKG' || TO_CHAR(SYSDATE, 'YYMMDDHH24MI') || DBMS_RANDOM.STRING('X', 4);
    v_qr_code := RAWTOHEX(SYS_GUID());

    -- 4. Ghi dữ liệu vào bảng Bookings
    INSERT INTO Bookings (
        customer_id, booking_code, qr_code, total_amount, deposit_amount,
        final_amount, booking_channel, note, created_by_staff_id, status, payment_status
    ) VALUES (
        p_customer_id, v_booking_code, v_qr_code, v_total_amount, p_deposit_amount,
        (v_total_amount - p_deposit_amount), -- Tính final_amount tạm thời (chưa tính discount)
        p_booking_channel, p_note, p_created_by_staff_id, 'PENDING',
        CASE WHEN p_deposit_amount > 0 THEN 'PARTIAL' ELSE 'UNPAID' END
    ) RETURNING booking_id INTO v_booking_id;

    -- 5. Ghi dữ liệu vào bảng BookingDetails và Cập nhật trạng thái bảng Spaces
    FOR i IN 1..p_spaces_list.COUNT LOOP
        INSERT INTO BookingDetails (
            booking_id, space_id, expected_start_time, expected_end_time,
            price_at_booking, note, booking_channel
        ) VALUES (
            v_booking_id, p_spaces_list(i).space_id, p_spaces_list(i).expected_start_time, p_spaces_list(i).expected_end_time,
            p_spaces_list(i).price_at_booking, p_spaces_list(i).note, p_booking_channel
        );

        -- Đổi trạng thái không gian thành BOOKED
        UPDATE Spaces
        SET current_status = 'BOOKED'
        WHERE space_id = p_spaces_list(i).space_id;
    END LOOP;

    -- 6. Xác nhận giao dịch thành công và gán giá trị OUT
    COMMIT;
    p_out_booking_code := v_booking_code;
    p_out_qr_code := v_qr_code;

EXCEPTION
    WHEN OTHERS THEN
        -- Nếu có bất kỳ lỗi nào xảy ra (hoặc do code lỗi, hoặc do RAISE_APPLICATION_ERROR), rollback toàn bộ
        ROLLBACK;
        RAISE;
END sp_CreateBooking;
/