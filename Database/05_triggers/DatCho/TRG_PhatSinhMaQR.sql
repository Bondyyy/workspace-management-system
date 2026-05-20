CREATE OR REPLACE TRIGGER trg_PhatSinhMaQR
BEFORE UPDATE OF TRANGTHAIDATTRUOC ON DATCHO
FOR EACH ROW
WHEN (NEW.TRANGTHAIDATTRUOC IN ('Đã thanh toán thành công') AND OLD.TRANGTHAIDATTRUOC = 'Đang chờ thanh toán')
BEGIN
    -- Mã QR hiện được cấp bởi Java sau khi nhân viên xác nhận thanh toán.
    :NEW.CapNhatLanCuoi := CURRENT_TIMESTAMP;
END;
/
