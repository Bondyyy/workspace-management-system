CREATE OR REPLACE TRIGGER trg_UpdateCustomerLoyalty
AFTER UPDATE OF status ON Payments
FOR EACH ROW
WHEN (NEW.status = 'SUCCESS' AND OLD.status != 'SUCCESS')
DECLARE
    v_customer_id NUMBER;
    v_new_points NUMBER;
    v_new_tier_id NUMBER;
BEGIN
    -- 1. Tìm hóa đơn của ai
    BEGIN
        SELECT customer_id INTO v_customer_id
        FROM Invoices
        WHERE invoice_id = :NEW.invoice_id;

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_customer_id := NULL;
    END;

    --2 TÍNH ĐIỂM HÓA ĐƠN
    IF v_customer_id IS NOT NULL THEN
        UPDATE Customers
        SET lifetime_spending = lifetime_spending + :NEW.amount,
            loyalty_points  = loyalty_points + FLOOR(:NEW.amount / 10000) --cap nhat sau
        WHERE customer_id = v_customer_id
        RETURNING loyalty_points INTO v_new_points;

        --3 TÌM HẠNG THÀNH VIÊN
        SELECT tier_id INTO v_new_tier_id
        FROM  (
                SELECT tier_id FROM MembershipTiers
                WHERE min_points <= v_new_points
                ORDER BY min_points DESC
        )
        WHERE ROWNUM = 1;
        IF v_new_tier_id IS NOT NULL THEN
                UPDATE Customers
                SET membership_tier_id = v_new_tier_id
                WHERE customer_id = v_customer_id;
        END IF;
    END IF;
END;