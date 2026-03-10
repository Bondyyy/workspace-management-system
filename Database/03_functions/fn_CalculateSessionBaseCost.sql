CREATE OR REPLACE FUNCTION fn_CalculateSessionBaseCost (
    p_session_id IN NUMBER 
) RETURN NUMBER
IS
    v_checkin_time TIMESTAMP;
    v_checkout_time TIMESTAMP;
    v_applied_rate NUMBER(15, 2);
    v_duration_hours NUMBER(10, 4);
    v_base_cost NUMBER(15, 2);
BEGIN
    SELECT checkin_time, checkout_time, applied_hourly_rate
    INTO v_checkin_time, v_checkout_time, v_applied_rate
    FROM Sessions
    WHERE session_id = p_session_id;

    IF v_checkout_time IS NULL THEN
        v_checkout_time := SYSTIMESTAMP;
    END IF;

    v_duration_hours := EXTRACT(DAY FROM (v_checkout_time - v_checkin_time)) * 24
                      + EXTRACT(HOUR FROM (v_checkout_time - v_checkin_time))
                      + EXTRACT(MINUTE FROM (v_checkout_time - v_checkin_time)) / 60;

    IF v_duration_hours < 0 THEN
        v_duration_hours := 0;
    END IF;

    v_base_cost := v_duration_hours * NVL(v_applied_rate, 0);

    RETURN v_base_cost;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
    WHEN OTHERS THEN
        RETURN 0;
END fn_CalculateSessionBaseCost;
/
