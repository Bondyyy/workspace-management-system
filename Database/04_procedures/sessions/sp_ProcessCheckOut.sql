CREATE OR REPLACE PROCEDURE sp_ProcessCheckOut (
    p_session_id      IN NUMBER,
    p_staff_id        IN NUMBER,          -- Nhân viên lễ tân thao tác
    p_payment_method  IN VARCHAR2,        -- Phương thức thanh toán (CASH, MOMO...)
    p_voucher_id      IN NUMBER,          -- Mã voucher áp dụng (nếu có, NULL nếu không)
    p_out_invoice_id  OUT NUMBER,         -- Trả về mã hóa đơn vừa tạo
    p_out_message     OUT VARCHAR2        -- Thông báo kết quả
)
AS
    -- Biến lưu trữ thông tin Session
    v_customer_id     NUMBER;
    v_booking_id      NUMBER;
    v_space_id        NUMBER;
    v_checkin_time    TIMESTAMP;
    v_applied_rate    NUMBER(15, 2);
    v_session_status  VARCHAR2(20);

    -- Biến tính toán chi phí
    v_duration_hours  NUMBER(10, 2);      -- Tổng thời gian ngồi (quy ra giờ)
    v_base_cost       NUMBER(15, 2) := 0; -- Tiền thuê chỗ
    v_fnb_cost        NUMBER(15, 2) := 0; -- Tiền nước/đồ ăn
    v_extension_cost  NUMBER(15, 2) := 0; -- Tiền gia hạn

    v_subtotal        NUMBER(15, 2) := 0; -- Tổng tiền chưa giảm
    v_discount        NUMBER(15, 2) := 0; -- Tiền được giảm giá
    v_deposit         NUMBER(15, 2) := 0; -- Tiền cọc (nếu có)
    v_final_amount    NUMBER(15, 2) := 0; -- Số tiền khách cần trả cuối cùng

    v_invoice_id      NUMBER;
BEGIN
    -- =================================================================
    -- BƯỚC 1: LẤY THÔNG TIN PHIÊN VÀ KHÓA DỮ LIỆU (Tránh double-click)
    -- =================================================================
    SELECT customer_id, booking_id, space_id, checkin_time, applied_hourly_rate, status
    INTO v_customer_id, v_booking_id, v_space_id, v_checkin_time, v_applied_rate, v_session_status
    FROM Sessions
    WHERE session_id = p_session_id
    FOR UPDATE NOWAIT;

    IF v_session_status != 'ACTIVE' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Phiên làm việc này đã được thanh toán hoặc đã hủy!');
    END IF;

    -- =================================================================
    -- BƯỚC 2: TỔNG HỢP CÁC KHOẢN CHI PHÍ (SUBTOTAL)
    -- =================================================================

    -- 2.1 Tính tiền thuê không gian cơ bản (Quy đổi chênh lệch thời gian ra số giờ)
    v_duration_hours := EXTRACT(DAY FROM (SYSTIMESTAMP - v_checkin_time)) * 24
                      + EXTRACT(HOUR FROM (SYSTIMESTAMP - v_checkin_time))
                      + EXTRACT(MINUTE FROM (SYSTIMESTAMP - v_checkin_time)) / 60;

    -- Đảm bảo thời gian tính tiền không bị âm hoặc bằng 0 (khách vào ra ngay thì tính tối thiểu 1 khoản tùy bạn, ở đây tính thực tế)
    IF v_duration_hours < 0 THEN v_duration_hours := 0; END IF;
    v_base_cost := v_duration_hours * v_applied_rate;

    -- 2.2 Tính tổng tiền F&B (SessionOrders)
    SELECT NVL(SUM(total_price), 0) INTO v_fnb_cost
    FROM SessionOrders
    WHERE session_id = p_session_id AND order_status != 'CANCELLED';

    -- 2.3 Tính tổng phí gia hạn (SessionExtensions)
    SELECT NVL(SUM(cost_incurred), 0) INTO v_extension_cost
    FROM SessionExtensions
    WHERE session_id = p_session_id;

    -- TỔNG CỘNG:
    v_subtotal := v_base_cost + v_fnb_cost + v_extension_cost;

    -- =================================================================
    -- BƯỚC 3: ÁP DỤNG GIẢM GIÁ (Voucher / Membership) & TRỪ CỌC
    -- =================================================================

    -- Gọi Function bạn đã viết để lấy số tiền được giảm
    v_discount := fn_CalculateDiscountAmount(v_customer_id, p_voucher_id, v_subtotal);

    -- Lấy tiền cọc nếu khách đi từ luồng Booking
    IF v_booking_id IS NOT NULL THEN
        SELECT NVL(deposit_amount, 0) INTO v_deposit
        FROM Bookings
        WHERE booking_id = v_booking_id;
    END IF;

    -- Tính số tiền cuối cùng cần thanh toán
    v_final_amount := v_subtotal - v_discount - v_deposit;
    IF v_final_amount < 0 THEN
        v_final_amount := 0; -- Đảm bảo không bị âm tiền nếu cọc nhiều hơn dùng
    END IF;

    -- =================================================================
    -- BƯỚC 4: LƯU TRỮ VÀO DATABASE (Invoices, Payments, Đóng Session)
    -- =================================================================

    -- 4.1 Tạo Hóa Đơn (Giả định bảng Invoices có các cột cơ bản này)
    INSERT INTO Invoices (customer_id, subtotal_amount, discount_amount, total_amount, status)
    VALUES (v_customer_id, v_subtotal, v_discount, v_final_amount, 'COMPLETED')
    RETURNING invoice_id INTO v_invoice_id;

    -- 4.2 Lưu lịch sử Thanh Toán (Payments)
    INSERT INTO Payments (invoice_id, payment_method, amount, status, note)
    VALUES (v_invoice_id, p_payment_method, v_final_amount, 'SUCCESS', 'Thanh toán Checkout cho Session ' || p_session_id);

    -- 4.3 Đóng Session
    UPDATE Sessions
    SET checkout_time = SYSTIMESTAMP,
        check_out_staff_id = p_staff_id,
        status = 'COMPLETED'
    WHERE session_id = p_session_id;

    -- 4.4 Cập nhật các bảng liên quan để nhả tài nguyên
    -- Đổi trạng thái bàn thành AVAILABLE
    UPDATE Spaces SET current_status = 'AVAILABLE' WHERE space_id = v_space_id;

    -- Đóng Booking (nếu có)
    IF v_booking_id IS NOT NULL THEN
        UPDATE Bookings SET status = 'COMPLETED', payment_status = 'PAID' WHERE booking_id = v_booking_id;
    END IF;

    -- Đánh dấu các order và extension đã thanh toán
    UPDATE SessionOrders SET payment_status = 'PAID' WHERE session_id = p_session_id;
    UPDATE SessionExtensions SET payment_status = 'PAID' WHERE session_id = p_session_id;

    -- Hoàn tất giao dịch
    COMMIT;
    p_out_invoice_id := v_invoice_id;
    p_out_message := 'Checkout thành công! Tổng tiền thanh toán: ' || v_final_amount || ' VNĐ.';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_ProcessCheckOut;
/