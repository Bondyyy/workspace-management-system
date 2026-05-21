CREATE OR REPLACE TRIGGER TRG_KiemTraPhieuGiamGiaTruocXoa
BEFORE DELETE ON PHIEUGIAMGIA
FOR EACH ROW
DECLARE
    v_SoHDChuaThanhToan NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_SoHDChuaThanhToan
    FROM HOADON
    WHERE MaPGG = :OLD.MaPGG
      AND TrangThaiThanhToan <> 'Đã thanh toán thành công';

    IF v_SoHDChuaThanhToan > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20070,
            'Lỗi: Phiếu giảm giá [' || :OLD.MaChuSoPGG || '] đang được áp dụng trong '
            || v_SoHDChuaThanhToan || ' hóa đơn chưa thanh toán. Không thể xóa!'
        );
    END IF;
END TRG_KiemTraPhieuGiamGiaTruocXoa;
/
