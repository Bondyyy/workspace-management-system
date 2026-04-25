CREATE OR REPLACE TRIGGER trg_InitCustomerOnInsert
BEFORE INSERT ON Customers
FOR EACH ROW
DECLARE
    v_tier_id NUMBER;
BEGIN
    -- Sinh customer_code: KH-YYYYMMDD-000001
    IF :NEW.customer_code IS NULL THEN
        :NEW.customer_code := 'KH-'
            || TO_CHAR(SYSDATE, 'YYYYMMDD')
            || '-'
            || LPAD(TO_CHAR(seq_customer_code.NEXTVAL), 6, '0');
    END IF;

    -- Gán hạng thành viên ban đầu
    IF :NEW.membership_tier_id IS NULL THEN
        v_tier_id := fn_GetCustomerTierByPoints(NVL(:NEW.loyalty_points, 0));
        :NEW.membership_tier_id := v_tier_id;
    END IF;

    IF :NEW.loyalty_points IS NULL THEN
        :NEW.loyalty_points := 0;
    END IF;

    IF :NEW.lifetime_spending IS NULL THEN
        :NEW.lifetime_spending := 0;
    END IF;

END trg_InitCustomerOnInsert;
/