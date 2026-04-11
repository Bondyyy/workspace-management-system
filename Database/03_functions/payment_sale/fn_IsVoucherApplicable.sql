CREATE OR REPLACE FUNCTION fn_IsVoucherApplicable (
    p_voucher_id IN NUMBER,
    p_order_value IN NUMBER   -- Tổng giá trị hóa đơn trước giảm giá
) RETURN NUMBER  -- 1: hợp lệ, 0: không hợp lệ
IS
    v_is_active NUMBER(1);
    v_effective_date DATE;
    v_expiry_date DATE;
    v_usage_limit NUMBER;
    v_used_count NUMBER;
    v_min_order NUMBER(15,2);
BEGIN
    SELECT is_active,
           effective_date,
           expiry_date,
           usage_limit,
           used_count,
           min_order_value
    INTO v_is_active,
         v_effective_date,
         v_expiry_date,
         v_usage_limit,
         v_used_count,
         v_min_order
    FROM Vouchers
    WHERE voucher_id = p_voucher_id;

    IF v_is_active = 0 THEN
        RETURN 0;
    END IF;

    -- Kiểm tra đã đến ngày hiệu lực chưa
    IF TRUNC(SYSDATE) < v_effective_date THEN
        RETURN 0;
    END IF;

    -- Kiểm tra chưa hết hạn
    IF TRUNC(SYSDATE) > v_expiry_date THEN
        RETURN 0;
    END IF;

    -- Kiểm tra còn lượt sử dụng (NULL = không giới hạn)
    IF v_usage_limit IS NOT NULL AND v_used_count >= v_usage_limit THEN
        RETURN 0;
    END IF;

    -- Kiểm tra giá trị hóa đơn đạt tối thiểu
    IF p_order_value < v_min_order THEN
        RETURN 0;
    END IF;

    RETURN 1;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;  -- Voucher không tồn tại
    WHEN OTHERS THEN
        RETURN 0;
END fn_IsVoucherApplicable;
/