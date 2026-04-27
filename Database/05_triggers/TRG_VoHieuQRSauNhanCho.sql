CREATE OR REPLACE TRIGGER trg_VoHieuQRSauNhanCho
BEFORE UPDATE OF TrangThaiDatTruoc ON DATCHO
FOR EACH ROW
WHEN (NEW.TrangThaiDatTruoc IS 'Đã sử dụng' AND OLD.TrangThaiDatTruoc IS 'Đã thanh toán thành công')
BEGIN
    :NEW.MaQR := NULL;
    :NEW.GhiChu := :NEW.GhiChu || ' | Khách đã nhận chỗ. QR bị vô hiệu hóa.';
    :NEW.CapNhatLanCuoi := CURRENT_TIMESTAMP;
END;
/
