CREATE OR REPLACE TRIGGER trg_CapNhatTrangThaiChoThanhToan
BEFORE INSERT ON DATCHO
FOR EACH ROW
BEGIN
    IF :NEW.TrangThaiDatTruoc IS NULL THEN
        :NEW.TrangThaiDatTruoc := 'Đang chờ thanh toán';
    END IF;

    :NEW.ThoiGianDat := CURRENT_TIMESTAMP;
    :NEW.CapNhatLanCuoi := CURRENT_TIMESTAMP;
END;
/
