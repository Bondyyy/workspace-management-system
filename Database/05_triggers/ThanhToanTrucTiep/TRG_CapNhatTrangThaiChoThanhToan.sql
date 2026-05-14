CREATE OR REPLACE TRIGGER trg_CapNhatTrangThaiChoThanhToan
BEFORE INSERT ON DATCHO
FOR EACH ROW
BEGIN
    IF :NEW.TrangThaiDatTruoc IS NULL THEN
        :NEW.TrangThaiDatTruoc := UNISTR('\0110ang ch\1EDD thanh to\00E1n');
    END IF;

    :NEW.ThoiGianDat := CURRENT_TIMESTAMP;
    :NEW.CapNhatLanCuoi := CURRENT_TIMESTAMP;
END;
/
