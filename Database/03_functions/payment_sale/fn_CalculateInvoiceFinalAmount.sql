CREATE OR REPLACE FUNCTION fn_CalculateInvoiceFinalAmount (
    p_invoice_id IN NUMBER,
    p_voucher_id IN NUMBER DEFAULT NULL -- Cho phép truyền NULL nếu khách không xài voucher
) RETURN NUMBER
IS
    v_customer_id NUMBER;
    v_sub_total NUMBER(15, 2) := 0;
    v_line_total NUMBER(15, 2) := 0;
    v_discount NUMBER(15, 2) := 0;
    v_final_amount NUMBER(15, 2) := 0;
BEGIN
    -- 1. Lấy thông tin khách hàng từ Hóa đơn (để check hạng thành viên)
    SELECT customer_id INTO v_customer_id
    FROM Invoices
    WHERE invoice_id = p_invoice_id;

    -- 2. Tính tổng tiền Sub-total từ các dòng hóa đơn (InvoiceLines)
    FOR rec IN (
        SELECT reference_type, reference_id, subtotal
        FROM InvoiceLines
        WHERE invoice_id = p_invoice_id
    ) LOOP
        -- Nếu là tiền giờ ngồi, gọi hàm tính lại theo thời gian thực (vì giờ vẫn đang trôi)
        IF rec.reference_type = 'SESSION' THEN
            v_line_total := fn_CalculateSessionBaseCost(rec.reference_id);
        ELSE
            -- Với đồ ăn thức uống hoặc Booking, lấy nguyên giá trị đã lưu
            v_line_total := rec.subtotal;
        END IF;
        
        v_sub_total := v_sub_total + v_line_total;
    END LOOP;

    -- 3. Tính toán số tiền được giảm giá (Từ Hạng thành viên + Voucher)
    v_discount := fn_CalculateDiscountAmount(
        p_customer_id => v_customer_id,
        p_voucher_id => p_voucher_id,
        p_subtotal_amount => v_sub_total
    );

    -- 4. Tính tổng tiền cuối cùng
    v_final_amount := v_sub_total - v_discount;

    -- Đảm bảo tổng tiền không bị âm (phòng trường hợp giảm giá quá tay)
    IF v_final_amount < 0 THEN
        v_final_amount := 0;
    END IF;

    RETURN v_final_amount;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
    WHEN OTHERS THEN
        RETURN 0; -- Trả về 0 nếu có lỗi bất ngờ để app không bị crash
END fn_CalculateInvoiceFinalAmount;
/