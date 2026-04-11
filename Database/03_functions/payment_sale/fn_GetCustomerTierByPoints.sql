CREATE OR REPLACE FUNCTION fn_GetCustomerTierByPoints (
    p_points IN NUMBER
) RETURN NUMBER
IS
    v_tier_id NUMBER;
BEGIN
    SELECT tier_id INTO v_tier_id
    FROM (
        SELECT tier_id
        FROM MembershipTiers
        WHERE min_points <= p_points
        ORDER BY min_points DESC
    )
    WHERE ROWNUM = 1;

    RETURN v_tier_id;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN NULL;
    WHEN OTHERS THEN
        RETURN NULL;
END fn_GetCustomerTierByPoints;
/