CREATE OR REPLACE PROCEDURE sp_CreatePayment(
    p_invoice_id      IN NUMBER,
    p_payment_method  IN VARCHAR2, -- Phương thức thanh toán: 'CASH', 'VNPAY', 'MOMO', 'CREDIT_CARD'
    p_amount_given    IN NUMBER,   -- Số tiền khách đưa hoặc số tiền đã chuyển
    p_transaction_ref IN VARCHAR2 DEFAULT NULL,
    p_note            IN VARCHAR2 DEFAULT NULL,
    p_result_code     OUT NUMBER,
    p_result_msg      OUT VARCHAR2
) AS
    v_invoice_total NUMBER(15,2);
    v_invoice_status VARCHAR2(20);
    v_amount_to_record NUMBER(15,2);
BEGIN
    -- Lỗi truy xuất hóa đơn
    BEGIN
        SELECT total_amount, status 
        INTO v_invoice_total, v_invoice_status 
        FROM Invoices 
        WHERE invoice_id = p_invoice_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_result_code := 1;
            p_result_msg := 'Lỗi truy xuất dữ liệu hóa đơn. Vui lòng thử lại.';
            RETURN;
    END;

    -- Kiểm tra hóa đơn đã thanh toán hoặc bị hủy chưa
    IF v_invoice_status = 'PAID' THEN
        p_result_code := 1;
        p_result_msg := 'Hóa đơn này đã được thanh toán hoàn tất trước đó.';
        RETURN;
    ELSIF v_invoice_status = 'CANCELLED' THEN
        p_result_code := 1;
        p_result_msg := 'Hóa đơn này đã bị hủy, không thể thanh toán.';
        RETURN;
    END IF;

    -- Số tiền khách đưa không đủ (Chỉ áp dụng tiền mặt)
    IF p_payment_method = 'CASH' AND p_amount_given < v_invoice_total THEN
        p_result_code := 1;
        p_result_msg := 'Số tiền khách đưa chưa đủ để thanh toán.';
        RETURN;
    END IF;

    -- Ghi nhận thanh toán
    IF p_payment_method = 'CASH' THEN
        v_amount_to_record := v_invoice_total; 
    ELSE
        v_amount_to_record := p_amount_given; 
    END IF;

    INSERT INTO Payments (
        invoice_id, payment_method, amount, status, transaction_reference, note
    ) VALUES (
        p_invoice_id, p_payment_method, v_amount_to_record, 'SUCCESS', p_transaction_ref, p_note
    );

    COMMIT;
    
    p_result_code := 0;
    p_result_msg := 'Thanh toán thành công'; 

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result_code := -1;
        p_result_msg := 'Lỗi truy xuất dữ liệu hóa đơn. Vui lòng thử lại.'; -- Fallback cho lỗi DB
END sp_CreatePayment;
/