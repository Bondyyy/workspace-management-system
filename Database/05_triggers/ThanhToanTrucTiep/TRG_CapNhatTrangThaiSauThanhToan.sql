-- =====================================================================
-- Trigger: TRG_CapNhatTrangThaiSauThanhToan
-- Mục đích: Tự động cập nhật trạng thái thanh toán thành công 
--           khi lễ tân xác nhận phương thức thanh toán
-- Thời điểm: BEFORE UPDATE OF PhuongThucThanhToan
-- =====================================================================

CREATE OR REPLACE TRIGGER TRG_CapNhatTrangThaiSauThanhToan
BEFORE UPDATE OF PhuongThucThanhToan ON HOADON
FOR EACH ROW
WHEN (NEW.PhuongThucThanhToan IS NOT NULL AND OLD.PhuongThucThanhToan IS NULL)
BEGIN
    -- Tự động cập nhật trạng thái thanh toán thành công
    -- khi lễ tân xác nhận phương thức (Tiền mặt hoặc Chuyển khoản)
    :NEW.TrangThaiThanhToan := 'Đã thanh toán thành công';
    
    -- Cập nhật ngày lập hóa đơn nếu chưa có
    IF :NEW.NgayLapHoaDon IS NULL THEN
        :NEW.NgayLapHoaDon := SYSTIMESTAMP;
    END IF;
END TRG_CapNhatTrangThaiSauThanhToan;
/
