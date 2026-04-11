CREATE OR REPLACE PROCEDURE sp_DeactivateSpace (
    p_space_id IN NUMBER,
    p_result_code OUT NUMBER,   -- 0: thành công, 1: lỗi nghiệp vụ, -1: lỗi hệ thống
    p_result_msg OUT VARCHAR2
)
IS
    v_exists NUMBER;
    v_is_active NUMBER(1);
    v_current_status VARCHAR2(20);
BEGIN
    SELECT COUNT(*), MAX(is_active), MAX(current_status)
    INTO v_exists, v_is_active, v_current_status
    FROM Spaces
    WHERE space_id = p_space_id;

    IF v_exists = 0 THEN
        p_result_code := 1;
        p_result_msg := 'Không tìm thấy không gian.';
        RETURN;
    END IF;

    IF v_is_active = 0 THEN
        p_result_code := 1;
        p_result_msg := 'Không gian đã bị vô hiệu hóa trước đó.';
        RETURN;
    END IF;

    -- Không cho vô hiệu hóa khi đang có khách hoặc đã đặt trước
    IF v_current_status IN ('OCCUPIED', 'BOOKED') THEN
        p_result_code := 1;
        p_result_msg := 'Không thể vô hiệu hóa: không gian đang ở trạng thái ' || v_current_status || '.';
        RETURN;
    END IF;

    -- Không cho vô hiệu hoá khi có khách đang dùng
    DECLARE
        v_active_session NUMBER;
    BEGIN
        SELECT COUNT(*)
        INTO v_active_session
        FROM Sessions s
        JOIN SessionDetails sd ON s.session_id = sd.session_id
        WHERE sd.space_id = p_space_id
          AND sd.status = 'ACTIVE';

        IF v_active_session > 0 THEN
            p_result_code := 1;
            p_result_msg := 'Không thể vô hiệu hóa: không gian đang có phiên làm việc chưa kết thúc.';
            RETURN;
        END IF;
    END;

    UPDATE Spaces
    SET is_active = 0, updated_at = CURRENT_TIMESTAMP
    WHERE space_id = p_space_id;

    COMMIT;

    p_result_code := 0;
    p_result_msg := 'Vô hiệu hóa không gian thành công.';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result_code := -1;
        p_result_msg := 'Lỗi hệ thống: ' || SQLERRM;
END sp_DeactivateSpace;
/