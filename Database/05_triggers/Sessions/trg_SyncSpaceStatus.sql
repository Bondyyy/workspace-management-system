CREATE OR REPLACE TRIGGER trg_SyncSpaceStatus
AFTER UPDATE OF status ON SessionDetails
FOR EACH ROW
WHEN (NEW.status = 'COMPLETED' AND OLD.status = 'ACTIVE')
BEGIN
    -- 1. Update space status to CLEANING automatically
    UPDATE Spaces
    SET current_status = 'CLEANING'
    WHERE space_id = :NEW.space_id;
END;
/