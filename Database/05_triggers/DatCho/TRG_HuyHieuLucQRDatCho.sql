CREATE OR REPLACE TRIGGER TRG_HuyHieuLucVeCheckIn
AFTER INSERT ON PHIENLAMVIEC
FOR EACH ROW
WHEN (NEW.MaDatCho IS NOT NULL)
BEGIN
    IF LOWER(:NEW.TrangThaiPhien) NOT LIKE '%t tr%' THEN
        UPDATE DATCHO
        SET TrangThaiDatTruoc = 'Đã sử dụng'
        WHERE MaDatCho = :NEW.MaDatCho;
    END IF;
END;
/
