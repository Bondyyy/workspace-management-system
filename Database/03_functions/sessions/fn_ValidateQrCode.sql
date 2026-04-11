CREATE OR REPLACE FUNCTION fn_ValidateQrCode (
    p_qrCode    IN VARCHAR2,
    p_branchId  IN NUMBER
) RETURN NUMBER
IS
    v_bookingId     NUMBER;
    v_status        VARCHAR2(20);
    v_spaceBranchId NUMBER;
    v_startTime     TIMESTAMP;
    v_endTime       TIMESTAMP;
BEGIN
    -- 1. Check if QR code exists
    BEGIN
        SELECT booking_id, status 
        INTO v_bookingId, v_status
        FROM Bookings 
        WHERE qr_code = p_qrCode;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN 0; -- Code 0: QR does not exist
    END;

    -- 2. Check if already used or invalid status
    IF v_status IN ('ACTIVE', 'COMPLETED', 'CANCELLED') THEN
        RETURN -3; -- Code -3: Already used or cancelled
    END IF;

    -- 3. Retrieve booking details for branch and time validation
    SELECT s.branch_id, bd.expected_start_time, bd.expected_end_time
    INTO v_spaceBranchId, v_startTime, v_endTime
    FROM BookingDetails bd
    JOIN Spaces s ON bd.space_id = s.space_id
    WHERE bd.booking_id = v_bookingId
    FETCH FIRST 1 ROWS ONLY;

    -- 4. Validate branch match
    IF v_spaceBranchId != p_branchId THEN
        RETURN -1; -- Code -1: Wrong branch
    END IF;

    -- 5. Validate timeframe (e.g., QR is expired)
    IF SYSTIMESTAMP > v_endTime THEN
        RETURN -2; -- Code -2: Expired waiting time
    END IF;

    -- 6. Passed all validations
    RETURN 1; -- Code 1: Valid
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END fn_ValidateQrCode;
/