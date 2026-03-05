CREATE OR REPLACE TRIGGER trg_CalcSubtotal_OrderDetails
BEFORE INSERT OR UPDATE ON SessionOrderDetails
FOR EACH ROW
BEGIN
    :NEW.subtotal := :NEW.quantity * :NEW.unit_price;
END;
/