CREATE OR REPLACE FUNCTION fn_CalculateSessionBaseCost (
    p_session_id IN NUMBER 
) RETURN NUMBER
IS
    v_total_base_cost NUMBER(15, 2) := 0;
BEGIN
    -- Quét toàn bộ các bàn trong Session này để tính tổng tiền
    FOR rec IN (
        SELECT checkin_time, checkout_time, applied_hourly_rate
        FROM SessionDetails
        WHERE session_id = p_session_id
    ) LOOP
        DECLARE
            v_end_time TIMESTAMP;
            v_duration_hours NUMBER(10, 4);
        BEGIN
            -- Nếu bàn chưa checkout, lấy giờ hiện tại làm mốc để tính tạm
            v_end_time := NVL(rec.checkout_time, SYSTIMESTAMP);

            v_duration_hours := EXTRACT(DAY FROM (v_end_time - rec.checkin_time)) * 24
                              + EXTRACT(HOUR FROM (v_end_time - rec.checkin_time))
                              + EXTRACT(MINUTE FROM (v_end_time - rec.checkin_time)) / 60;

            IF v_duration_hours < 0 THEN
                v_duration_hours := 0;
            END IF;

            -- Cộng dồn tiền của từng bàn vào tổng
            v_total_base_cost := v_total_base_cost + (v_duration_hours * rec.applied_hourly_rate);
        END;
    END LOOP;

    RETURN v_total_base_cost;

EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END fn_CalculateSessionBaseCost;
/