-- =====================================================================
-- Trigger: TRG_KiemTraTruocXoaHoaDon
-- Mục đích: Ngăn chặn xóa hóa đơn đã thanh toán thành công
-- Thời điểm: BEFORE DELETE ON HOADON
-- =====================================================================

CREATE OR REPLACE TRIGGER TRG_KiemTraTruocXoaHoaDon
BEFORE DELETE ON HOADON
FOR EACH ROW
BEGIN
    -- Không cho phép xóa hóa đơn đã thanh toán thành công
    IF :OLD.TrangThaiThanhToan = 'Đã thanh toán thành công' THEN
        RAISE_APPLICATION_ERROR(
            -20050,
            'Lỗi: Không thể xóa hóa đơn [' || :OLD.SoHD || '] '
            || 'đã thanh toán thành công! '
            || 'Ngày thanh toán: ' || TO_CHAR(:OLD.NgayLapHoaDon, 'DD/MM/YYYY HH24:MI')
        );
    END IF;
END TRG_KiemTraTruocXoaHoaDon;
/
