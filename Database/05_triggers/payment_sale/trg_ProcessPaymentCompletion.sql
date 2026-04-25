CREATE OR REPLACE TRIGGER trg_ProcessPaymentCompletion
AFTER INSERT OR UPDATE OF status ON Payments
FOR EACH ROW
WHEN (NEW.status = 'SUCCESS')
DECLARE
    PRAGMA AUTONOMOUS_TRANSACTION;
    v_total_paid NUMBER := 0;
    v_invoice_total NUMBER := 0;
BEGIN
    -- Tính tổng tiền
    SELECT NVL(SUM(amount), 0) INTO v_total_paid FROM Payments WHERE invoice_id = :NEW.invoice_id AND status = 'SUCCESS';
    SELECT total_amount INTO v_invoice_total FROM Invoices WHERE invoice_id = :NEW.invoice_id;
    
    -- Nếu đủ tiền
    IF v_total_paid >= v_invoice_total THEN
        
        -- 1. Chốt hóa đơn
        UPDATE Invoices SET status = 'PAID' WHERE invoice_id = :NEW.invoice_id;
        
        -- 2. Cập nhật giờ checkout
        UPDATE Sessions 
        SET checkout_time = CURRENT_TIMESTAMP
        WHERE session_id IN (
            SELECT reference_id FROM InvoiceLines 
            WHERE invoice_id = :NEW.invoice_id AND reference_type = 'SESSION'
        );
        
        UPDATE SessionDetails 
        SET checkout_time = CURRENT_TIMESTAMP
        WHERE session_id IN (
            SELECT reference_id FROM InvoiceLines 
            WHERE invoice_id = :NEW.invoice_id AND reference_type = 'SESSION'
        );
        
    END IF;
    
    COMMIT;
END;
/