CREATE OR REPLACE FUNCTION fn_CalculateTotalFnBCost (
    p_sessionId IN NUMBER
) RETURN NUMBER
IS
    v_totalCost NUMBER(15, 2) := 0;
BEGIN
    -- 1. Sum total_price from SessionOrders for the given session
    SELECT NVL(SUM(total_price), 0)
    INTO v_totalCost
    FROM SessionOrders
    WHERE session_id = p_sessionId
      AND order_status != 'CANCELLED';

    -- 2. Return total F&B cost
    RETURN v_totalCost;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END fn_CalculateTotalFnBCost;
/