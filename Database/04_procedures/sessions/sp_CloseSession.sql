CREATE OR REPLACE PROCEDURE sp_CloseSession (
    p_sessionId   IN NUMBER,
    p_staffId     IN NUMBER,
    p_outMessage  OUT VARCHAR2
)
AS
    v_sessionStatus  VARCHAR2(20);
BEGIN
    -- 1. Validate session status
    SELECT status INTO v_sessionStatus FROM Sessions 
    WHERE session_id = p_sessionId FOR UPDATE NOWAIT;

    IF v_sessionStatus != 'ACTIVE' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Phiên làm việc này đã kết thúc hoặc bị hủy trước đó!');
    END IF;

    -- 2. Checkout all spaces in this session
    UPDATE SessionDetails
    SET checkout_time = SYSTIMESTAMP, status = 'COMPLETED'
    WHERE session_id = p_sessionId AND status = 'ACTIVE';

    -- 3. Release spaces to cleaning queue
    UPDATE Spaces
    SET current_status = 'CLEANING'
    WHERE space_id IN (SELECT space_id FROM SessionDetails WHERE session_id = p_sessionId);

    -- 4. Close the main session
    UPDATE Sessions
    SET checkout_time = SYSTIMESTAMP, check_out_staff_id = p_staffId, status = 'COMPLETED'
    WHERE session_id = p_sessionId;

    -- 5. Commit transaction
    COMMIT;
    p_outMessage := 'Đóng phiên thành công. Vị trí đã chuyển sang chờ dọn dẹp.';
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_CloseSession;
/