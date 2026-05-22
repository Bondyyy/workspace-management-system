CREATE OR REPLACE TRIGGER trg_PhatSinhMaQR
BEFORE UPDATE OF TrangThaiDatTruoc ON DATCHO
FOR EACH ROW
WHEN (NEW.TrangThaiDatTruoc = 'Đã thanh toán thành công' AND OLD.TrangThaiDatTruoc = 'Đang chờ thanh toán')
BEGIN
    -- QR nhận chỗ được cấp bởi Java sau khi webhook/mock xác nhận đã nhận tiền.
    -- Không tự sinh QR ở DB để tránh tạo mã trước khi thanh toán thật sự thành công.
    :NEW.CapNhatLanCuoi := CURRENT_TIMESTAMP;
END;
/
