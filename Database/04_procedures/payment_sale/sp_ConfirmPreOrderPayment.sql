CREATE OR REPLACE PROCEDURE sp_ConfirmPreOrderPayment (
    p_booking_id       IN NUMBER,
    p_amount_paid      IN NUMBER,
    p_transaction_code IN VARCHAR2 -- Mã giao dịch trả về từ cổng thanh toán
) IS
    v_current_status     VARCHAR2(20);
    v_customer_id        NUMBER;
    v_voucher_id         NUMBER;
    v_total_amount       NUMBER(15, 2);

    -- Các biến tính toán
    v_calculated_discount NUMBER(15, 2) := 0;
    v_required_amount     NUMBER(15, 2) := 0;
    v_new_payment_status  VARCHAR2(20);
BEGIN
    SELECT status, NVL(total_amount, 0), customer_id, voucher_id
    INTO v_current_status, v_total_amount, v_customer_id, v_voucher_id
    FROM Bookings
    WHERE booking_id = p_booking_id;

    -- Kiểm tra trạng thái hợp lệ
    IF v_current_status != 'PENDING' THEN
        RAISE_APPLICATION_ERROR(-20201, 'Đơn đặt chỗ không ở trạng thái chờ thanh toán.');
    END IF;
    --Tính số tiền thực tế khách cần trả
    v_calculated_discount := fn_CalculateDiscountAmount(
        p_customer_id     => v_customer_id,
        p_voucher_id      => v_voucher_id,
        p_subtotal_amount => v_total_amount
    );

    v_required_amount := v_total_amount - v_calculated_discount;
    IF v_required_amount < 0 THEN
        v_required_amount := 0;
    END IF;

    -- Phân loại thanh toán (Cọc/Trả hết)
    IF p_amount_paid >= v_required_amount THEN
        v_new_payment_status := 'PAID';           -- Đã trả đủ
    ELSIF p_amount_paid > 0 THEN
        v_new_payment_status := 'PARTIAL'; -- Cọc một phần
    ELSE
        RAISE_APPLICATION_ERROR(-20202, 'Số tiền thanh toán không hợp lệ (phải > 0).');
    END IF;

    -- Trigger QR chạy khi payment_status thay đổi.
    -- Trigger Bàn chạy khi status thay đổi.
    -- Cập nhật dữ liệu vào bảng Bookings
    UPDATE Bookings
    SET status = 'BOOKED',
        payment_status = v_new_payment_status,
        deposit_amount = p_amount_paid,         -- Tiền hội viên chuyển
        discount_amount = v_calculated_discount, -- Tiền giảm giá (Voucher + Member)
        final_amount = v_required_amount,       -- Tiền phải trả sau giảm
        note = note || ' | Thu tiền qua GD: ' || p_transaction_code || ' (' || v_new_payment_status || ')',
        updated_at = CURRENT_TIMESTAMP
    WHERE booking_id = p_booking_id;

    COMMIT;
END;
//