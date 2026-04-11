CREATE OR REPLACE FUNCTION fn_CalculateRentalCost (
    p_startTime   IN TIMESTAMP,
    p_endTime     IN TIMESTAMP,
    p_hourlyRate  IN NUMBER
) RETURN NUMBER
IS
    v_totalMinutes  NUMBER;
    v_hours         NUMBER;
    v_remainder     NUMBER;
    v_billableHours NUMBER(10, 2);
BEGIN
    -- 1. Calculate total minutes difference
    v_totalMinutes := EXTRACT(DAY FROM (p_endTime - p_startTime)) * 24 * 60
                    + EXTRACT(HOUR FROM (p_endTime - p_startTime)) * 60
                    + EXTRACT(MINUTE FROM (p_endTime - p_startTime));

    IF v_totalMinutes <= 0 THEN
        RETURN 0;
    END IF;

    -- 2. Extract hours and remaining minutes
    v_hours := TRUNC(v_totalMinutes / 60);
    v_remainder := MOD(v_totalMinutes, 60);

    -- 3. Apply rounding rules: > 15 mins = +0.5 hour, > 45 mins = +1.0 hour
    IF v_remainder <= 15 THEN
        v_billableHours := v_hours;
    ELSE 
        v_billableHours := v_hours + 1.0;
    END IF;

    -- 4. Minimum charge for at least 0.5 hour if usage > 0
    IF v_billableHours = 0 AND v_totalMinutes > 0 THEN
        v_billableHours := 0.5;
    END IF;

    -- 5. Return final cost
    RETURN v_billableHours * p_hourlyRate;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END fn_CalculateRentalCost;
/