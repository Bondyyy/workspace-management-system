CREATE OR REPLACE FUNCTION fn_IsCustomerInActiveSession (
    p_customerId IN NUMBER
) RETURN NUMBER IS
    v_active_count NUMBER;
BEGIN
    -- Khách vãng lai
    IF p_customerId IS NULL THEN
        RETURN 0;
    END IF;

    -- Đếm các Session đang ACTIVE
    SELECT COUNT(*)
    INTO v_active_count
    FROM Sessions
    WHERE customer_id = p_customerId
      AND status = 'ACTIVE';

    IF v_active_count > 0 THEN
        RETURN 1; -- Đang có ít nhất 1 session hoạt động
    ELSE
        RETURN 0; -- Đã check-out hết/chưa có session
    END IF;
END;
/