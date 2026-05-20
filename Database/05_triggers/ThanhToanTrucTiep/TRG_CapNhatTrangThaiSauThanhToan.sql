CREATE OR REPLACE TRIGGER TRG_CapNhatTrangThaiSauThanhToan
BEFORE UPDATE OF PhuongThucThanhToan ON HOADON
FOR EACH ROW
WHEN (NEW.PhuongThucThanhToan IS NOT NULL AND OLD.PhuongThucThanhToan IS NULL)
BEGIN
    -- Tự động cập nhật trạng thái thanh toán thành công khi lễ tân xác nhận phương thức.
    :NEW.TrangThaiThanhToan := 'Đã thanh toán thành công';
    
    IF :NEW.NgayLapHoaDon IS NULL THEN
        :NEW.NgayLapHoaDon := SYSTIMESTAMP;
    END IF;
END;
/
