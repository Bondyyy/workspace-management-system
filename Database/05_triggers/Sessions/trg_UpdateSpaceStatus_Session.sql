CREATE OR REPLACE TRIGGER trg_UpdateSpaceStatus_Session
AFTER INSERT OR UPDATE OF checkout_time ON Sessions
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        UPDATE Spaces
        SET current_status = 'OCCUPIED'
        WHERE space_id = :NEW.space_id;

    ELSIF UPDATING AND :NEW.checkout_time IS NOT NULL AND :OLD.checkout_time IS NULL THEN
        UPDATE Spaces
        SET current_status = 'CLEANING'
        WHERE space_id = :NEW.space_id;
    END IF;
END;
/