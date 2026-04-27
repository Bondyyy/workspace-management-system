CREATE OR REPLACE TRIGGER trg_UpdateSessionOrderTotal
AFTER INSERT OR UPDATE OR DELETE ON SessionOrderDetails
FOR EACH ROW
BEGIN
    -- 1. Add subtotal to SessionOrders on INSERT
    IF INSERTING THEN
        UPDATE SessionOrders
        SET total_price = NVL(total_price, 0) + :NEW.subtotal
        WHERE order_id = :NEW.order_id;

    -- 2. Adjust difference on UPDATE
    ELSIF UPDATING THEN
        UPDATE SessionOrders
        SET total_price = NVL(total_price, 0) - :OLD.subtotal + :NEW.subtotal
        WHERE order_id = :NEW.order_id;

    -- 3. Subtract subtotal on DELETE
    ELSIF DELETING THEN
        UPDATE SessionOrders
        SET total_price = NVL(total_price, 0) - :OLD.subtotal
        WHERE order_id = :OLD.order_id;
    END IF;
END;
/