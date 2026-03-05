CREATE OR REPLACE TRIGGER trg_CalculateOrderTotal
AFTER INSERT OR UPDATE OR DELETE ON SessionOrderDetails
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        UPDATE SessionOrders
        SET total_price = total_price + :NEW.subtotal
        WHERE order_id = :NEW.order_id;

    ELSIF UPDATING THEN
        UPDATE SessionOrders
        SET total_price = total_price - :OLD.subtotal + :NEW.subtotal
        WHERE order_id = :NEW.order_id;

    ELSIF DELETING THEN
        UPDATE SessionOrders
        SET total_price = total_price - :OLD.subtotal
        WHERE order_id = :OLD.order_id;
    END IF;
END;
/