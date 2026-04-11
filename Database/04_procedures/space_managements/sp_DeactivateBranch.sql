CREATE OR REPLACE PROCEDURE sp_DeactivateBranch (
    p_branch_id IN NUMBER,
    p_result_code OUT NUMBER,   -- 0: thành công, 1: lỗi nghiệp vụ, -1: lỗi hệ thống
    p_result_msg OUT VARCHAR2
)
IS
    v_exists NUMBER;
    v_is_active NUMBER(1);
    v_active_session NUMBER;
    v_active_booking NUMBER;
BEGIN
    SELECT COUNT(*), MAX(is_active)
    INTO v_exists, v_is_active
    FROM Branches
    WHERE branch_id = p_branch_id;

    IF v_exists = 0 THEN
        p_result_code := 1;
        p_result_msg := 'Không tìm thấy chi nhánh.';
        RETURN;
    END IF;

    IF v_is_active = 0 THEN
        p_result_code := 1;
        p_result_msg := 'Chi nhánh đã bị vô hiệu hóa trước đó.';
        RETURN;
    END IF;

    -- Kiểm tra không còn session ACTIVE tại chi nhánh
    SELECT COUNT(*)
    INTO v_active_session
    FROM Sessions s
    JOIN SessionDetails sd ON s.session_id = sd.session_id
    JOIN Spaces sp ON sd.space_id = sp.space_id
    WHERE sp.branch_id = p_branch_id
      AND s.status = 'ACTIVE';

    IF v_active_session > 0 THEN
        p_result_code := 1;
        p_result_msg := 'Không thể vô hiệu hóa: chi nhánh đang có ' || v_active_session || ' phiên làm việc chưa kết thúc.';
        RETURN;
    END IF;

    -- Kiểm tra không còn booking PENDING hoặc BOOKED
    SELECT COUNT(*)
    INTO v_active_booking
    FROM Bookings b
    JOIN BookingDetails bd ON b.booking_id = bd.booking_id
    JOIN Spaces sp ON bd.space_id = sp.space_id
    WHERE sp.branch_id = p_branch_id
      AND b.status IN ('PENDING', 'BOOKED');

    IF v_active_booking > 0 THEN
        p_result_code := 1;
        p_result_msg := 'Không thể vô hiệu hóa: chi nhánh đang có ' || v_active_booking || ' lịch đặt chỗ chưa hoàn tất.';
        RETURN;
    END IF;

    -- Vô hiệu hóa toàn bộ không gian thuộc chi nhánh
    UPDATE Spaces
    SET is_active = 0, updated_at = CURRENT_TIMESTAMP
    WHERE branch_id = p_branch_id
      AND is_active = 1;

    UPDATE Branches
    SET is_active = 0, updated_at = CURRENT_TIMESTAMP
    WHERE branch_id = p_branch_id;

    COMMIT;

    p_result_code := 0;
    p_result_msg := 'Vô hiệu hóa chi nhánh thành công.';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result_code := -1;
        p_result_msg := 'Lỗi hệ thống: ' || SQLERRM;
END sp_DeactivateBranch;
/