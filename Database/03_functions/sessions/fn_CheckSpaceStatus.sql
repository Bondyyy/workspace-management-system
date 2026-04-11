CREATE OR REPLACE FUNCTION fn_CheckSpaceStatus (
    p_spaceId IN NUMBER
) RETURN NUMBER
IS
    v_currentStatus VARCHAR2(20);
BEGIN
    -- 1. Get current status of the space
    SELECT current_status 
    INTO v_currentStatus
    FROM Spaces
    WHERE space_id = p_spaceId;

    -- 2. Return 1 if AVAILABLE, else return 0
    IF v_currentStatus = 'AVAILABLE' THEN
        RETURN 1;
    ELSE
        RETURN 0;
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
    WHEN OTHERS THEN
        RETURN 0;
END fn_CheckSpaceStatus;
/