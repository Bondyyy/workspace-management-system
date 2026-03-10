CREATE OR REPLACE PROCEDURE sp_ProcessCheckOut (
    p_session_id      IN NUMBER,
    p_staff_id        IN NUMBER,
    p_payment_method  IN VARCHAR2,
    p_voucher_id      IN NUMBER,
    p_out_invoice_id  OUT NUMBER,
    p_out_message     OUT VARCHAR2
)
AS
    v_customer_id     NUMBER;
    v_booking_id      NUMBER;
    v_session_status  VARCHAR2(20);

    v_duration_hours  NUMBER(10, 2);
    v_base_cost       NUMBER(15, 2) := 0;
    v_fnb_cost        NUMBER(15, 2) := 0;
    v_extension_cost  NUMBER(15, 2) := 0;

    v_subtotal        NUMBER(15, 2) := 0;
    v_discount        NUMBER(15, 2) := 0;
    v_deposit         NUMBER(15, 2) := 0;
    v_final_amount    NUMBER(15, 2) := 0;

    v_invoice_id      NUMBER;
BEGIN
    SELECT customer_id, booking_id, status
    INTO v_customer_id, v_booking_id, v_session_status
    FROM Sessions
    WHERE session_id = p_session_id
    FOR UPDATE NOWAIT;

    IF v_session_status != 'ACTIVE' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Phiên làm việc này đã được thanh toán hoặc đã hủy!');
    END IF;

    -- Tính tiền thuê không gian cơ bản
    FOR rec IN (
        SELECT detail_id, space_id, checkin_time, applied_hourly_rate
        FROM SessionDetails
        WHERE session_id = p_session_id AND status = 'ACTIVE'
        FOR UPDATE NOWAIT
    ) LOOP
        v_duration_hours := EXTRACT(DAY FROM (SYSTIMESTAMP - rec.checkin_time)) * 24
                          + EXTRACT(HOUR FROM (SYSTIMESTAMP - rec.checkin_time))
                          + EXTRACT(MINUTE FROM (SYSTIMESTAMP - rec.checkin_time)) / 60;

        IF v_duration_hours < 0 THEN v_duration_hours := 0; END IF;

        -- Cộng dồn tiền của từng bàn
        v_base_cost := v_base_cost + (v_duration_hours * rec.applied_hourly_rate);

        -- Đóng SessionDetails của bàn đó
        UPDATE SessionDetails
        SET checkout_time = SYSTIMESTAMP,
            status = 'COMPLETED'
        WHERE detail_id = rec.detail_id;

        UPDATE Spaces
        SET current_status = 'AVAILABLE'
        WHERE space_id = rec.space_id;
    END LOOP;

    -- Tính tổng tiền SessionOrders
    SELECT NVL(SUM(total_price), 0) INTO v_fnb_cost
    FROM SessionOrders
    WHERE session_id = p_session_id AND order_status != 'CANCELLED';

    -- Tính tổng tiền SessionExtensions
    SELECT NVL(SUM(cost_incurred), 0) INTO v_extension_cost
    FROM SessionExtensions
    WHERE session_id = p_session_id;

    v_subtotal := v_base_cost + v_fnb_cost + v_extension_cost;

    -- Áp dụng giảm giá và trừ cọc
    v_discount := fn_CalculateDiscountAmount(v_customer_id, p_voucher_id, v_subtotal);

    IF v_booking_id IS NOT NULL THEN
        SELECT NVL(deposit_amount, 0) INTO v_deposit
        FROM Bookings
        WHERE booking_id = v_booking_id;
    END IF;

    v_final_amount := v_subtotal - v_discount - v_deposit;
    IF v_final_amount < 0 THEN
        v_final_amount := 0;
    END IF;

    -- Tạo Hóa Đơn
    INSERT INTO Invoices (customer_id, staff_id, sub_total, discount_amount, total_amount, status)
    VALUES (v_customer_id, p_staff_id, v_subtotal, v_discount, v_final_amount, 'PAID')
    RETURNING invoice_id INTO v_invoice_id;

    -- Lưu lịch sử thanh Toán
    INSERT INTO Payments (invoice_id, payment_method, amount, status, note)
    VALUES (v_invoice_id, p_payment_method, v_final_amount, 'SUCCESS', 'Thanh toán Checkout cho Session ' || p_session_id);

    -- Đóng Session, Booking
    UPDATE Sessions
    SET checkout_time = SYSTIMESTAMP,
        check_out_staff_id = p_staff_id,
        status = 'COMPLETED'
    WHERE session_id = p_session_id;

    IF v_booking_id IS NOT NULL THEN
        UPDATE Bookings SET status = 'COMPLETED', payment_status = 'PAID' WHERE booking_id = v_booking_id;
    END IF;

    UPDATE SessionOrders SET payment_status = 'PAID' WHERE session_id = p_session_id;
    UPDATE SessionExtensions SET payment_status = 'PAID' WHERE session_id = p_session_id;

    COMMIT;
    p_out_invoice_id := v_invoice_id;
    p_out_message := 'Checkout thành công! Tổng tiền thanh toán: ' || v_final_amount || ' VNĐ.';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_ProcessCheckOut;
/