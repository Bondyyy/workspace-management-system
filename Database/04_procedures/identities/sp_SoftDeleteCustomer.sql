CREATE OR REPLACE PROCEDURE sp_SoftDeleteCustomer (
    p_customer_id IN NUMBER,
    p_result_code OUT NUMBER,   -- 0: thành công, 1: lỗi nghiệp vụ, -1: lỗi hệ thống
    p_result_msg OUT VARCHAR2
)
IS
    v_exists NUMBER;
    v_active_session NUMBER;
    v_active_booking NUMBER;
    v_user_id NUMBER;
BEGIN
    SELECT COUNT(*), MAX(user_id) INTO v_exists, v_user_id
    FROM Customers
    WHERE customer_id = p_customer_id
        AND is_deleted = 0;

    IF v_exists = 0 THEN
        p_result_code := 1;
        p_result_msg := 'Không tìm thấy hội viên hoặc hội viên đã bị xóa.';
        RETURN;
    END IF;

    SELECT COUNT(*) INTO v_active_session
    FROM Sessions
    WHERE customer_id = p_customer_id
      AND status = 'ACTIVE';

    IF v_active_session > 0 THEN
        p_result_code := 1;
        p_result_msg := 'Không thể xóa hội viên đang có phiên làm việc chưa kết thúc.';
        RETURN;
    END IF;

    SELECT COUNT(*) INTO v_active_booking
    FROM Bookings
    WHERE customer_id = p_customer_id
      AND status IN ('PENDING', 'BOOKED');

    IF v_active_booking > 0 THEN
        p_result_code := 1;
        p_result_msg := 'Không thể xóa hội viên đang có lịch đặt chỗ chưa hoàn tất.';
        RETURN;
    END IF;

    -- Soft delete tài khoản Customers
    UPDATE Customers
    SET is_deleted = 1, updated_at = CURRENT_TIMESTAMP
    WHERE  customer_id = p_customer_id;

    -- Soft delete tài khoản Users liên kết
    IF v_user_id IS NOT NULL THEN
        UPDATE Users
        SET    is_deleted = 1,
               updated_at = CURRENT_TIMESTAMP,
               status = 'INACTIVE'
        WHERE  user_id = v_user_id;
    END IF;

    COMMIT;

    p_result_code := 0;
    p_result_msg  := 'Xóa hội viên thành công.';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result_code := -1;
        p_result_msg  := 'Lỗi hệ thống: ' || SQLERRM;
END sp_SoftDeleteCustomer;
/