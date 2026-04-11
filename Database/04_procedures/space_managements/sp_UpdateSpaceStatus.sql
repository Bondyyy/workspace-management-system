CREATE OR REPLACE PROCEDURE sp_UpdateSpaceStatus (
    p_space_id IN NUMBER,
    p_new_status IN VARCHAR2,  -- Chỉ chấp nhận: 'AVAILABLE', 'CLEANING', 'MAINTENANCE'
    p_result_code OUT NUMBER,   -- 0: thành công, 1: lỗi nghiệp vụ, -1: lỗi hệ thống
    p_result_msg OUT VARCHAR2
)
IS
    v_exists NUMBER;
    v_current_status VARCHAR2(20);
    v_is_active NUMBER(1);
    v_active_session NUMBER;
BEGIN
    -- Kiểm tra không gian tồn tại và đang hoạt động
    SELECT COUNT(*), MAX(current_status), MAX(is_active)
    INTO v_exists, v_current_status, v_is_active
    FROM Spaces
    WHERE space_id = p_space_id;

    IF v_exists = 0 OR v_is_active = 0 THEN
        p_result_code := 1;
        p_result_msg := 'Không tìm thấy không gian hoặc không gian đã bị vô hiệu hóa.';
        RETURN;
    END IF;

    -- Chặn cập nhật thủ công sang OCCUPIED hoặc BOOKED
    IF p_new_status IN ('OCCUPIED', 'BOOKED') THEN
        p_result_code := 1;
        p_result_msg  := 'Trạng thái ' || p_new_status
            || ' được hệ thống tự động quản lý. Không thể cập nhật thủ công.';
        RETURN;
    END IF;

    IF p_new_status NOT IN ('AVAILABLE', 'CLEANING', 'MAINTENANCE') THEN
        p_result_code := 1;
        p_result_msg  := 'Trạng thái không hợp lệ: ' || p_new_status
            || '. Chỉ chấp nhận AVAILABLE, CLEANING hoặc MAINTENANCE.';
        RETURN;
    END IF;

    -- Không cho chuyển sang AVAILABLE nếu còn session ACTIVE
    IF p_new_status = 'AVAILABLE' THEN
        SELECT COUNT(*)
        INTO v_active_session
        FROM Sessions s
        JOIN SessionDetails sd ON s.session_id = sd.session_id
        WHERE sd.space_id = p_space_id
            AND s.status = 'ACTIVE';

        IF v_active_session > 0 THEN
            p_result_code := 1;
            p_result_msg  := 'Không thể chuyển sang AVAILABLE: không gian đang có khách sử dụng.';
            RETURN;
        END IF;
    END IF;

    IF p_new_status = 'MAINTENANCE' AND v_current_status IN ('OCCUPIED', 'BOOKED') THEN
        p_result_code := 1;
        p_result_msg  := 'Không thể chuyển sang MAINTENANCE khi không gian đang được sử dụng hoặc đã đặt trước.';
        RETURN;
    END IF;


    UPDATE Spaces
    SET current_status = p_new_status, updated_at = CURRENT_TIMESTAMP
    WHERE space_id = p_space_id;

    COMMIT;

    p_result_code := 0;
    p_result_msg := 'Cập nhật trạng thái không gian thành ' || p_new_status || ' thành công.';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result_code := -1;
        p_result_msg := 'Lỗi hệ thống: ' || SQLERRM;
END sp_UpdateSpaceStatus;
/