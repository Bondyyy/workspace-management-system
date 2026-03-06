CREATE OR REPLACE FUNCTION fn_CalculateSessionBaseCost (
    p_session_id IN NUMBER
) RETURN NUMBER
IS
    v_checkin_time TIMESTAMP;
    v_checkout_time TIMESTAMP;
    v_applied_rate NUMBER(15, 2);
    v_duration_hours NUMBER(10, 4); -- Lấy 4 số thập phân để tính toán cho chuẩn
    v_base_cost NUMBER(15, 2);
BEGIN
    -- 1. Lấy dữ liệu từ bảng Sessions
    SELECT checkin_time, checkout_time, applied_hourly_rate
    INTO v_checkin_time, v_checkout_time, v_applied_rate
    FROM Sessions
    WHERE session_id = p_session_id;

    -- 2. Xử lý trường hợp phiên vẫn đang ACTIVE (chưa checkout)
    IF v_checkout_time IS NULL THEN
        v_checkout_time := SYSTIMESTAMP; -- Lấy thời điểm hiện tại để tính "Tiền tạm tính"
    END IF;

    -- 3. Tính toán khoảng thời gian (Quy đổi ra Giờ)
    v_duration_hours := EXTRACT(DAY FROM (v_checkout_time - v_checkin_time)) * 24 
                      + EXTRACT(HOUR FROM (v_checkout_time - v_checkin_time)) 
                      + EXTRACT(MINUTE FROM (v_checkout_time - v_checkin_time)) / 60;

    -- Chặn lỗi logic (nếu vì lý do nào đó thời gian bị âm)
    IF v_duration_hours < 0 THEN
        v_duration_hours := 0;
    END IF;

    -- 4. Tính tổng tiền thuê gốc
    v_base_cost := v_duration_hours * NVL(v_applied_rate, 0);

    RETURN v_base_cost;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        -- Trả về 0 nếu truyền sai session_id
        RETURN 0; 
    WHEN OTHERS THEN
        RETURN 0;
END fn_CalculateSessionBaseCost;
/
