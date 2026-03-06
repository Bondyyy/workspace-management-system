CREATE OR REPLACE PROCEDURE sp_AddSessionExtension (
    p_session_id          IN NUMBER,
    p_extended_duration   IN NUMBER,          -- Thời gian gia hạn (giả định đơn vị tính là Giờ)
    p_request_channel     IN VARCHAR2,        -- Kênh yêu cầu: 'APP' hoặc 'RECEPTION'
    p_staff_id            IN NUMBER,          -- NULL nếu khách tự gia hạn qua APP, có ID nếu Lễ tân thao tác
    p_out_extension_id    OUT NUMBER,         -- Trả về ID của phiếu gia hạn
    p_out_message         OUT VARCHAR2        -- Thông báo kết quả
)
AS
    v_session_status      VARCHAR2(20);
    v_applied_rate        NUMBER(15, 2);
    v_booking_id          NUMBER;
    v_space_id            NUMBER;

    v_current_end_time    TIMESTAMP;
    v_new_end_time        TIMESTAMP;
    v_cost_incurred       NUMBER(15, 2);
    v_extension_id        NUMBER;
BEGIN
    -- =================================================================
    -- BƯỚC 1: LẤY THÔNG TIN SESSION VÀ KHÓA DỮ LIỆU
    -- =================================================================
    SELECT status, applied_hourly_rate, booking_id, space_id
    INTO v_session_status, v_applied_rate, v_booking_id, v_space_id
    FROM Sessions
    WHERE session_id = p_session_id
    FOR UPDATE NOWAIT; -- Khóa session để tránh bị checkout giữa chừng lúc đang gia hạn

    IF v_session_status != 'ACTIVE' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Lỗi: Chỉ có thể gia hạn cho phiên làm việc đang hoạt động (ACTIVE).');
    END IF;

    IF p_extended_duration <= 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Lỗi: Thời gian gia hạn phải lớn hơn 0.');
    END IF;

    -- =================================================================
    -- BƯỚC 2: TÌM THỜI ĐIỂM BẮT ĐẦU GIA HẠN (start_time)
    -- =================================================================

    -- Ưu tiên 1: Lấy thời gian kết thúc của lần gia hạn gần nhất (Nếu khách gia hạn nhiều lần)
    SELECT MAX(end_time) INTO v_current_end_time
    FROM SessionExtensions
    WHERE session_id = p_session_id;

    -- Ưu tiên 2: Nếu chưa từng gia hạn, lấy expected_end_time từ BookingDetails (Nếu khách có đặt trước)
    IF v_current_end_time IS NULL AND v_booking_id IS NOT NULL THEN
        BEGIN
            SELECT expected_end_time INTO v_current_end_time
            FROM BookingDetails
            WHERE booking_id = v_booking_id AND space_id = v_space_id
            FETCH FIRST 1 ROWS ONLY;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_current_end_time := NULL;
        END;
    END IF;

    -- Ưu tiên 3: Nếu là khách vãng lai (không có giờ kết thúc dự kiến ban đầu), lấy thời gian hiện tại
    IF v_current_end_time IS NULL THEN
        v_current_end_time := SYSTIMESTAMP;
    END IF;

    -- =================================================================
    -- BƯỚC 3: TÍNH TOÁN THỜI GIAN KẾT THÚC & CHI PHÍ PHÁT SINH
    -- =================================================================

    -- Cộng thêm số giờ gia hạn vào mốc thời gian hiện tại (Sử dụng hàm của Oracle)
    v_new_end_time := v_current_end_time + NUMTODSINTERVAL(p_extended_duration, 'HOUR');

    -- Tính tiền = Số giờ gia hạn * Giá áp dụng tại thời điểm Check-in
    v_cost_incurred := p_extended_duration * v_applied_rate;

    -- =================================================================
    -- BƯỚC 4: GHI NHẬN VÀO BẢNG SessionExtensions
    -- =================================================================
    INSERT INTO SessionExtensions (
        session_id, extended_duration, start_time, end_time,
        cost_incurred, payment_status, request_channel, created_by
    ) VALUES (
        p_session_id, p_extended_duration, v_current_end_time, v_new_end_time,
        v_cost_incurred, 'UNPAID', p_request_channel, p_staff_id
    ) RETURNING extension_id INTO v_extension_id;

    -- Hoàn tất giao dịch
    COMMIT;

    p_out_extension_id := v_extension_id;
    p_out_message := 'Gia hạn thành công thêm ' || p_extended_duration || ' giờ. Phí phát sinh: ' || v_cost_incurred || ' VNĐ.';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_AddSessionExtension;
/