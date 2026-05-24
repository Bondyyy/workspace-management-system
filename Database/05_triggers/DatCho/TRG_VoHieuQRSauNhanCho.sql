CREATE OR REPLACE TRIGGER trg_VoHieuQRSauNhanCho
BEFORE UPDATE OF TrangThaiDatTruoc ON DATCHO
FOR EACH ROW
WHEN (NEW.TrangThaiDatTruoc = 'Đã sử dụng' AND OLD.TrangThaiDatTruoc = 'Đã thanh toán thành công')
BEGIN
    :NEW.MaQR := NULL;
    IF :NEW.GhiChu IS NULL OR INSTR(:NEW.GhiChu, '[SYSTEM_NO_SHOW]') = 0 THEN
        :NEW.GhiChu := NVL(:NEW.GhiChu, '') || ' | Khách đã nhận chỗ. QR bị vô hiệu hóa.';
    END IF;
    :NEW.CapNhatLanCuoi := CURRENT_TIMESTAMP;
END;
/
